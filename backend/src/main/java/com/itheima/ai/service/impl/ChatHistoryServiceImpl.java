package com.itheima.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.ai.entity.po.ChatHistory;
import com.itheima.ai.enums.ChatType;
import com.itheima.ai.mapper.ChatHistoryMapper;
import com.itheima.ai.service.IChatHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements IChatHistoryService {

    @Override
    public void saveSession(Long userId, ChatType type, String chatId) {
        Long count = lambdaQuery()
                .eq(ChatHistory::getUserId, userId)
                .eq(ChatHistory::getType, type)
                .eq(ChatHistory::getChatId, chatId).count();
        if (count == 0) {
            ChatHistory chatHistory = new ChatHistory();
            chatHistory.setUserId(userId).setType(type).setChatId(chatId);
            save(chatHistory);
        }
    }

    @Override
    public List<String> getChatIds(Long userId, ChatType type) {
        List<ChatHistory> chatHistories = lambdaQuery().eq(ChatHistory::getUserId, userId)
                .eq(ChatHistory::getType, type)
                .orderByDesc(ChatHistory::getCreatedAt)
                .list();
        return chatHistories.stream().map(ChatHistory::getChatId).toList();
    }
}
