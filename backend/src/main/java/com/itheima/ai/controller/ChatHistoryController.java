package com.itheima.ai.controller;

import com.itheima.ai.entity.vo.MessageVO;
import com.itheima.ai.entity.vo.Result;
import com.itheima.ai.enums.ChatType;
import com.itheima.ai.service.IChatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ai/history")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final IChatHistoryService chatHistoryService;
    private final ChatMemory chatMemory;

    // 列出当前用户某类型的会话
    @GetMapping("/{type}")
    public Result<List<String>> listSessions(@PathVariable ChatType type,
                                             @AuthenticationPrincipal Long userId) {
        return Result.ok(chatHistoryService.getChatIds(userId, type));
    }

    // 返回某会话的历史消息
    @GetMapping("/{type}/{chatId}")
    public Result<List<MessageVO>> getMessages(@PathVariable ChatType type,
                                               @PathVariable String chatId,
                                               @AuthenticationPrincipal Long userId) {
        String conversationId = userId + ":" + type.getValue() + ":" + chatId;
        List<Message> messages = chatMemory.get(conversationId);
        List<MessageVO> vos = messages.stream().map(MessageVO::new).toList();
        return Result.ok(vos);
    }
}
