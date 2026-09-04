package com.example.noticement.notion;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notion")
public record NotionProperties(
        String apiBaseUrl,
        String apiVersion,
        String apiKey,
        String databaseId,
        long timeoutMs
) {}
