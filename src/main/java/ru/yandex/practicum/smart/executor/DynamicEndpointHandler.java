package ru.yandex.practicum.smart.executor;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DynamicEndpointHandler {

    public static final Method HANDLER_METHOD;

    static {
        try {
            HANDLER_METHOD = DynamicEndpointHandler.class.getMethod("handle", HttpServletRequest.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final Long featureId;
    private final List<String> results;
    private final SqlFeatureExecutor sqlFeatureExecutor;
    private final String sql;

    public DynamicEndpointHandler(Long featureId, List<String> results,
                                   SqlFeatureExecutor sqlFeatureExecutor, String sql) {
        this.featureId = featureId;
        this.results = results != null ? results : List.of();
        this.sqlFeatureExecutor = sqlFeatureExecutor;
        this.sql = sql;
    }

    public ResponseEntity<Object> handle(HttpServletRequest request) {
        log.info("Dynamic API called featureId={} method={} uri={} params={}",
                featureId, request.getMethod(), request.getRequestURI(), request.getParameterMap().keySet());

        if (sql != null && sqlFeatureExecutor != null) {
            Map<String, Object> params = extractParams(request);
            List<Map<String, Object>> data = sqlFeatureExecutor.execute(sql, params);
            return ResponseEntity.ok(data);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("featureId", featureId);
        response.put("fields", results);
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> extractParams(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) ->
                params.put(key, values.length == 1 ? values[0] : values));
        return params;
    }
}