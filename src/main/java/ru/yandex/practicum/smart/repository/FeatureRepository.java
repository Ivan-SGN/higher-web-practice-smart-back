package ru.yandex.practicum.smart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.smart.model.Feature;

public interface FeatureRepository extends JpaRepository<Feature, Long> {
}