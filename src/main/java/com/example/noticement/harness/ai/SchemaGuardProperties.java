package com.example.noticement.harness.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "harness.ai.schema")
public record SchemaGuardProperties(
        int maxSummaryLength,
        int maxActionItems
) {}
