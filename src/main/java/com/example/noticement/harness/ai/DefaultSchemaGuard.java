package com.example.noticement.harness.ai;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.Evidence;
import com.example.noticement.harness.GuardDecision;
import com.example.noticement.harness.GuardStatus;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@EnableConfigurationProperties(SchemaGuardProperties.class)
public class DefaultSchemaGuard implements SchemaGuard {

    private static final Pattern CVE_PATTERN = Pattern.compile("CVE-\\d{4}-\\d{4,7}", Pattern.CASE_INSENSITIVE);

    private final SchemaGuardProperties properties;

    public DefaultSchemaGuard(SchemaGuardProperties properties) {
        this.properties = properties;
    }

    @Override
    public GuardDecision validate(DocumentAnalysis analysis) {
        if (analysis.importanceScore() < 0 || analysis.importanceScore() > 100) {
            return block("IMPORTANCE_SCORE_OUT_OF_RANGE",
                    "importanceScore must be between 0 and 100: " + analysis.importanceScore());
        }

        if (analysis.confidence() < 0.0 || analysis.confidence() > 1.0) {
            return block("CONFIDENCE_OUT_OF_RANGE",
                    "confidence must be between 0 and 1: " + analysis.confidence());
        }

        if (analysis.oneLineSummary() == null || analysis.oneLineSummary().isBlank()
                || analysis.summary() == null || analysis.summary().isBlank()) {
            return block("SUMMARY_MISSING", "oneLineSummary and summary must not be blank");
        }

        if (analysis.summary().length() > properties.maxSummaryLength()) {
            return block("SUMMARY_TOO_LONG",
                    "summary exceeds max length " + properties.maxSummaryLength());
        }

        if (analysis.actionItems().size() > properties.maxActionItems()) {
            return block("TOO_MANY_ACTION_ITEMS",
                    "actionItems exceeds max count " + properties.maxActionItems());
        }

        for (String issue : analysis.securityIssues()) {
            if (issue.toUpperCase().contains("CVE") && !CVE_PATTERN.matcher(issue).find()) {
                return new GuardDecision(GuardStatus.REVIEW, "INVALID_CVE_FORMAT",
                        "security issue mentions CVE but does not match CVE-YYYY-NNNN format: " + issue, Map.of());
            }
        }

        long distinctClaims = analysis.evidences().stream()
                .map(Evidence::claim)
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet())
                .size();
        if (distinctClaims < analysis.evidences().size()) {
            return new GuardDecision(GuardStatus.REVIEW, "DUPLICATE_CLAIM",
                    "evidences contain duplicate claims", Map.of());
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
