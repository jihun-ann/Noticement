package com.example.noticement.harness.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "harness.ai.evidence")
public record EvidenceGuardProperties(
        boolean requireEvidenceForSecurityIssues,
        boolean requireEvidenceForBreakingChanges
) {}
