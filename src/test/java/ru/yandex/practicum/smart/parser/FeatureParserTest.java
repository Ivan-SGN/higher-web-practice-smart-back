package ru.yandex.practicum.smart.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.smart.dto.GeneratedFeature;
import ru.yandex.practicum.smart.exception.ValidationException;
import ru.yandex.practicum.smart.model.enums.FeatureType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeatureParserTest {

    private FeatureParser featureParser;

    @BeforeEach
    void setUp() {
        featureParser = new FeatureParser(new ObjectMapper());
    }

    @Test
    void parseValidJsonTest() {
        String json = validJson();

        GeneratedFeature result = featureParser.parse(json);

        assertThat(result.type()).isEqualTo(FeatureType.SQL);
        assertThat(result.description()).isEqualTo("Creates users table");
        assertThat(result.code()).isEqualTo("CREATE TABLE users (id BIGSERIAL PRIMARY KEY)");
        assertThat(result.parameters()).isEmpty();
        assertThat(result.results()).isEmpty();
    }

    @Test
    void parseWithThinkTagsTest() {
        String response = "<think>Let me think about this...</think>\n" + validJson();

        GeneratedFeature result = featureParser.parse(response);

        assertThat(result.type()).isEqualTo(FeatureType.SQL);
        assertThat(result.code()).isEqualTo("CREATE TABLE users (id BIGSERIAL PRIMARY KEY)");
    }

    @Test
    void parseJsonInMarkdownFenceTest() {
        String response = "Here is the SQL:\n```json\n" + validJson() + "\n```";

        GeneratedFeature result = featureParser.parse(response);

        assertThat(result.type()).isEqualTo(FeatureType.SQL);
    }

    @Test
    void parseJsonInUnlabeledMarkdownFenceTest() {
        String response = "```\n" + validJson() + "\n```";

        GeneratedFeature result = featureParser.parse(response);

        assertThat(result.type()).isEqualTo(FeatureType.SQL);
        assertThat(result.code()).isEqualTo("CREATE TABLE users (id BIGSERIAL PRIMARY KEY)");
    }

    @Test
    void extractJsonPrefersCodeBlockOverBareJsonTest() {
        String innerJson = validJson().trim();
        String outerJson = "{\"decoy\": true}";
        String response = outerJson + "\n```json\n" + innerJson + "\n```";

        String extracted = featureParser.extractJson(response);

        assertThat(extracted).isEqualTo(innerJson);
    }

    @Test
    void parseNoJsonFoundTest() {
        String response = "I cannot generate SQL for this request.";

        assertThatThrownBy(() -> featureParser.parse(response))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("No JSON object found");
    }

    @Test
    void parseWrongSchemaTest() {
        String response = "{\"foo\": \"bar\"}";

        assertThatThrownBy(() -> featureParser.parse(response))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Cannot parse generated feature");
    }

    private String validJson() {
        return """
                {
                  "type": "SQL",
                  "description": "Creates users table",
                  "code": "CREATE TABLE users (id BIGSERIAL PRIMARY KEY)",
                  "parameters": [],
                  "results": []
                }
                """;
    }
}