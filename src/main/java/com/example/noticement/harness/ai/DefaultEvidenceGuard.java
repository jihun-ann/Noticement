package com.example.noticement.harness.ai;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.Evidence;
import com.example.noticement.harness.GuardDecision;
import com.example.noticement.harness.GuardStatus;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@EnableConfigurationProperties(EvidenceGuardProperties.class)
public class DefaultEvidenceGuard implements EvidenceGuard {

    private final EvidenceGuardProperties properties;

    public DefaultEvidenceGuard(EvidenceGuardProperties properties) {
        this.properties = properties;
    }

    @Override
    public GuardDecision validate(DocumentAnalysis analysis) {
        for (Evidence evidence : analysis.evidences()) {
            if (evidence.sourceUrl() == null || evidence.sourceUrl().isBlank()) {
                return block("EVIDENCE_MISSING_SOURCE_URL",
                        "evidence claim has no sourceUrl: " + evidence.claim());
            }
        }

        boolean hasCriticalClaims = (properties.requireEvidenceForSecurityIssues() && !analysis.securityIssues().isEmpty())
                || (properties.requireEvidenceForBreakingChanges() && !analysis.breakingChanges().isEmpty());

        if (hasCriticalClaims && analysis.evidences().isEmpty()) {
            return block("MISSING_EVIDENCE_FOR_CRITICAL_CLAIM",
                    "securityIssues/breakingChanges present but no evidence provided");
        }

        return allow();
    }

    private GuardDecision allow() {
        return new GuardDecision(GuardStatus.ALLOW, "OK", null, Map.of());
    }

    private GuardDecision block(String code, String message) {
        return new GuardDecision(GuardStatus.BLOCK, code, message, Map.of());
    }
}
