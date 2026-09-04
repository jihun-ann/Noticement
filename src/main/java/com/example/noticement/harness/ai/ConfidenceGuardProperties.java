package com.example.noticement.harness.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "harness.ai.confidence")
public record ConfidenceGuardProperties(
        double reviewBelow,
        double blockBelow
) {}
