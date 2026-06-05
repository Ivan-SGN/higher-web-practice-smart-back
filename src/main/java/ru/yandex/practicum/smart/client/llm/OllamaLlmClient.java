package ru.yandex.practicum.smart.client.llm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.smart.client.llm.dto.LlmMessage;
import ru.yandex.practicum.smart.client.llm.dto.LlmRequest;
import ru.yandex.practicum.smart.client.llm.dto.LlmResponse;
import ru.yandex.practicum.smart.config.LlmProperties;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OllamaLlmClient implements LlmClient {

    private final LlmProperties llmProperties;
    private final RestClient restClient;

    @Override
    public LlmResponse send(List<LlmMessage> messages) {
        LlmRequest request = new LlmRequest(
                llmProperties.model(),
                messages,
                false
        );

        return restClient.post()
                .uri(llmProperties.baseUrl() + "/api/chat")
                .body(request)
                .retrieve()
                .body(LlmResponse.class);
    }
}

