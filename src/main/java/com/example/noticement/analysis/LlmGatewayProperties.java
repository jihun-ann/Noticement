package com.example.noticement.analysis;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm")
public record LlmGatewayProperties(
        ProviderConfig analysis,
        ProviderConfig weekly,
        long timeoutMs
) {
    public record ProviderConfig(
            String provider,
            String baseUrl,
            String apiKey,
            String model
    ) {}
}
