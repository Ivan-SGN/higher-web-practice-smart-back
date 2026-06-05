package ru.yandex.practicum.smart.client.llm;

import ru.yandex.practicum.smart.client.llm.dto.LlmMessage;
import ru.yandex.practicum.smart.client.llm.dto.LlmResponse;

import java.util.List;

public interface LlmClient {

    LlmResponse send(List<LlmMessage> messages);
}