package ru.yandex.practicum.smart.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import ru.yandex.practicum.smart.client.llm.LlmClient;
import ru.yandex.practicum.smart.client.llm.dto.LlmResponse;
import ru.yandex.practicum.smart.client.llm.dto.LlmResponseMessage;
import ru.yandex.practicum.smart.dto.FeatureResponse;
import ru.yandex.practicum.smart.dto.GeneratedFeature;
import ru.yandex.practicum.smart.exception.ConflictException;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureServiceTest {

    @Mock private ChatRepository chatRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private FeatureRepository featureRepository;
    @Mock private FeatureMapper featureMapper;
    @Mock private FeatureParser featureParser;
    @Mock private MessageMapper messageMapper;
    @Mock private LlmClient llmClient;
    @Mock private PromptProvider promptProvider;
    @Mock private SqlFeatureExecutor sqlFeatureExecutor;

    @InjectMocks
    private FeatureService featureService;

    @Test
    void generateFeatureHappyPathTest() {
        Chat chat = createChat();
        GeneratedFeature generatedFeature = createGeneratedFeature();
        Feature feature = createFeature(chat, FeatureStatus.DRAFT);
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(featureRepository.existsByChatIdAndStatus(1L, FeatureStatus.DRAFT)).thenReturn(false);
        when(promptProvider.getPrompt(FeatureType.SQL)).thenReturn("system prompt");
        when(messageRepository.findAllByChatIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());
        when(llmClient.sendJson(any())).thenReturn(createLlmResponse());
        when(featureParser.parse(any())).thenReturn(generatedFeature);
        when(featureMapper.toEntity(chat, generatedFeature)).thenReturn(feature);
        when(featureRepository.save(any())).thenReturn(feature);
        when(featureMapper.toDto(feature)).thenReturn(createFeatureResponse(FeatureStatus.DRAFT));

        FeatureResponse result = featureService.generate(1L, FeatureType.SQL);

        assertThat(result.status()).isEqualTo(FeatureStatus.DRAFT);
        verify(featureRepository).save(any());
    }

    @Test
    void generateFeatureChatNotFoundTest() {
        when(chatRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> featureService.generate(99L, FeatureType.SQL))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void generateFeatureDraftConflictTest() {
        when(chatRepository.findById(1L)).thenReturn(Optional.of(createChat()));
        when(featureRepository.existsByChatIdAndStatus(1L, FeatureStatus.DRAFT)).thenReturn(true);

        assertThatThrownBy(() -> featureService.generate(1L, FeatureType.SQL))
                .isInstanceOf(ConflictException.class);
    }

@Test
    void executeFeatureHappyPathTest() {
        Feature feature = createFeature(createChat(), FeatureStatus.DRAFT);
        when(featureRepository.findById(1L)).thenReturn(Optional.of(feature));
        when(featureRepository.save(any())).thenReturn(feature);
        when(featureMapper.toDto(any())).thenReturn(createFeatureResponse(FeatureStatus.EXECUTED));

        FeatureResponse result = featureService.execute(1L, Map.of());

        assertThat(result.status()).isEqualTo(FeatureStatus.EXECUTED);
    }

    @Test
    void executeFeatureSqlFailsTest() {
        Feature feature = createFeature(createChat(), FeatureStatus.DRAFT);
        DataAccessException dbError = new DataIntegrityViolationException("table already exists");
        when(featureRepository.findById(1L)).thenReturn(Optional.of(feature));
        when(sqlFeatureExecutor.execute(any(), any())).thenThrow(dbError);
        when(featureRepository.save(any())).thenReturn(feature);
        when(featureMapper.toDto(any())).thenReturn(createFeatureResponse(FeatureStatus.FAILED));

        FeatureResponse result = featureService.execute(1L, Map.of());

        assertThat(result.status()).isEqualTo(FeatureStatus.FAILED);
    }

    @Test
    void executeFeatureNotDraftStatusTest() {
        Feature feature = createFeature(createChat(), FeatureStatus.EXECUTED);
        when(featureRepository.findById(1L)).thenReturn(Optional.of(feature));

        assertThatThrownBy(() -> featureService.execute(1L, Map.of()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    void executeFeatureNotFoundTest() {
        when(featureRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> featureService.execute(99L, Map.of()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void retryFeatureHappyPathTest() {
        Chat chat = createChat();
        Feature feature = createFeature(chat, FeatureStatus.FAILED);
        feature.setErrorMessage("table already exists");
        GeneratedFeature generatedFeature = createGeneratedFeature();
        when(featureRepository.findById(1L)).thenReturn(Optional.of(feature));
        when(promptProvider.getPrompt(FeatureType.SQL)).thenReturn("system prompt");
        when(messageRepository.findAllByChatIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());
        when(llmClient.sendJson(any())).thenReturn(createLlmResponse());
        when(featureParser.parse(any())).thenReturn(generatedFeature);
        when(featureRepository.save(any())).thenReturn(feature);
        when(featureMapper.toDto(any())).thenReturn(createFeatureResponse(FeatureStatus.DRAFT));

        FeatureResponse result = featureService.retry(1L);

        assertThat(result.status()).isEqualTo(FeatureStatus.DRAFT);
        verify(featureRepository).save(any());
    }

    @Test
    void retryFeatureNotFailedStatusTest() {
        Feature feature = createFeature(createChat(), FeatureStatus.DRAFT);
        when(featureRepository.findById(1L)).thenReturn(Optional.of(feature));

        assertThatThrownBy(() -> featureService.retry(1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("FAILED");
    }

    @Test
    void retryFeatureNotFoundTest() {
        when(featureRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> featureService.retry(99L))
                .isInstanceOf(NotFoundException.class);
    }

    private Chat createChat() {
        Chat chat = new Chat();
        chat.setId(1L);
        chat.setTitle("Test chat");
        return chat;
    }

    private Feature createFeature(Chat chat, FeatureStatus status) {
        Feature feature = new Feature();
        feature.setId(1L);
        feature.setChat(chat);
        feature.setType(FeatureType.SQL);
        feature.setContent("CREATE TABLE users (id BIGSERIAL PRIMARY KEY)");
        feature.setStatus(status);
        return feature;
    }

    private GeneratedFeature createGeneratedFeature() {
        return new GeneratedFeature(FeatureType.SQL, "Creates users table",
                "CREATE TABLE users (id BIGSERIAL PRIMARY KEY)", List.of(), List.of());
    }

    private LlmResponse createLlmResponse() {
        return new LlmResponse("qwen3:1.7b", "2026-01-01T00:00:00Z",
                new LlmResponseMessage("assistant", "{\"type\":\"SQL\"}"), true);
    }

    private FeatureResponse createFeatureResponse(FeatureStatus status) {
        return new FeatureResponse(1L, FeatureType.SQL, "Creates users table",
                "CREATE TABLE users (id BIGSERIAL PRIMARY KEY)", status, List.of(), List.of(), null);
    }
}