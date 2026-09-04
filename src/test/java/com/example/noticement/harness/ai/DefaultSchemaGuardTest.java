package com.example.noticement.harness.ai;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.Evidence;
import com.example.noticement.harness.GuardStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultSchemaGuardTest {

    private final DefaultSchemaGuard guard = new DefaultSchemaGuard(new SchemaGuardProperties(2000, 10));

    private DocumentAnalysis validAnalysis() {
        return new DocumentAnalysis(
                UUID.randomUUID(),
                "one line",
                "a valid summary",
                List.of("point"),
                List.of(),
                List.of(),
                List.of(),
                50,
                0.9,
                List.of(new Evidence("claim", "https://example.com", "hash"))
        );
    }

    @Test
    void allowsValidAnalysis() {
        assertEquals(GuardStatus.ALLOW, guard.validate(validAnalysis()).status());
    }

    @Test
    void blocksOutOfRangeImportanceScore() {
        var analysis = replaceImportance(validAnalysis(), 150);
        assertEquals(GuardStatus.BLOCK, guard.validate(analysis).status());
    }

    @Test
    void blocksOutOfRangeConfidence() {
        var invalid = new DocumentAnalysis(
                UUID.randomUUID(), "one line", "summary", List.of(), List.of(), List.of(), List.of(),
                50, 1.5, List.of()
        );
        assertEquals(GuardStatus.BLOCK, guard.validate(invalid).status());
    }

    @Test
    void reviewsInvalidCveFormat() {
        var invalid = new DocumentAnalysis(
                UUID.randomUUID(), "one line", "summary", List.of(), List.of(),
                List.of("mentions CVE without a real id"), List.of(),
                50, 0.9, List.of()
        );
        assertEquals(GuardStatus.REVIEW, guard.validate(invalid).status());
    }

    @Test
    void reviewsDuplicateClaims() {
        var invalid = new DocumentAnalysis(
                UUID.randomUUID(), "one line", "summary", List.of(), List.of(), List.of(), List.of(),
                50, 0.9,
                List.of(
                        new Evidence("same claim", "https://example.com/a", "h1"),
                        new Evidence("Same Claim", "https://example.com/b", "h2")
                )
        );
        assertEquals(GuardStatus.REVIEW, guard.validate(invalid).status());
    }

    private DocumentAnalysis replaceImportance(DocumentAnalysis base, int importanceScore) {
        return new DocumentAnalysis(
                base.documentId(), base.oneLineSummary(), base.summary(), base.keyPoints(),
                base.breakingChanges(), base.securityIssues(), base.actionItems(),
                importanceScore, base.confidence(), base.evidences()
        );
    }
}
