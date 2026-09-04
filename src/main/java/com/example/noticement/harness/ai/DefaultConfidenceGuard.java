package com.example.noticement.harness.ai;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.harness.GuardDecision;
import com.example.noticement.harness.GuardStatus;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@EnableConfigurationProperties(ConfidenceGuardProperties.class)
public class DefaultConfidenceGuard implements ConfidenceGuard {

    private final ConfidenceGuardProperties properties;

    public DefaultConfidenceGuard(ConfidenceGuardProperties properties) {
        this.properties = properties;
    }

    @Override
    public GuardDecision validate(DocumentAnalysis analysis) {
        double confidence = analysis.confidence();

        if (confidence < properties.blockBelow()) {
            return new GuardDecision(GuardStatus.BLOCK, "CONFIDENCE_TOO_LOW",
                    "confidence " + confidence + " below block threshold " + properties.blockBelow(), Map.of());
        }

        if (confidence < properties.reviewBelow()) {
            return new GuardDecision(GuardStatus.REVIEW, "CONFIDENCE_LOW",
                    "confidence " + confidence + " below review threshold " + properties.reviewBelow(), Map.of());
        }

        return new GuardDecision(GuardStatus.ALLOW, "OK", null, Map.of());
    }
}
