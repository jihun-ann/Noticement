package com.example.noticement.harness.ai;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.DocumentCategory;
import com.example.noticement.domain.document.TechDocument;
import com.example.noticement.harness.GuardStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultAiAnalysisHarnessTest {

    private final DefaultAiAnalysisHarness harness = new DefaultAiAnalysisHarness(
            new DefaultSchemaGuard(new SchemaGuardProperties(2000, 10)),
            new DefaultEvidenceGuard(new EvidenceGuardProperties(true, true)),
            new DefaultConfidenceGuard(new ConfidenceGuardProperties(0.5, 0.2))
    );

    private final TechDocument document = new TechDocument(
            UUID.randomUUID(), "src", "https://example.com", "title", "vendor",
            DocumentCategory.JAVA, Instant.now(), Instant.now(), "content", "hash"
    );

    @Test
    void schemaViolationOutranksLowerSeverityFindings() {
        var analysis = new DocumentAnalysis(
                UUID.randomUUID(), "one line", "summary", List.of(), List.of(), List.of(), List.of(),
                999, 0.1, List.of()
        );

        var decision = harness.validate(document, analysis);
        assertEquals(GuardStatus.BLOCK, decision.status());
    }

    @Test
    void allowsFullyValidAnalysis() {
        var analysis = new DocumentAnalysis(
                UUID.randomUUID(), "one line", "summary", List.of(), List.of(), List.of(), List.of(),
                50, 0.9, List.of()
        );

        var decision = harness.validate(document, analysis);
        assertEquals(GuardStatus.ALLOW, decision.status());
    }
}
