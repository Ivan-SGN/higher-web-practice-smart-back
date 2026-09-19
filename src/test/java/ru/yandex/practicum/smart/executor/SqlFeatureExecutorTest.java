package ru.yandex.practicum.smart.executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqlFeatureExecutorTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private SqlFeatureExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new SqlFeatureExecutor(jdbcTemplate);
    }

    @Test
    void executeSelectQueryTest() {
        String sql = "SELECT id, name FROM users";
        when(jdbcTemplate.queryForList(eq(sql), any(MapSqlParameterSource.class)))
                .thenReturn(List.of(Map.of("id", 1L, "name", "Alice")));

        List<Map<String, Object>> result = executor.execute(sql, Map.of());

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsEntry("name", "Alice");
        verify(jdbcTemplate).queryForList(eq(sql), any(MapSqlParameterSource.class));
    }

    @Test
    void executeDdlQueryTest() {
        String sql = "CREATE TABLE users (id BIGSERIAL PRIMARY KEY)";

        List<Map<String, Object>> result = executor.execute(sql, Map.of());

        assertThat(result).isEmpty();
        verify(jdbcTemplate).update(eq(sql), any(MapSqlParameterSource.class));
    }

    @Test
    void executeWithParametersTest() {
        String sql = "SELECT id FROM users WHERE id = :id";
        when(jdbcTemplate.queryForList(eq(sql), any(MapSqlParameterSource.class))).thenReturn(List.of());
        ArgumentCaptor<MapSqlParameterSource> captor = ArgumentCaptor.forClass(MapSqlParameterSource.class);

        executor.execute(sql, Map.of("id", 42L));

        verify(jdbcTemplate).queryForList(eq(sql), captor.capture());
        assertThat(captor.getValue().getValue("id")).isEqualTo(42L);
    }

    @Test
    void executeWithNullParametersTest() {
        String sql = "CREATE TABLE test (id INT)";

        executor.execute(sql, null);

        verify(jdbcTemplate).update(eq(sql), any(MapSqlParameterSource.class));
    }
}