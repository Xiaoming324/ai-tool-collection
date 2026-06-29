package com.itheima.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.ai.entity.po.ChatMessageAttachment;
import com.itheima.ai.mapper.ChatMessageAttachmentMapper;
import com.itheima.ai.service.IChatMessageAttachmentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMessageAttachmentServiceImpl extends ServiceImpl<ChatMessageAttachmentMapper, ChatMessageAttachment> implements IChatMessageAttachmentService {

    @Override
    public ChatMessageAttachment bindFileToMessage(Long messageId, Long fileId, Integer sortOrder) {
        ChatMessageAttachment attachment = new ChatMessageAttachment()
                .setMessageId(messageId)
                .setFileId(fileId)
                .setSortOrder(sortOrder == null ? 0 : sortOrder)
                .setCreatedAt(LocalDateTime.now());
        save(attachment);
        return attachment;
    }

    @Override
    public List<ChatMessageAttachment> listByMessageId(Long messageId) {
        return lambdaQuery()
                .eq(ChatMessageAttachment::getMessageId, messageId)
                .orderByAsc(ChatMessageAttachment::getSortOrder)
                .orderByAsc(ChatMessageAttachment::getCreatedAt)
                .list();
    }
}
