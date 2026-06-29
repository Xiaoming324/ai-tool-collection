package com.itheima.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.ai.entity.po.ChatMessage;

import java.util.List;

public interface IChatMessageService extends IService<ChatMessage> {
    int getNextSequenceNo(Long sessionId);

    ChatMessage saveUserMessage(Long sessionId, Long userId, String content);

    ChatMessage saveAssistantMessage(Long sessionId, Long userId, String content);

    List<ChatMessage> listBySessionId(Long sessionId);
}
