package com.example.noticement.analysis;

import com.example.noticement.domain.document.DocumentCategory;
import com.example.noticement.domain.document.Evidence;
import com.example.noticement.domain.document.TechDocument;
import com.example.noticement.harness.GuardDecision;
import com.example.noticement.harness.GuardStatus;
import com.example.noticement.harness.ai.PromptInjectionGuard;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultDocumentAnalyzerTest {

    private final TechDocument document = new TechDocument(
            UUID.randomUUID(), "src", "https://example.com", "title", "vendor",
            DocumentCategory.JAVA, Instant.now(), Instant.now(), "content", "hash"
    );

    private final LlmAnalysisResult llmResult = new LlmAnalysisResult(
            "one line", "summary", List.of(), List.of(), List.of(), List.of(),
            80, 0.9, List.of(new Evidence("claim", "https://example.com", "hash"))
    );

    @Test
    void keepsLlmConfidenceWhenDocumentIsClean() {
        var analyzer = new DefaultDocumentAnalyzer(alwaysAllow(), stubGateway(llmResult));

        var analysis = analyzer.analyze(document);

        assertEquals(document.id(), analysis.documentId());
        assertEquals(0.9, analysis.confidence());
    }

    @Test
    void capsConfidenceWhenPromptInjectionSuspected() {
        var analyzer = new DefaultDocumentAnalyzer(alwaysReview(), stubGateway(llmResult));

        var analysis = analyzer.analyze(document);

        assertTrue(analysis.confidence() <= 0.3);
    }

    private PromptInjectionGuard alwaysAllow() {
        return doc -> new GuardDecision(GuardStatus.ALLOW, "OK", null, Map.of());
    }

    private PromptInjectionGuard alwaysReview() {
        return doc -> new GuardDecision(GuardStatus.REVIEW, "SUSPICIOUS", "suspicious", Map.of());
    }

    private LlmGateway stubGateway(LlmAnalysisResult result) {
        return new LlmGateway() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T generate(LlmRequest request, Class<T> responseType) {
                return (T) result;
            }
        };
    }
}
