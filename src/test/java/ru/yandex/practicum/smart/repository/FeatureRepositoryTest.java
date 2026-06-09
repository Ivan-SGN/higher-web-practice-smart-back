package ru.yandex.practicum.smart.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import ru.yandex.practicum.smart.model.Chat;
import ru.yandex.practicum.smart.model.Feature;
import ru.yandex.practicum.smart.model.enums.FeatureStatus;
import ru.yandex.practicum.smart.model.enums.FeatureType;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FeatureRepositoryTest {

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Test
    void existsByChatIdAndStatusDraftExistsTest() {
        Chat chat = chatRepository.save(createChat());
        featureRepository.save(createFeature(chat, FeatureStatus.DRAFT));

        boolean result = featureRepository.existsByChatIdAndStatus(chat.getId(), FeatureStatus.DRAFT);

        assertThat(result).isTrue();
    }

    @Test
    void existsByChatIdAndStatusNoDraftTest() {
        Chat chat = chatRepository.save(createChat());
        featureRepository.save(createFeature(chat, FeatureStatus.EXECUTED));

        boolean result = featureRepository.existsByChatIdAndStatus(chat.getId(), FeatureStatus.DRAFT);

        assertThat(result).isFalse();
    }

    private Chat createChat() {
        Chat chat = new Chat();
        chat.setTitle("Test chat");
        return chat;
    }

    private Feature createFeature(Chat chat, FeatureStatus status) {
        Feature feature = new Feature();
        feature.setChat(chat);
        feature.setType(FeatureType.SQL);
        feature.setContent("SELECT 1");
        feature.setStatus(status);
        return feature;
    }
}