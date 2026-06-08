package ru.yandex.practicum.smart.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class GeneratorDataSourceConfig {

    @Bean
    @ConfigurationProperties("generator.datasource")
    public DataSource generatorDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public NamedParameterJdbcTemplate generatorJdbcTemplate(DataSource generatorDataSource) {
        return new NamedParameterJdbcTemplate(generatorDataSource);
    }
}