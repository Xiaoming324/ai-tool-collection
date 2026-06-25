package com.itheima.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.ai.entity.po.ChatHistory;
import com.itheima.ai.enums.ChatType;

import java.util.List;


public interface IChatHistoryService extends IService<ChatHistory> {
    void saveSession(Long userId, ChatType type, String chatId); // 登记会话(已存在则跳过)

    List<String> getChatIds(Long userId, ChatType type); // 列出该用户某类型的会话
}
