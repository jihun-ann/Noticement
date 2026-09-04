package com.example.noticement.harness.ai;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.harness.GuardStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultConfidenceGuardTest {

    private final DefaultConfidenceGuard guard = new DefaultConfidenceGuard(new ConfidenceGuardProperties(0.5, 0.2));

    private DocumentAnalysis analysisWithConfidence(double confidence) {
        return new DocumentAnalysis(
                UUID.randomUUID(), "one line", "summary", List.of(), List.of(), List.of(), List.of(),
                50, confidence, List.of()
        );
    }

    @Test
    void allowsHighConfidence() {
        assertEquals(GuardStatus.ALLOW, guard.validate(analysisWithConfidence(0.9)).status());
    }

    @Test
    void reviewsMidConfidence() {
        assertEquals(GuardStatus.REVIEW, guard.validate(analysisWithConfidence(0.3)).status());
    }

    @Test
    void blocksLowConfidence() {
        assertEquals(GuardStatus.BLOCK, guard.validate(analysisWithConfidence(0.1)).status());
    }
}
