package com.example.noticement.analysis;

public record LlmRequest(
        LlmPurpose purpose,
        String systemPrompt,
        String policyPrompt,
        String untrustedDocument
) {}
