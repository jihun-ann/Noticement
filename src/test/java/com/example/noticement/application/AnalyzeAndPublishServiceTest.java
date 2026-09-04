package com.example.noticement.application;

import com.example.noticement.analysis.DocumentAnalyzer;
import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.DocumentCategory;
import com.example.noticement.domain.document.ProcessingStatus;
import com.example.noticement.domain.document.TechDocument;
import com.example.noticement.domain.notion.PublishedPage;
import com.example.noticement.harness.GuardDecision;
import com.example.noticement.harness.GuardStatus;
import com.example.noticement.harness.ai.AiAnalysisHarness;
import com.example.noticement.harness.notion.NotionPublishHarness;
import com.example.noticement.notion.NotionPublisher;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnalyzeAndPublishServiceTest {

    private final TechDocument document = new TechDocument(
            UUID.randomUUID(), "openai-release", "https://openai.com/blog/x", "title", "OPENAI",
            DocumentCategory.AI, Instant.now(), Instant.now(), "content", "hash"
    );

    private final DocumentAnalysis analysis = new DocumentAnalysis(
            document.id(), "one line", "summary", List.of(), List.of(), List.of(), List.of(), 50, 0.9, List.of()
    );

    private final DocumentAnalyzer analyzer = mock(DocumentAnalyzer.class);
    private final AiAnalysisHarness analysisHarness = mock(AiAnalysisHarness.class);
    private final NotionPublishHarness notionHarness = mock(NotionPublishHarness.class);
    private final NotionPublisher notionPublisher = mock(NotionPublisher.class);
    private final AnalyzeAndPublishService service =
            new AnalyzeAndPublishService(analyzer, analysisHarness, notionHarness, notionPublisher);

    @Test
    void publishesWhenAllGuardsAllow() {
        when(analyzer.analyze(document)).thenReturn(analysis);
        when(analysisHarness.validate(document, analysis)).thenReturn(new GuardDecision(GuardStatus.ALLOW, "OK", "", Map.of()));
        when(notionHarness.validate(document, analysis)).thenReturn(new GuardDecision(GuardStatus.ALLOW, "OK", "", Map.of()));
        PublishedPage page = new PublishedPage(document.id(), "page-1", "https://notion.so/page-1", ProcessingStatus.NOTION_PUBLISHED);
        when(notionPublisher.publishDocument(document, analysis)).thenReturn(page);

        assertEquals(page, service.execute(document));
    }

    @Test
    void blocksBeforeNotionWhenAiHarnessBlocks() {
        when(analyzer.analyze(document)).thenReturn(analysis);
        when(analysisHarness.validate(document, analysis))
                .thenReturn(new GuardDecision(GuardStatus.BLOCK, "LOW_EVIDENCE_COVERAGE", "", Map.of()));

        PipelineRejectedException e = assertThrows(PipelineRejectedException.class, () -> service.execute(document));
        assertEquals("LOW_EVIDENCE_COVERAGE", e.getMessage());
        verify(notionHarness, never()).validate(any(), any());
        verify(notionPublisher, never()).publishDocument(any(), any());
    }

    @Test
    void blocksWhenNotionHarnessDoesNotAllow() {
        when(analyzer.analyze(document)).thenReturn(analysis);
        when(analysisHarness.validate(document, analysis)).thenReturn(new GuardDecision(GuardStatus.ALLOW, "OK", "", Map.of()));
        when(notionHarness.validate(document, analysis))
                .thenReturn(new GuardDecision(GuardStatus.REVIEW, "SECRET_DETECTED", "", Map.of()));

        PipelineRejectedException e = assertThrows(PipelineRejectedException.class, () -> service.execute(document));
        assertEquals("SECRET_DETECTED", e.getMessage());
        verify(notionPublisher, never()).publishDocument(any(), any());
    }
}
