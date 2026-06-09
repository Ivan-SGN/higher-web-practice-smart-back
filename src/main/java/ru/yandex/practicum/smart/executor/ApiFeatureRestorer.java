package ru.yandex.practicum.smart.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.smart.model.Feature;
import ru.yandex.practicum.smart.model.enums.FeatureStatus;
import ru.yandex.practicum.smart.model.enums.FeatureType;
import ru.yandex.practicum.smart.repository.FeatureRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiFeatureRestorer {

    private final FeatureRepository featureRepository;
    private final ApiFeatureExecutor apiFeatureExecutor;

    @EventListener(ApplicationReadyEvent.class)
    public void restoreApiFeatures() {
        List<Feature> features = featureRepository.findAllByTypeAndStatus(FeatureType.API, FeatureStatus.EXECUTED);
        log.info("Restoring {} dynamic API endpoints on startup", features.size());
        for (Feature feature : features) {
            try {
                String sql = resolveSqlForRestore(feature);
                apiFeatureExecutor.register(feature, sql);
            } catch (Exception e) {
                log.warn("Failed to restore dynamic API featureId={} error={}", feature.getId(), e.getMessage());
            }
        }
    }

    private String resolveSqlForRestore(Feature apiFeature) {
        if (apiFeature.getLinkedFeatureId() == null) {
            return null;
        }
        return featureRepository.findById(apiFeature.getLinkedFeatureId())
                .map(Feature::getContent)
                .orElse(null);
    }
}