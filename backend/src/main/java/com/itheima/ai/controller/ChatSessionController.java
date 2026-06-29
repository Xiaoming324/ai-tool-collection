package com.itheima.ai.controller;

import com.itheima.ai.entity.po.ChatMessage;
import com.itheima.ai.entity.po.ChatMessageAttachment;
import com.itheima.ai.entity.po.ChatSession;
import com.itheima.ai.entity.po.StoredFile;
import com.itheima.ai.entity.vo.AttachmentVO;
import com.itheima.ai.entity.vo.ChatSessionVO;
import com.itheima.ai.entity.vo.MessageVO;
import com.itheima.ai.entity.vo.Result;
import com.itheima.ai.enums.ChatType;
import com.itheima.ai.exception.BusinessException;
import com.itheima.ai.service.S3FileService;
import com.itheima.ai.service.IChatMessageAttachmentService;
import com.itheima.ai.service.IChatMessageService;
import com.itheima.ai.service.IChatSessionService;
import com.itheima.ai.service.IStoredFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai/history")
@RequiredArgsConstructor
public class ChatSessionController {

    private final IChatSessionService chatSessionService;
    private final IChatMessageService chatMessageService;
    private final IChatMessageAttachmentService chatMessageAttachmentService;
    private final IStoredFileService storedFileService;
    private final S3FileService s3FileService;

    @GetMapping("/{type}")
    public Result<List<ChatSessionVO>> listSessions(@PathVariable ChatType type,
                                                    @AuthenticationPrincipal Long userId) {
        List<ChatSessionVO> sessions = chatSessionService.listSessions(userId, type).stream()
                .map(ChatSessionVO::new)
                .toList();
        return Result.ok(sessions);
    }

    @GetMapping("/{type}/{chatId}")
    public Result<List<MessageVO>> getMessages(@PathVariable ChatType type,
                                               @PathVariable String chatId,
                                               @AuthenticationPrincipal Long userId) {
        ChatSession session = chatSessionService.getByUserIdAndTypeAndChatId(userId, type, chatId);
        if (session == null) {
            throw new BusinessException("Session does not exist");
        }
        List<ChatMessage> chatMessages = chatMessageService.listBySessionId(session.getId());
        List<MessageVO> messageVOS = chatMessages.stream()
                .map(this::toMessageVO)
                .toList();

        return Result.ok(messageVOS);
    }

    private MessageVO toMessageVO(ChatMessage message) {
        List<ChatMessageAttachment> attachments = chatMessageAttachmentService.listByMessageId(message.getId());
        if (attachments.isEmpty()) {
            return new MessageVO(message);
        }

        List<Long> fileIds = attachments.stream()
                .map(ChatMessageAttachment::getFileId)
                .toList();
        Map<Long, StoredFile> storedFileMap = storedFileService.listByIds(fileIds).stream()
                .collect(Collectors.toMap(StoredFile::getId, Function.identity()));
        List<AttachmentVO> attachmentVOS = attachments.stream()
                .map(attachment -> storedFileMap.get(attachment.getFileId()))
                .filter(java.util.Objects::nonNull)
                .map(this::toAttachmentVO)
                .toList();
        return new MessageVO(message, attachmentVOS);
    }

    private AttachmentVO toAttachmentVO(StoredFile storedFile) {
        return new AttachmentVO(storedFile, s3FileService.generatePresignedUrl(storedFile));
    }
}
