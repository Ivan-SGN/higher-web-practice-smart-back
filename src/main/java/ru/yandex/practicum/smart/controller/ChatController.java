package ru.yandex.practicum.smart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.smart.dto.*;
import ru.yandex.practicum.smart.model.enums.FeatureType;
import ru.yandex.practicum.smart.service.ChatService;
import ru.yandex.practicum.smart.service.FeatureService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final FeatureService featureService;

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


    @PostMapping("/{chatId}/features/generate")
    public FeatureResponse generateFeature(
            @PathVariable Long chatId,
            @RequestParam FeatureType type
    ) {
        return featureService.generate(chatId, type);
    }

    @PostMapping("/features/{featureId}/execute")
    public FeatureResponse executeFeature(
            @PathVariable Long featureId,
            @RequestBody(required = false) Map<String, Object> parameters
    ) {
        return featureService.execute(featureId, parameters);
    }

    @PostMapping("/features/{featureId}/retry")
    public FeatureResponse retryFeature(
            @PathVariable Long featureId
    ) {
        return featureService.retry(featureId);
    }
}
