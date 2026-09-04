package com.example.noticement.analysis;

import com.example.noticement.analysis.LlmGatewayProperties.ProviderConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Chat Completions 호환 스펙(response_format=json_object)으로 동작하는
 * 기본 LlmGateway 구현체. base-url을 설정만 바꾸면 동일 스펙을 쓰는 다른 provider도
 * 그대로 태울 수 있어 provider별 클라이언트를 따로 두지 않는다.
 */
@Component
@EnableConfigurationProperties(LlmGatewayProperties.class)
public class OpenAiCompatibleLlmGateway implements LlmGateway {

    private final LlmGatewayProperties properties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleLlmGateway(LlmGatewayProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.webClient = WebClient.builder().build();
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T generate(LlmRequest request, Class<T> responseType) {
        ProviderConfig config = selectProvider(request.purpose());

        Map<String, Object> body = Map.of(
                "model", config.model(),
                "messages", List.of(
                        Map.of("role", "system", "content", request.systemPrompt() + "\n\n" + request.policyPrompt()),
                        Map.of("role", "user", "content", request.untrustedDocument())
                ),
                "response_format", Map.of("type", "json_object")
        );

        ChatCompletionResponse response = webClient.post()
                .uri(config.baseUrl() + "/chat/completions")
                .header("Authorization", "Bearer " + config.apiKey())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(ChatCompletionResponse.class)
                .timeout(Duration.ofMillis(properties.timeoutMs()))
                .block();

        String content = response.choices().get(0).message().content();
        return parse(content, responseType);
    }

    private <T> T parse(String content, Class<T> responseType) {
        try {
            return objectMapper.readValue(content, responseType);
        } catch (JsonProcessingException e) {
            throw new LlmResponseParseException(
                    "failed to parse LLM response as " + responseType.getSimpleName(), e);
        }
    }

    private ProviderConfig selectProvider(LlmPurpose purpose) {
        return switch (purpose) {
            case ANALYSIS -> properties.analysis();
            case WEEKLY -> properties.weekly();
        };
    }

    private record ChatCompletionResponse(List<Choice> choices) {}

    private record Choice(Message message) {}

    private record Message(String content) {}
}
