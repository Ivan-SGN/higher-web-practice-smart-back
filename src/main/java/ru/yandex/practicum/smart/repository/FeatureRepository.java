package ru.yandex.practicum.smart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.smart.model.Feature;
import ru.yandex.practicum.smart.model.enums.FeatureStatus;
import ru.yandex.practicum.smart.model.enums.FeatureType;

import java.util.List;

public interface FeatureRepository extends JpaRepository<Feature, Long> {

    boolean existsByChatIdAndStatus(Long chatId, FeatureStatus status);

    List<Feature> findAllByTypeAndStatus(FeatureType type, FeatureStatus status);
}