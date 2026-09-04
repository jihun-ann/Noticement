package com.example.noticement.mail;

import com.example.noticement.domain.delivery.MailCommand;
import com.example.noticement.domain.document.ActionItem;
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

class DefaultImmediateAlertMailBuilderTest {

    private final TechDocument document = new TechDocument(
            UUID.randomUUID(), "src", "https://example.com/advisory", "Critical CVE found", "vendor",
            DocumentCategory.SECURITY, Instant.now(), Instant.now(), "content", "hash"
    );

    private final DocumentAnalysis analysis = new DocumentAnalysis(
            document.id(), "one line summary", "summary", List.of(), List.of(), List.of("CVE-2024-1234"),
            List.of(new ActionItem("upgrade dependency to 1.2.4")), 95, 0.9, List.of()
    );

    private final PublishedPage page = new PublishedPage(document.id(), "page-1", "https://notion.so/page-1", ProcessingStatus.NOTION_PUBLISHED);

    @Test
    void buildsAlertWithSummaryActionItemsAndNotionUrl() {
        var builder = new DefaultImmediateAlertMailBuilder();
        MailCommand command = builder.build(document, analysis, page, "security-team", List.of("secops@example.com"));

        assertEquals("mail:immediate:" + document.id() + ":security-team", command.idempotencyKey());
        assertTrue(command.subject().contains(document.title()));
        assertTrue(command.bodyText().contains(analysis.oneLineSummary()));
        assertTrue(command.bodyText().contains("upgrade dependency to 1.2.4"));
        assertTrue(command.bodyText().contains(page.pageUrl()));
        assertEquals(page.pageUrl(), command.notionPageUrl());
        assertEquals(List.of("secops@example.com"), command.recipients());
    }
}
