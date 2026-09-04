package com.example.noticement.mail;

import com.example.noticement.domain.delivery.MailCommand;
import com.example.noticement.domain.delivery.WeeklyDigestEntry;
import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.DocumentCategory;
import com.example.noticement.domain.document.ProcessingStatus;
import com.example.noticement.domain.document.TechDocument;
import com.example.noticement.domain.notion.PublishedPage;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultDigestMailBuilderTest {

    private final TechDocument document = new TechDocument(
            UUID.randomUUID(), "src", "https://example.com", "Spring Boot 3.4 released", "vendor",
            DocumentCategory.JAVA, Instant.now(), Instant.now(), "content", "hash"
    );

    private final DocumentAnalysis analysis = new DocumentAnalysis(
            document.id(), "minor version bump", "summary", List.of(), List.of(), List.of(), List.of(), 40, 0.8, List.of()
    );

    private final PublishedPage weeklyPage = new PublishedPage(null, "weekly-page", "https://notion.so/weekly", ProcessingStatus.NOTION_PUBLISHED);

    @Test
    void buildsWeeklyDigestWithEntriesAndNotionUrl() {
        var builder = new DefaultDigestMailBuilder();
        var entries = List.of(new WeeklyDigestEntry(document, analysis));

        MailCommand command = builder.build("2026-W36", entries, weeklyPage, "weekly-default", List.of("team@example.com"));

        assertEquals("mail:weekly:2026-W36:weekly-default:v1", command.idempotencyKey());
        assertTrue(command.subject().contains("2026-W36"));
        assertTrue(command.bodyText().contains(document.title()));
        assertTrue(command.bodyText().contains(analysis.oneLineSummary()));
        assertTrue(command.bodyText().contains(weeklyPage.pageUrl()));
        assertEquals(weeklyPage.pageUrl(), command.notionPageUrl());
    }

    @Test
    void buildsEmptyDigestWhenNoEntries() {
        var builder = new DefaultDigestMailBuilder();
        MailCommand command = builder.build("2026-W36", List.of(), weeklyPage, "weekly-default", List.of("team@example.com"));

        assertTrue(command.bodyText().contains("no items this week"));
    }
}
