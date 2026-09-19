package ru.yandex.practicum.smart.client.llm;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.smart.client.llm.dto.LlmMessage;
import ru.yandex.practicum.smart.client.llm.dto.LlmRequest;
import ru.yandex.practicum.smart.client.llm.dto.LlmResponse;
import ru.yandex.practicum.smart.config.LlmProperties;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OllamaLlmClient implements LlmClient {

    private final LlmProperties llmProperties;
    private final RestTemplate restTemplate;

    @Override
    public LlmResponse send(List<LlmMessage> messages) {
        return doSend(new LlmRequest(llmProperties.model(), messages, false, null, false));
    }

    @Override
    public LlmResponse sendJson(List<LlmMessage> messages) {
        return doSend(new LlmRequest(llmProperties.model(), messages, false, "json", false));
    }

    private LlmResponse doSend(LlmRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LlmRequest> entity = new HttpEntity<>(request, headers);
        return restTemplate.postForObject(llmProperties.baseUrl() + "/api/chat", entity, LlmResponse.class);
    }
}