package com.itheima.ai.controller;

import com.itheima.ai.entity.po.ChatSession;
import com.itheima.ai.enums.ChatType;
import com.itheima.ai.service.IChatMessageService;
import com.itheima.ai.service.IChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.content.Media;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient chatClient;
    private final IChatSessionService chatSessionService;
    private final IChatMessageService chatMessageService;

    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam("prompt") String prompt,
                             @RequestParam("chatId") String chatId,
                             @RequestParam(value = "files", required = false) List<MultipartFile> files,
                             @AuthenticationPrincipal Long userId) {
        /*
         * @AuthenticationPrincipal Long userId：自动取出 SecurityContext 里那个 principal——也就是 JwtAuthFilter 里 new UsernamePasswordAuthenticationToken(userId, ...) 设的 userId。
         */
        // Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 用 userId type 给会话 id 加前缀，实现按用户隔离
        String conversationId = userId + ":" + ChatType.CHAT.getValue() + ":" + chatId;

        chatSessionService.createOrUpdateSession(userId, ChatType.CHAT, chatId, prompt);
        ChatSession session = chatSessionService.getByUserIdAndTypeAndChatId(userId, ChatType.CHAT, chatId);
        chatMessageService.saveUserMessage(session.getId(), userId, prompt);

        Flux<String> response;
        if (files == null || files.isEmpty()) {
            response = textChat(prompt, conversationId);
        } else {
            response = multiModalChat(prompt, conversationId, files);
        }
        return recordAssistantReply(response, session.getId(), userId);
    }

    // 纯文字
    private Flux<String> textChat(String prompt, String conversationId) {
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }

    // 文字 + 图片
    private Flux<String> multiModalChat(String prompt, String conversationId, List<MultipartFile> files) {
        // 1.解析多媒体
        List<Media> medias = files.stream()
                .map(file -> new Media(
                        MimeType.valueOf(Objects.requireNonNull(file.getContentType())),
                        file.getResource()))
                .toList();
        // 2.请求模型
        return chatClient.prompt()
                .user(p -> p.text(prompt).media(medias.toArray(Media[]::new)))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
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
}
