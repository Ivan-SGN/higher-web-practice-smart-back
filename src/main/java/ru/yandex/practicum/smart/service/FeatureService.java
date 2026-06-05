import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.smart.client.llm.LlmClient;
import ru.yandex.practicum.smart.client.llm.dto.LlmMessage;
import ru.yandex.practicum.smart.client.llm.dto.LlmResponse;
import ru.yandex.practicum.smart.dto.FeatureResponse;
import ru.yandex.practicum.smart.exception.NotFoundException;
import ru.yandex.practicum.smart.mapper.FeatureMapper;
import ru.yandex.practicum.smart.mapper.MessageMapper;
import ru.yandex.practicum.smart.model.Chat;
import ru.yandex.practicum.smart.model.Feature;
import ru.yandex.practicum.smart.model.enums.FeatureStatus;
import ru.yandex.practicum.smart.model.enums.FeatureType;
import ru.yandex.practicum.smart.prompt.PromptProvider;
import ru.yandex.practicum.smart.repository.ChatRepository;
import ru.yandex.practicum.smart.repository.FeatureRepository;
import ru.yandex.practicum.smart.repository.MessageRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final FeatureRepository featureRepository;
    private final FeatureMapper featureMapper;
    private final MessageMapper messageMapper;
    private final LlmClient llmClient;
    private final PromptProvider promptProvider;

    public FeatureResponse generate(
            Long chatId,
            FeatureType featureType
    ) {
        Chat chat = getChatOrThrow(chatId);

        List<LlmMessage> messages = buildMessages(
                chatId,
                featureType
        );

        LlmResponse response = llmClient.send(messages);

        Feature feature = createFeature(
                chat,
                featureType,
                response.message().content()
        );

        Feature savedFeature = featureRepository.save(feature);

        log.info(
                "Feature generated featureId={} chatId={} type={}",
                savedFeature.getId(),
                chatId,
                featureType
        );

        return featureMapper.toDto(savedFeature);
    }

    private List<LlmMessage> buildMessages(
            Long chatId,
            FeatureType featureType
    ) {
        List<LlmMessage> messages = new ArrayList<>();

        messages.add(
                new LlmMessage(
                        "system",
                        promptProvider.getPrompt(featureType)
                )
        );

        messages.addAll(getChatHistory(chatId));

        return messages;
    }

    private Feature createFeature(
            Chat chat,
            FeatureType featureType,
            String content
    ) {
        Feature feature = featureMapper.toEntity(
                chat,
                content
        );

        feature.setType(featureType);
        feature.setStatus(FeatureStatus.DRAFT);
        feature.setDescription("Generated feature");

        return feature;
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