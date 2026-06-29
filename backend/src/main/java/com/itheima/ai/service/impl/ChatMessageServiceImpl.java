package com.itheima.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.ai.entity.po.ChatMessage;
import com.itheima.ai.enums.MessageRole;
import com.itheima.ai.mapper.ChatMessageMapper;
import com.itheima.ai.service.IChatMessageService;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

    private static final int MAX_SEQUENCE_RETRIES = 3;

    @Override
    public int getNextSequenceNo(Long sessionId) {
        ChatMessage chatMessage = lambdaQuery()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByDesc(ChatMessage::getSequenceNo)
                .last("limit 1")
                .one();

        if (chatMessage == null || chatMessage.getSequenceNo() == null) {
            return 1;
        }

        return chatMessage.getSequenceNo() + 1;
    }

    @Override
    public ChatMessage saveUserMessage(Long sessionId, Long userId, String content) {
        return saveMessage(sessionId, userId, MessageRole.USER, content);
    }

    @Override
    public ChatMessage saveAssistantMessage(Long sessionId, Long userId, String content) {
        return saveMessage(sessionId, userId, MessageRole.ASSISTANT, content);
    }

    @Override
    public List<ChatMessage> listBySessionId(Long sessionId) {
        return lambdaQuery()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getSequenceNo)
                .list();
    }

    private ChatMessage saveMessage(Long sessionId, Long userId, MessageRole role, String content) {
        for (int attempt = 0; attempt < MAX_SEQUENCE_RETRIES; attempt++) {
            ChatMessage chatMessage = new ChatMessage()
                    .setSessionId(sessionId)
                    .setUserId(userId)
                    .setRole(role)
                    .setSequenceNo(getNextSequenceNo(sessionId))
                    .setTextContent(content)
                    .setCreatedAt(LocalDateTime.now());
            try {
                save(chatMessage);
                return chatMessage;
            } catch (DuplicateKeyException ex) {
                if (attempt == MAX_SEQUENCE_RETRIES - 1) {
                    throw ex;
                }
            }
        }
        throw new IllegalStateException("Unable to save chat message");
    }
}
