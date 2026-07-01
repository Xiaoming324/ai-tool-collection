package com.itheima.ai.controller;

import com.itheima.ai.entity.po.ChatMessage;
import com.itheima.ai.entity.po.ChatSession;
import com.itheima.ai.entity.po.TravelItinerary;
import com.itheima.ai.entity.vo.Result;
import com.itheima.ai.entity.vo.TravelItineraryVO;
import com.itheima.ai.enums.ChatType;
import com.itheima.ai.enums.MessageRole;
import com.itheima.ai.exception.BusinessException;
import com.itheima.ai.service.IChatMessageService;
import com.itheima.ai.service.IChatSessionService;
import com.itheima.ai.service.ITravelItineraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/travel")
@RequiredArgsConstructor
public class TravelController {

    private static final int MAX_MEMORY_MESSAGES = 10;

    private final ChatClient travelChatClient;
    private final IChatSessionService chatSessionService;
    private final IChatMessageService chatMessageService;
    private final ITravelItineraryService travelItineraryService;

    @PostMapping(produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam("prompt") String prompt,
                             @RequestParam("chatId") String chatId,
                             @AuthenticationPrincipal Long userId) {
        chatSessionService.createOrUpdateSession(userId, ChatType.TRAVEL, chatId, prompt);
        ChatSession session = chatSessionService.getByUserIdAndTypeAndChatId(userId, ChatType.TRAVEL, chatId);
        if (session == null) {
            throw new BusinessException("Travel session does not exist");
        }

        List<Message> memoryMessages = buildTravelMemoryMessages(session.getId());

        chatMessageService.saveUserMessage(session.getId(), userId, prompt);
        Flux<String> response = travelChatClient.prompt()
                .messages(memoryMessages)
                .user(prompt)
                .tools(tool -> tool.context(Map.of(
                        "userId", userId,
                        "sessionId", session.getId(),
                        "chatId", chatId
                )))
                .stream()
                .content();
        
        return recordAssistantReply(response, session.getId(), userId);
    }

    @GetMapping("/itineraries")
    public Result<List<TravelItineraryVO>> listItineraries(@AuthenticationPrincipal Long userId) {
        return Result.ok(travelItineraryService.listByUserId(userId).stream()
                .map(TravelItineraryVO::new)
                .toList());
    }

    @GetMapping("/itineraries/{id}")
    public Result<TravelItineraryVO> getItinerary(@PathVariable Long id,
                                                  @AuthenticationPrincipal Long userId) {
        TravelItinerary itinerary = travelItineraryService.getByIdAndUserId(id, userId);
        if (itinerary == null) {
            throw new BusinessException("Itinerary does not exist");
        }
        return Result.ok(new TravelItineraryVO(itinerary));
    }

    private Flux<String> recordAssistantReply(Flux<String> response, Long sessionId, Long userId) {
        StringBuilder assistantReply = new StringBuilder();
        return response
                .doOnNext(assistantReply::append)
                .doOnComplete(() -> {
                    String content = assistantReply.toString();
                    if (StringUtils.hasText(content)) {
                        chatMessageService.saveAssistantMessage(sessionId, userId, content);
                    }
                });
    }

    private List<Message> buildTravelMemoryMessages(Long sessionId) {
        List<ChatMessage> chatMessages = chatMessageService.listBySessionId(sessionId);
        int fromIndex = Math.max(chatMessages.size() - MAX_MEMORY_MESSAGES, 0);
        List<Message> memoryMessages = new ArrayList<>();

        for (ChatMessage chatMessage : chatMessages.subList(fromIndex, chatMessages.size())) {
            if (!StringUtils.hasText(chatMessage.getTextContent())) {
                continue;
            }
            if (chatMessage.getRole() == MessageRole.USER) {
                memoryMessages.add(new UserMessage(chatMessage.getTextContent()));
            } else if (chatMessage.getRole() == MessageRole.ASSISTANT) {
                memoryMessages.add(new AssistantMessage(chatMessage.getTextContent()));
            }
        }

        return memoryMessages;
    }
}
