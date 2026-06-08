package ru.yandex.practicum.smart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;
import ru.yandex.practicum.smart.client.llm.LlmClient;
import ru.yandex.practicum.smart.client.llm.dto.LlmMessage;
import ru.yandex.practicum.smart.client.llm.dto.LlmResponse;
import ru.yandex.practicum.smart.dto.FeatureResponse;
import ru.yandex.practicum.smart.dto.GeneratedFeature;
import ru.yandex.practicum.smart.exception.NotFoundException;
import ru.yandex.practicum.smart.exception.ValidationException;
import ru.yandex.practicum.smart.executor.SqlFeatureExecutor;
import ru.yandex.practicum.smart.mapper.FeatureMapper;
import ru.yandex.practicum.smart.mapper.MessageMapper;
import ru.yandex.practicum.smart.model.Chat;
import ru.yandex.practicum.smart.model.Feature;
import ru.yandex.practicum.smart.model.enums.FeatureStatus;
import ru.yandex.practicum.smart.model.enums.FeatureType;
import ru.yandex.practicum.smart.parser.FeatureParser;
import ru.yandex.practicum.smart.prompt.PromptProvider;
import ru.yandex.practicum.smart.repository.ChatRepository;
import ru.yandex.practicum.smart.repository.FeatureRepository;
import ru.yandex.practicum.smart.repository.MessageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final FeatureRepository featureRepository;
    private final FeatureMapper featureMapper;
    private final FeatureParser featureParser;
    private final MessageMapper messageMapper;
    private final LlmClient llmClient;
    private final PromptProvider promptProvider;
    private final SqlFeatureExecutor sqlFeatureExecutor;

    public FeatureResponse generate(Long chatId, FeatureType featureType) {
        Chat chat = getChatOrThrow(chatId);

        GeneratedFeature generatedFeature = generateFeature(chatId, featureType);
        Feature feature = featureMapper.toEntity(chat, generatedFeature);
        feature.setStatus(FeatureStatus.DRAFT);
        Feature savedFeature = featureRepository.save(feature);

        log.info("Feature generated featureId={} chatId={} type={}", savedFeature.getId(), chatId, featureType);
        return featureMapper.toDto(savedFeature);
    }

    public FeatureResponse execute(Long featureId, Map<String, Object> parameters) {
        Feature feature = getFeatureOrThrow(featureId);
        if (feature.getStatus() != FeatureStatus.DRAFT) {
            throw new ValidationException("Only DRAFT features can be executed");
        }
        try {
            sqlFeatureExecutor.execute(feature.getContent(), parameters);
            feature.setStatus(FeatureStatus.EXECUTED);
            feature.setErrorMessage(null);
            log.info("Feature executed featureId={}", featureId);
        } catch (DataAccessException e) {
            feature.setStatus(FeatureStatus.FAILED);
            feature.setErrorMessage(e.getMessage());
            log.warn("Feature execution failed featureId={} error={}", featureId, e.getMessage());
        }
        return featureMapper.toDto(featureRepository.save(feature));
    }

    private Feature getFeatureOrThrow(Long featureId) {
        return featureRepository.findById(featureId)
                .orElseThrow(() -> {
                    log.warn("Feature not found id={}", featureId);
                    return new NotFoundException("Feature not found = " + featureId);
                });
    }

    private GeneratedFeature generateFeature(Long chatId, FeatureType featureType) {
        List<LlmMessage> messages = buildMessages(chatId, featureType);
        LlmResponse response = llmClient.sendJson(messages);
        GeneratedFeature generatedFeature = featureParser.parse(response.message().content());
        validateFeatureType(featureType, generatedFeature);
        return generatedFeature;
    }

    private List<LlmMessage> buildMessages(Long chatId, FeatureType featureType) {
        List<LlmMessage> messages = new ArrayList<>();
        messages.add(new LlmMessage("system", promptProvider.getPrompt(featureType)));
        messages.addAll(getChatHistory(chatId));
        return messages;
    }

    private void validateFeatureType(FeatureType expectedType, GeneratedFeature generatedFeature) {
        if (expectedType != generatedFeature.type()) {
            throw new ValidationException("Generated feature type mismatch");
        }
    }

    private List<LlmMessage> getChatHistory(Long chatId) {
        return messageRepository.findAllByChatIdOrderByCreatedAtAsc(chatId)
                .stream()
                .map(messageMapper::toLlmMessage)
                .toList();
    }

    private Chat getChatOrThrow(Long chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> {
                    log.warn("Chat not found id={}", chatId);
                    return new NotFoundException(
                            "Chat not found = " + chatId
                    );
                });
    }
}