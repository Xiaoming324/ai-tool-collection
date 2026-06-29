package com.itheima.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.ai.entity.po.ChatSession;
import com.itheima.ai.enums.ChatType;
import com.itheima.ai.mapper.ChatSessionMapper;
import com.itheima.ai.service.IChatSessionService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements IChatSessionService {

    private static final int TITLE_MAX_LENGTH = 20;

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
}
