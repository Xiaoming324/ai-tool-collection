package com.itheima.ai.controller;

import com.itheima.ai.entity.po.ChatMessage;
import com.itheima.ai.entity.po.ChatSession;
import com.itheima.ai.entity.vo.ChatSessionVO;
import com.itheima.ai.entity.vo.MessageVO;
import com.itheima.ai.entity.vo.Result;
import com.itheima.ai.enums.ChatType;
import com.itheima.ai.exception.BusinessException;
import com.itheima.ai.service.IChatMessageService;
import com.itheima.ai.service.IChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ai/history")
@RequiredArgsConstructor
public class ChatSessionController {

    private final IChatSessionService chatSessionService;
    private final IChatMessageService chatMessageService;

    @GetMapping("/{type}")
    public Result<List<ChatSessionVO>> listSessions(@PathVariable ChatType type,
                                                    @AuthenticationPrincipal Long userId) {
        List<ChatSessionVO> sessions = chatSessionService.listSessions(userId, type).stream()
                .map(ChatSessionVO::new)
                .toList();
        return Result.ok(sessions);
    }

    @GetMapping("/{type}/{chatId}")
    public Result<List<MessageVO>> getMessages(@PathVariable ChatType type,
                                               @PathVariable String chatId,
                                               @AuthenticationPrincipal Long userId) {
        ChatSession session = chatSessionService.getByUserIdAndTypeAndChatId(userId, type, chatId);
        if (session == null) {
            throw new BusinessException("Session does not exist");
        }
        List<ChatMessage> chatMessages = chatMessageService.listBySessionId(session.getId());
        List<MessageVO> messageVOS = chatMessages.stream().map(MessageVO::new).toList();

        return Result.ok(messageVOS);
    }
}
