package com.itheima.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.ai.entity.po.ChatMessage;
import com.itheima.ai.entity.po.ChatSession;
import com.itheima.ai.entity.po.StoredFile;
import com.itheima.ai.enums.ChatType;
import com.itheima.ai.exception.BusinessException;
import com.itheima.ai.mapper.ChatSessionMapper;
import com.itheima.ai.service.IChatMessageAttachmentService;
import com.itheima.ai.service.IChatMessageService;
import com.itheima.ai.service.IChatSessionService;
import com.itheima.ai.service.IStoredFileService;
import com.itheima.ai.service.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements IChatSessionService {

    private static final int TITLE_MAX_LENGTH = 20;
    private final IChatMessageService chatMessageService;
    private final IChatMessageAttachmentService chatMessageAttachmentService;
    private final IStoredFileService storedFileService;
    private final S3FileService s3FileService;
    private final ChatMemoryRepository chatMemoryRepository;
    private final VectorStore vectorStore;

    @Override
    public void createOrUpdateSession(Long userId, ChatType type, String chatId, String prompt) {
        ChatSession existingSession = lambdaQuery()
                .eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getType, type)
                .eq(ChatSession::getChatId, chatId)
                .one();

        String title = buildTitle(prompt);
        if (existingSession == null) {
            ChatSession chatSession = new ChatSession();
            chatSession.setUserId(userId)
                    .setType(type)
                    .setChatId(chatId)
                    .setTitle(title)
                    .setCreatedAt(LocalDateTime.now())
                    .setUpdatedAt(LocalDateTime.now());
            save(chatSession);
            return;
        }

        ChatSession sessionToUpdate = new ChatSession();
        sessionToUpdate.setId(existingSession.getId())
                .setUpdatedAt(LocalDateTime.now());
        if (!StringUtils.hasText(existingSession.getTitle()) && StringUtils.hasText(title)) {
            sessionToUpdate.setTitle(title);
        }
        updateById(sessionToUpdate);
    }

    @Override
    public List<ChatSession> listSessions(Long userId, ChatType type) {
        return lambdaQuery()
                .eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getType, type)
                .orderByDesc(ChatSession::getUpdatedAt)
                .list();
    }

    @Override
    public ChatSession getByUserIdAndTypeAndChatId(Long userId, ChatType type, String chatId) {
        return lambdaQuery()
                .eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getType, type)
                .eq(ChatSession::getChatId, chatId)
                .one();
    }

    @Override
    @Transactional
    public void deleteSession(Long userId, ChatType type, String chatId) {
        ChatSession session = getByUserIdAndTypeAndChatId(userId, type, chatId);
        if (session == null) {
            throw new BusinessException("Session does not exist");
        }

        List<ChatMessage> chatMessages = chatMessageService.listBySessionId(session.getId());
        List<Long> messageIds = chatMessages.stream()
                .map(ChatMessage::getId)
                .toList();
        List<StoredFile> storedFiles = storedFileService.listBySessionId(session.getId());

        for (StoredFile storedFile : storedFiles) {
            s3FileService.deleteObject(storedFile);
        }

        if (type == ChatType.PDF) {
            deletePdfVectors(userId, session.getId());
        }

        String conversationId = buildConversationId(userId, type, chatId);
        chatMemoryRepository.deleteByConversationId(conversationId);

        chatMessageAttachmentService.removeByMessageIds(messageIds);

        if (!messageIds.isEmpty()) {
            chatMessageService.removeByIds(messageIds);
        }

        List<Long> storedFileIds = storedFiles.stream()
                .map(StoredFile::getId)
                .collect(Collectors.toList());
        if (!storedFileIds.isEmpty()) {
            storedFileService.removeByIds(storedFileIds);
        }

        removeById(session.getId());
    }

    private String buildTitle(String prompt) {
        if (!StringUtils.hasText(prompt)) {
            return null;
        }

        String normalized = prompt.trim().replaceAll("\\s+", " ");
        if (normalized.length() <= TITLE_MAX_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, TITLE_MAX_LENGTH);
    }

    private String buildConversationId(Long userId, ChatType type, String chatId) {
        return userId + ":" + type.getValue() + ":" + chatId;
    }

    private void deletePdfVectors(Long userId, Long sessionId) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        vectorStore.delete(builder.and(
                builder.eq("user_id", userId),
                builder.eq("session_id", sessionId)
        ).build());
    }
}
