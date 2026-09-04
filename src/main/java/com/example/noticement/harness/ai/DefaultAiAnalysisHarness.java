package com.example.noticement.harness.ai;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.TechDocument;
import com.example.noticement.harness.GuardDecision;
import com.example.noticement.harness.GuardStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultAiAnalysisHarness implements AiAnalysisHarness {

    private static final List<GuardStatus> SEVERITY_ORDER = List.of(
            GuardStatus.BLOCK, GuardStatus.REVIEW, GuardStatus.RETRY, GuardStatus.WARN, GuardStatus.ALLOW
    );

    private final SchemaGuard schemaGuard;
    private final EvidenceGuard evidenceGuard;
    private final ConfidenceGuard confidenceGuard;

    public DefaultAiAnalysisHarness(SchemaGuard schemaGuard, EvidenceGuard evidenceGuard, ConfidenceGuard confidenceGuard) {
        this.schemaGuard = schemaGuard;
        this.evidenceGuard = evidenceGuard;
        this.confidenceGuard = confidenceGuard;
    }

    @Override
    public GuardDecision validate(TechDocument document, DocumentAnalysis analysis) {
        List<GuardDecision> decisions = List.of(
                schemaGuard.validate(analysis),
                evidenceGuard.validate(analysis),
                confidenceGuard.validate(analysis)
        );

        for (GuardStatus status : SEVERITY_ORDER) {
            for (GuardDecision decision : decisions) {
                if (decision.status() == status) {
                    return decision;
                }
            }
        }

        return decisions.get(0);
    }
}
