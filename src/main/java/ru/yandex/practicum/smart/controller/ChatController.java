package ru.yandex.practicum.smart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.smart.dto.SendMessageRequest;
import ru.yandex.practicum.smart.dto.SendMessageResponse;
import ru.yandex.practicum.smart.service.ChatService;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/messages")
    public SendMessageResponse sendMessage(
            @RequestBody SendMessageRequest request
    ) {
        return chatService.sendMessage(request);
    }
}
