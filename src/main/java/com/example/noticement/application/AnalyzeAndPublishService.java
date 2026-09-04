package com.example.noticement.application;

import com.example.noticement.analysis.DocumentAnalyzer;
import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.TechDocument;
import com.example.noticement.domain.notion.PublishedPage;
import com.example.noticement.harness.GuardDecision;
import com.example.noticement.harness.GuardStatus;
import com.example.noticement.harness.ai.AiAnalysisHarness;
import com.example.noticement.harness.notion.NotionPublishHarness;
import com.example.noticement.notion.NotionPublisher;
import org.springframework.stereotype.Service;

/**
 * AGENT.md §47: 분석 -> AI Harness -> Notion Harness -> Notion Publish 순서로만 실행한다.
 * 어떤 단계도 건너뛰고 바로 다음 단계로 넘어가지 않는다(§49 Harness 실행 순서).
 */
@Service
public class AnalyzeAndPublishService {

    private final DocumentAnalyzer analyzer;
    private final AiAnalysisHarness analysisHarness;
    private final NotionPublishHarness notionHarness;
    private final NotionPublisher notionPublisher;

    public AnalyzeAndPublishService(
            DocumentAnalyzer analyzer,
            AiAnalysisHarness analysisHarness,
            NotionPublishHarness notionHarness,
            NotionPublisher notionPublisher
    ) {
        this.analyzer = analyzer;
        this.analysisHarness = analysisHarness;
        this.notionHarness = notionHarness;
        this.notionPublisher = notionPublisher;
    }

    public PublishedPage execute(TechDocument document) {
        DocumentAnalysis analysis = analyzer.analyze(document);

        GuardDecision aiDecision = analysisHarness.validate(document, analysis);
        if (aiDecision.status() == GuardStatus.BLOCK) {
            throw new PipelineRejectedException(aiDecision.code());
        }

        GuardDecision notionDecision = notionHarness.validate(document, analysis);
        if (notionDecision.status() != GuardStatus.ALLOW) {
            throw new PipelineRejectedException(notionDecision.code());
        }

        return notionPublisher.publishDocument(document, analysis);
    }
}
