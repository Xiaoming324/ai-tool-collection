package com.itheima.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.ai.entity.po.ChatMessageAttachment;

import java.util.List;

public interface IChatMessageAttachmentService extends IService<ChatMessageAttachment> {
    ChatMessageAttachment bindFileToMessage(Long messageId, Long fileId, Integer sortOrder);

    List<ChatMessageAttachment> listByMessageId(Long messageId);
}
