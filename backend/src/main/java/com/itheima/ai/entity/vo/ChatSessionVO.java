package com.itheima.ai.entity.vo;

import com.itheima.ai.entity.po.ChatSession;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatSessionVO {
    private Long id;
    private String chatId;
    private String title;

    public ChatSessionVO(ChatSession session) {
        this.id = session.getId();
        this.chatId = session.getChatId();
        this.title = session.getTitle();
    }
}
