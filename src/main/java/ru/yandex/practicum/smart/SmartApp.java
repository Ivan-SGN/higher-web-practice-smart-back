package ru.yandex.practicum.smart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.yandex.practicum.smart.config.LlmProperties;

@SpringBootApplication
@EnableConfigurationProperties(LlmProperties.class)
public class SmartApp {

    public static void main(String[] args) {
        SpringApplication.run(SmartApp.class, args);
    }
}
