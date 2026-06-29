package com.itheima.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.ai.entity.po.ChatSession;
import com.itheima.ai.enums.ChatType;

import java.util.List;

public interface IChatSessionService extends IService<ChatSession> {
    void createOrUpdateSession(Long userId, ChatType type, String chatId, String prompt);

    List<ChatSession> listSessions(Long userId, ChatType type);

    ChatSession getByUserIdAndTypeAndChatId(Long userId, ChatType type, String chatId);
}
