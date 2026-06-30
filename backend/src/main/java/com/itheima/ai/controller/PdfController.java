package com.itheima.ai.controller;

import com.itheima.ai.entity.dto.StoredObjectInfo;
import com.itheima.ai.entity.po.ChatSession;
import com.itheima.ai.entity.po.StoredFile;
import com.itheima.ai.entity.vo.PdfUploadVO;
import com.itheima.ai.entity.vo.Result;
import com.itheima.ai.enums.ChatType;
import com.itheima.ai.enums.FileKind;
import com.itheima.ai.exception.BusinessException;
import com.itheima.ai.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.Locale;

@RestController
@RequestMapping("/ai/pdf")
@RequiredArgsConstructor
public class PdfController {
    private final IChatSessionService chatSessionService;
    private final IChatMessageService chatMessageService;
    private final IStoredFileService storedFileService;
    private final S3FileService s3FileService;
    private final PdfIngestionService pdfIngestionService;
    private final ChatClient pdfChatClient;


    @PostMapping("/upload/{chatId}")
    public Result<PdfUploadVO> uploadPdf(@PathVariable String chatId,
                                         @RequestParam("file") MultipartFile file,
                                         @AuthenticationPrincipal Long userId) {
        if (!isPdfFile(file)) {
            throw new BusinessException("Only PDF files are allowed");
        }

        String titleSource = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : chatId;
        chatSessionService.createOrUpdateSession(userId, ChatType.PDF, chatId, titleSource);

        ChatSession session = chatSessionService.getByUserIdAndTypeAndChatId(userId, ChatType.PDF, chatId);
        if (session == null) {
            throw new BusinessException("Failed to create PDF session");
        }

        StoredObjectInfo storedObjectInfo = s3FileService.uploadPdf(userId, chatId, file);
        StoredFile storedFile = storedFileService.saveFileMetadata(
                userId,
                session.getId(),
                FileKind.PDF,
                file,
                storedObjectInfo.getBucket(),
                storedObjectInfo.getKey()
        );
        pdfIngestionService.ingestPdf(storedFile);
        return Result.ok(new PdfUploadVO(chatId, storedFile));
    }

    @GetMapping("/file/{fileId}")
    public Result<String> getPdfFileUrl(@PathVariable Long fileId,
                                        @AuthenticationPrincipal Long userId) {
        StoredFile storedFile = storedFileService.getByIdAndUserId(fileId, userId);
        if (storedFile == null) {
            throw new BusinessException("File does not exist");
        }
        if (storedFile.getFileKind() != FileKind.PDF) {
            throw new BusinessException("File is not a PDF");
        }
        return Result.ok(s3FileService.generatePresignedUrl(storedFile));
    }

    @PostMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam("prompt") String prompt,
                             @RequestParam("chatId") String chatId,
                             @AuthenticationPrincipal Long userId) {
        ChatSession session = chatSessionService.getByUserIdAndTypeAndChatId(userId, ChatType.PDF, chatId);
        if (session == null) {
            throw new BusinessException("PDF session does not exist");
        }

        if (storedFileService.listBySessionIdAndFileKind(session.getId(), FileKind.PDF).isEmpty()) {
            throw new BusinessException("No PDF file found in this session");
        }

        String conversationId = userId + ":" + ChatType.PDF.getValue() + ":" + chatId;
        chatMessageService.saveUserMessage(session.getId(), userId, prompt);
        String filterExpression = "user_id == " + userId + " AND session_id == " + session.getId();

        Flux<String> response = pdfChatClient.prompt()
                .user(prompt)
                .advisors(advisor -> advisor
                        .param(ChatMemory.CONVERSATION_ID, conversationId)
                        .param(QuestionAnswerAdvisor.FILTER_EXPRESSION, filterExpression))
                .stream()
                .content();

        return recordAssistantReply(response, session.getId(), userId);
    }

    private boolean isPdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        if ("application/pdf".equalsIgnoreCase(file.getContentType())) {
            return true;
        }

        String filename = file.getOriginalFilename();
        return StringUtils.hasText(filename) && filename.toLowerCase(Locale.ROOT).endsWith(".pdf");
    }

    private Flux<String> recordAssistantReply(Flux<String> response, Long sessionId, Long userId) {
        StringBuilder assistantReply = new StringBuilder();
        return response
                .doOnNext(assistantReply::append)
                .doOnComplete(() -> {
                    String content = assistantReply.toString();
                    if (StringUtils.hasText(content)) {
                        chatMessageService.saveAssistantMessage(sessionId, userId, content);
                    }
                });
    }
}
