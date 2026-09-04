package com.example.noticement.harness.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "harness.ai.prompt-injection")
public record PromptInjectionGuardProperties(List<String> suspiciousPhrases) {}
