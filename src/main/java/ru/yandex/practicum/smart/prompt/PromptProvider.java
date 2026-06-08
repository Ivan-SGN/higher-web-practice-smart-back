package ru.yandex.practicum.smart.prompt;

import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.smart.model.enums.FeatureType;

import java.nio.charset.StandardCharsets;

@Component
public class PromptProvider {

    @Value("classpath:prompts/sql_feature.txt")
    private Resource sqlPrompt;

    public String getPrompt(FeatureType featureType) {
        return switch (featureType) {
            case SQL -> readPrompt(sqlPrompt);
        };
    }

    @SneakyThrows
    private String readPrompt(Resource resource) {
        return new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );
    }
}