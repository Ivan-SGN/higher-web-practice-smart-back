package ru.yandex.practicum.smart.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import ru.yandex.practicum.smart.exception.ValidationException;
import ru.yandex.practicum.smart.model.Feature;

@Slf4j
@Component
public class ApiFeatureExecutor {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final SqlFeatureExecutor sqlFeatureExecutor;

    public ApiFeatureExecutor(RequestMappingHandlerMapping requestMappingHandlerMapping,
                               SqlFeatureExecutor sqlFeatureExecutor) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.sqlFeatureExecutor = sqlFeatureExecutor;
    }

    public void register(Feature feature, String sql) {
        String[] parts = parseContent(feature.getContent());
        String method = parts[0].toUpperCase();
        String url = parts[1];

        RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();
        options.setPatternParser(requestMappingHandlerMapping.getPatternParser());

        RequestMappingInfo mappingInfo = RequestMappingInfo
                .paths(url)
                .methods(RequestMethod.valueOf(method))
                .options(options)
                .build();

        DynamicEndpointHandler handler = new DynamicEndpointHandler(feature.getId(), feature.getResults(),
                sql != null ? sqlFeatureExecutor : null, sql);
        requestMappingHandlerMapping.registerMapping(mappingInfo, handler, DynamicEndpointHandler.HANDLER_METHOD);
        log.info("Registered dynamic API featureId={} {} {} linkedSql={}", feature.getId(), method, url, sql != null);
    }

    private String[] parseContent(String content) {
        if (content == null || !content.contains(" ")) {
            throw new ValidationException("Invalid API feature content, expected: 'METHOD /url', got: " + content);
        }
        String[] parts = content.trim().split(" ", 2);
        if (parts.length != 2 || parts[1].isBlank()) {
            throw new ValidationException("Invalid API feature content, expected: 'METHOD /url', got: " + content);
        }
        return parts;
    }
}
