package ru.yandex.practicum.smart.executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.PathContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;
import ru.yandex.practicum.smart.exception.ValidationException;
import ru.yandex.practicum.smart.model.Feature;
import ru.yandex.practicum.smart.model.enums.FeatureStatus;
import ru.yandex.practicum.smart.model.enums.FeatureType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApiFeatureExecutorTest {

    private RequestMappingHandlerMapping handlerMapping;
    private ApiFeatureExecutor apiFeatureExecutor;

    @BeforeEach
    void setUp() {
        handlerMapping = mock(RequestMappingHandlerMapping.class);
        when(handlerMapping.getPatternParser()).thenReturn(new PathPatternParser());
        SqlFeatureExecutor sqlFeatureExecutor = mock(SqlFeatureExecutor.class);
        apiFeatureExecutor = new ApiFeatureExecutor(handlerMapping, sqlFeatureExecutor);
    }

    @Test
    void registerGetEndpointTest() {
        Feature feature = createApiFeature("GET /api/users");

        apiFeatureExecutor.register(feature, null);

        verify(handlerMapping).registerMapping(any(), any(), any());
    }

    @Test
    void registerGetEndpointWithSqlTest() {
        Feature feature = createApiFeature("GET /api/users");

        apiFeatureExecutor.register(feature, "SELECT id, login FROM users");

        verify(handlerMapping).registerMapping(any(), any(), any());
    }

    @Test
    void registerPostEndpointTest() {
        Feature feature = createApiFeature("POST /api/orders");

        apiFeatureExecutor.register(feature, null);

        verify(handlerMapping).registerMapping(any(), any(), any());
    }

    @Test
    void registerInvalidContentNoSpaceTest() {
        Feature feature = createApiFeature("GET");

        assertThatThrownBy(() -> apiFeatureExecutor.register(feature, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid API feature content");
    }

    @Test
    void registerInvalidContentNullTest() {
        Feature feature = createApiFeature(null);

        assertThatThrownBy(() -> apiFeatureExecutor.register(feature, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid API feature content");
    }

    private Feature createApiFeature(String content) {
        Feature feature = new Feature();
        feature.setId(1L);
        feature.setType(FeatureType.API);
        feature.setContent(content);
        feature.setStatus(FeatureStatus.DRAFT);
        feature.setResults(List.of("id", "login"));
        return feature;
    }
}