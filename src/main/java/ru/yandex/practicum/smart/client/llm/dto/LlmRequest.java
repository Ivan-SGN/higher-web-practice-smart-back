package ru.yandex.practicum.smart.client.llm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LlmRequest(
        String model,
        List<LlmMessage> messages,
        boolean stream,
        String format,
        Boolean think
) {
}