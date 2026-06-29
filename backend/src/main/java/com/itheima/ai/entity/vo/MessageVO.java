package com.itheima.ai.entity.vo;

import com.itheima.ai.entity.po.ChatMessage;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

@NoArgsConstructor
@Data
public class MessageVO {
    private String role;
    private String content;
    private List<AttachmentVO> attachments = List.of();

    public MessageVO(Message message) {
        switch (message.getMessageType()) {
            case USER:
                this.role = "user";
                break;
            case ASSISTANT:
                this.role = "assistant";
                break;
            default:
                this.role = "";
        }
        this.content = message.getText();
    }

    public MessageVO(ChatMessage message) {
        this.role = message.getRole() == null ? "" : message.getRole().getValue();
        this.content = message.getTextContent();
    }

    public MessageVO(ChatMessage message, List<AttachmentVO> attachments) {
        this(message);
        this.attachments = attachments == null ? List.of() : attachments;
    }
}
