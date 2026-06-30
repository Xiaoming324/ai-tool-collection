package com.itheima.ai.controller;

import com.itheima.ai.entity.dto.StoredObjectInfo;
import com.itheima.ai.entity.po.ChatSession;
import com.itheima.ai.entity.po.StoredFile;
import com.itheima.ai.entity.vo.PdfUploadVO;
import com.itheima.ai.entity.vo.Result;
import com.itheima.ai.enums.ChatType;
import com.itheima.ai.enums.FileKind;
import com.itheima.ai.exception.BusinessException;
import com.itheima.ai.service.IChatSessionService;
import com.itheima.ai.service.IStoredFileService;
import com.itheima.ai.service.PdfIngestionService;
import com.itheima.ai.service.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@RestController
@RequestMapping("/ai/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final IChatSessionService chatSessionService;
    private final IStoredFileService storedFileService;
    private final S3FileService s3FileService;
    private final PdfIngestionService pdfIngestionService;

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
}
