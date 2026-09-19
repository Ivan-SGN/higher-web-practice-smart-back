package ru.yandex.practicum.smart.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SqlFeatureExecutor {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SqlFeatureExecutor(@Qualifier("generatorJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> execute(String sql, Map<String, Object> parameters) {
        MapSqlParameterSource params = new MapSqlParameterSource(parameters != null ? parameters : Map.of());
        String normalized = sql.trim().toUpperCase();

        if (normalized.startsWith("SELECT")) {
            log.debug("Executing SELECT: {}", sql);
            return jdbcTemplate.queryForList(sql, params);
        } else {
            log.debug("Executing DML/DDL: {}", sql);
            jdbcTemplate.update(sql, params);
            return List.of();
        }
    }
}