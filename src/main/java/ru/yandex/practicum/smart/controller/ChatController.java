package ru.yandex.practicum.smart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.smart.dto.SendMessageRequest;
import ru.yandex.practicum.smart.dto.SendMessageResponse;
import ru.yandex.practicum.smart.dto.MessageResponse;
import ru.yandex.practicum.smart.dto.CreateChatRequest;
import ru.yandex.practicum.smart.dto.ChatResponse;
import ru.yandex.practicum.smart.service.ChatService;
import java.util.List;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ChatResponse createChat(
            @RequestBody @Valid CreateChatRequest request
    ) {
        return chatService.createChat(request);
    }

    @PostMapping("/{chatId}/messages")
    public SendMessageResponse sendMessage(
            @PathVariable("chatId") Long chatId,
            @RequestBody @Valid SendMessageRequest request
    ) {
        return chatService.sendMessage(chatId, request);
    }

    @GetMapping("/{chatId}/messages")
    public List<MessageResponse> getMessages(
            @PathVariable("chatId") Long chatId
    ) {
        return chatService.getMessages(chatId);
    }
}
