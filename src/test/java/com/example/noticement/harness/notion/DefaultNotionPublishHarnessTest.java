package com.example.noticement.harness.notion;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.DocumentCategory;
import com.example.noticement.domain.document.Evidence;
import com.example.noticement.domain.document.TechDocument;
import com.example.noticement.harness.GuardStatus;
import com.example.noticement.harness.ai.DefaultEvidenceGuard;
import com.example.noticement.harness.ai.EvidenceGuardProperties;
import com.example.noticement.notion.NotionPageMapper;
import com.example.noticement.notion.NotionProperties;
import com.example.noticement.notion.NotionTemplateRenderer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultNotionPublishHarnessTest {

    private final TechDocument document = new TechDocument(
            UUID.randomUUID(), "src", "https://example.com/advisory", "title", "vendor",
            DocumentCategory.JAVA, Instant.now(), Instant.now(), "content", "hash"
    );

    private final DocumentAnalysis validAnalysis = new DocumentAnalysis(
            document.id(), "one line", "summary", List.of(), List.of(), List.of(), List.of(),
            50, 0.9, List.of()
    );

    private DefaultNotionPublishHarness harness(NotionPublishHarnessProperties properties, NotionProperties notionProperties) {
        return new DefaultNotionPublishHarness(
                properties,
                notionProperties,
                new DefaultEvidenceGuard(new EvidenceGuardProperties(true, true)),
                new NotionPageMapper(),
                new NotionTemplateRenderer(),
                new ObjectMapper()
        );
    }

    private NotionPublishHarnessProperties defaultProperties() {
        return new NotionPublishHarnessProperties(List.of("db-1"), 200, 200_000, 90, List.of("AKIA[0-9A-Z]{16}"));
    }

    private NotionProperties notionProperties(String databaseId) {
        return new NotionProperties("https://api.notion.com/v1", "2022-06-28", "key", databaseId, 15000);
    }

    @Test
    void allowsValidDocumentAndAnalysis() {
        var decision = harness(defaultProperties(), notionProperties("db-1")).validate(document, validAnalysis);
        assertEquals(GuardStatus.ALLOW, decision.status());
    }

    @Test
    void blocksWhenSourceUrlMissing() {
        var noUrlDocument = new TechDocument(
                document.id(), document.sourceId(), "", document.title(), document.vendor(),
                document.category(), document.publishedAt(), document.collectedAt(),
                document.normalizedContent(), document.contentHash()
        );
        var decision = harness(defaultProperties(), notionProperties("db-1")).validate(noUrlDocument, validAnalysis);
        assertEquals(GuardStatus.BLOCK, decision.status());
    }

    @Test
    void blocksWhenDatabaseNotAllowlisted() {
        var decision = harness(defaultProperties(), notionProperties("other-db")).validate(document, validAnalysis);
        assertEquals(GuardStatus.BLOCK, decision.status());
    }

    @Test
    void blocksWhenSecurityIssueHasNoEvidence() {
        var analysis = new DocumentAnalysis(
                document.id(), "one line", "summary", List.of(), List.of(),
                List.of("CVE-2024-1234 found"), List.of(), 90, 0.9, List.of()
        );
        var decision = harness(defaultProperties(), notionProperties("db-1")).validate(document, analysis);
        assertEquals(GuardStatus.BLOCK, decision.status());
    }

    @Test
    void blocksWhenSecretPatternDetected() {
        var analysis = new DocumentAnalysis(
                document.id(), "one line", "leaked key AKIAABCDEFGHIJKLMNOP in summary",
                List.of(), List.of(), List.of(), List.of(), 50, 0.9, List.of()
        );
        var decision = harness(defaultProperties(), notionProperties("db-1")).validate(document, analysis);
        assertEquals(GuardStatus.BLOCK, decision.status());
    }

    @Test
    void blocksWhenTooManyBlocksRendered() {
        var tightProperties = new NotionPublishHarnessProperties(List.of("db-1"), 200, 200_000, 0, List.of());
        var decision = harness(tightProperties, notionProperties("db-1")).validate(document, validAnalysis);
        assertEquals(GuardStatus.BLOCK, decision.status());
    }

    @Test
    void blocksWhenPayloadTooLarge() {
        var tightProperties = new NotionPublishHarnessProperties(List.of("db-1"), 200, 10, 90, List.of());
        var decision = harness(tightProperties, notionProperties("db-1")).validate(document, validAnalysis);
        assertEquals(GuardStatus.BLOCK, decision.status());
    }
}
