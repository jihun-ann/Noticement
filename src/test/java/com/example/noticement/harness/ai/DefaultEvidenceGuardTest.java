package com.example.noticement.harness.ai;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.Evidence;
import com.example.noticement.harness.GuardStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultEvidenceGuardTest {

    private final DefaultEvidenceGuard guard = new DefaultEvidenceGuard(new EvidenceGuardProperties(true, true));

    @Test
    void blocksSecurityIssueWithoutEvidence() {
        var analysis = new DocumentAnalysis(
                UUID.randomUUID(), "one line", "summary", List.of(), List.of(),
                List.of("CVE-2024-1234 remote code execution"), List.of(),
                80, 0.9, List.of()
        );
        assertEquals(GuardStatus.BLOCK, guard.validate(analysis).status());
    }

    @Test
    void blocksEvidenceMissingSourceUrl() {
        var analysis = new DocumentAnalysis(
                UUID.randomUUID(), "one line", "summary", List.of(), List.of(), List.of(), List.of(),
                50, 0.9, List.of(new Evidence("claim", "", "hash"))
        );
        assertEquals(GuardStatus.BLOCK, guard.validate(analysis).status());
    }

    @Test
    void allowsSecurityIssueWithEvidence() {
        var analysis = new DocumentAnalysis(
                UUID.randomUUID(), "one line", "summary", List.of(), List.of(),
                List.of("CVE-2024-1234 remote code execution"), List.of(),
                80, 0.9, List.of(new Evidence("claim", "https://example.com", "hash"))
        );
        assertEquals(GuardStatus.ALLOW, guard.validate(analysis).status());
    }
}
