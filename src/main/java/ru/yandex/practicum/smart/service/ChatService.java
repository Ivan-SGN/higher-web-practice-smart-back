package ru.yandex.practicum.smart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.smart.client.llm.LlmClient;
import ru.yandex.practicum.smart.client.llm.dto.LlmMessage;
import ru.yandex.practicum.smart.client.llm.dto.LlmResponse;
import ru.yandex.practicum.smart.dto.SendMessageResponse;
import ru.yandex.practicum.smart.dto.SendMessageRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final LlmClient llmClient;

    public SendMessageResponse sendMessage(SendMessageRequest request) {
        List<LlmMessage> messages = List.of(
                new LlmMessage("user", request.content())
        );

        LlmResponse response = llmClient.send(messages);

        return new SendMessageResponse(
                response.message().content()
        );
    }
}