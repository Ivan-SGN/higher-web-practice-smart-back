package ru.yandex.practicum.smart.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.smart.dto.GeneratedFeature;
import ru.yandex.practicum.smart.exception.ValidationException;

@Component
@RequiredArgsConstructor
public class FeatureParser {

    private final ObjectMapper objectMapper;

    public GeneratedFeature parse(String response) {
        try {
            return objectMapper.readValue(extractJson(response), GeneratedFeature.class);
        } catch (Exception exception) {
            throw new ValidationException("Cannot parse generated feature: " + exception.getMessage());
        }
    }

    private String extractJson(String response) {
        String stripped = response.replaceAll("(?s)<think>.*?</think>", "").trim();
        int start = stripped.indexOf('{');
        int end = stripped.lastIndexOf('}');
        if (start == -1 || end == -1 || end < start) {
            throw new ValidationException("No JSON object found in LLM response");
        }
        return stripped.substring(start, end + 1);
    }
}