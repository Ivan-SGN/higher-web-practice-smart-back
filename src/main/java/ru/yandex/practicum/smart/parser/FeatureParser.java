package ru.yandex.practicum.smart.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.smart.dto.GeneratedFeature;
import ru.yandex.practicum.smart.exception.ValidationException;

@Component
@RequiredArgsConstructor
public class FeatureParser {

    private final ObjectMapper objectMapper;
    private final Parser markdownParser = Parser.builder().build();

    public GeneratedFeature parse(String response) {
        try {
            return objectMapper.readValue(extractJson(response), GeneratedFeature.class);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception exception) {
            throw new ValidationException("Cannot parse generated feature: " + exception.getMessage());
        }
    }

    String extractJson(String response) {
        String stripped = response.replaceAll("(?s)<think>.*?</think>", "").trim();

        String fromCodeBlock = extractFromCodeBlock(stripped);
        if (fromCodeBlock != null) {
            return fromCodeBlock;
        }

        int start = stripped.indexOf('{');
        int end = stripped.lastIndexOf('}');
        if (start == -1 || end == -1 || end < start) {
            throw new ValidationException("No JSON object found in LLM response");
        }
        return stripped.substring(start, end + 1);
    }

    private String extractFromCodeBlock(String text) {
        Node document = markdownParser.parse(text);
        for (Node node : document.getChildren()) {
            if (node instanceof FencedCodeBlock codeBlock) {
                BasedSequence info = codeBlock.getInfo();
                if (info.isNull() || info.isEmpty() || info.toString().equalsIgnoreCase("json")) {
                    String content = codeBlock.getContentChars().toString().trim();
                    if (content.startsWith("{")) {
                        return content;
                    }
                }
            }
        }
        return null;
    }
}