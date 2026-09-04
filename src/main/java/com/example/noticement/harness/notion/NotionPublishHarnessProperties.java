package com.example.noticement.harness.notion;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "harness.notion")
public record NotionPublishHarnessProperties(
        List<String> allowedDatabaseIds,
        int maxTitleLength,
        long maxPayloadBytes,
        int maxBlockCount,
        List<String> secretPatterns
) {}
