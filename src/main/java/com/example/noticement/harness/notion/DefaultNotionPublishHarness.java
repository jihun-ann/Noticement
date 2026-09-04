package com.example.noticement.harness.notion;

import com.example.noticement.domain.document.ActionItem;
import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.Evidence;
import com.example.noticement.domain.document.TechDocument;
import com.example.noticement.harness.GuardDecision;
import com.example.noticement.harness.GuardStatus;
import com.example.noticement.harness.ai.EvidenceGuard;
import com.example.noticement.notion.NotionPageMapper;
import com.example.noticement.notion.NotionProperties;
import com.example.noticement.notion.NotionTemplateRenderer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@EnableConfigurationProperties(NotionPublishHarnessProperties.class)
public class DefaultNotionPublishHarness implements NotionPublishHarness {

    private final NotionPublishHarnessProperties properties;
    private final NotionProperties notionProperties;
    private final EvidenceGuard evidenceGuard;
    private final NotionPageMapper pageMapper;
    private final NotionTemplateRenderer templateRenderer;
    private final ObjectMapper objectMapper;
    private final List<Pattern> secretPatterns;

    public DefaultNotionPublishHarness(
            NotionPublishHarnessProperties properties,
            NotionProperties notionProperties,
            EvidenceGuard evidenceGuard,
            NotionPageMapper pageMapper,
            NotionTemplateRenderer templateRenderer,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.notionProperties = notionProperties;
        this.evidenceGuard = evidenceGuard;
        this.pageMapper = pageMapper;
        this.templateRenderer = templateRenderer;
        this.objectMapper = objectMapper;
        this.secretPatterns = properties.secretPatterns().stream().map(Pattern::compile).toList();
    }

    @Override
    public GuardDecision validate(TechDocument document, DocumentAnalysis analysis) {
        if (document.sourceUrl() == null || document.sourceUrl().isBlank()) {
            return block("SOURCE_URL_MISSING", "document has no sourceUrl");
        }

        GuardDecision evidenceDecision = evidenceGuard.validate(analysis);
        if (evidenceDecision.status() != GuardStatus.ALLOW) {
            return evidenceDecision;
        }

        if (document.title().length() > properties.maxTitleLength()) {
            return block("TITLE_TOO_LONG", "title exceeds max length " + properties.maxTitleLength());
        }

        if (!properties.allowedDatabaseIds().contains(notionProperties.databaseId())) {
            return block("DATABASE_NOT_ALLOWLISTED", "target database is not in the allowlist: " + notionProperties.databaseId());
        }

        String secretMatch = findSecret(document, analysis);
        if (secretMatch != null) {
            return block("SECRET_DETECTED", "possible secret/token pattern detected in publishable content");
        }

        List<Map<String, Object>> blocks = templateRenderer.renderBody(analysis);
        if (blocks.size() > properties.maxBlockCount()) {
            return block("TOO_MANY_BLOCKS", "rendered block count " + blocks.size() + " exceeds max " + properties.maxBlockCount());
        }

        long payloadBytes = estimatePayloadBytes(document, analysis, blocks);
        if (payloadBytes > properties.maxPayloadBytes()) {
            return block("PAYLOAD_TOO_LARGE", "estimated payload size " + payloadBytes + " exceeds max " + properties.maxPayloadBytes());
        }

        return allow();
    }

    // ponytail: regex secret scan only, no entropy analysis - misses custom/internal token formats.
    // upgrade to a real secret-scanning library if false negatives show up in practice.
    private String findSecret(TechDocument document, DocumentAnalysis analysis) {
        String haystack = Stream.of(
                        document.title(),
                        document.vendor(),
                        analysis.oneLineSummary(),
                        analysis.summary(),
                        String.join(" ", analysis.keyPoints()),
                        String.join(" ", analysis.breakingChanges()),
                        String.join(" ", analysis.securityIssues()),
                        analysis.actionItems().stream().map(ActionItem::description).collect(Collectors.joining(" ")),
                        analysis.evidences().stream().map(Evidence::claim).collect(Collectors.joining(" "))
                )
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));

        for (Pattern pattern : secretPatterns) {
            if (pattern.matcher(haystack).find()) {
                return pattern.pattern();
            }
        }
        return null;
    }

    private long estimatePayloadBytes(TechDocument document, DocumentAnalysis analysis, List<Map<String, Object>> blocks) {
        try {
            Map<String, Object> payload = Map.of(
                    "properties", pageMapper.toProperties(document, analysis),
                    "children", blocks
            );
            return objectMapper.writeValueAsBytes(payload).length;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to estimate notion payload size", e);
        }
    }

    private GuardDecision allow() {
        return new GuardDecision(GuardStatus.ALLOW, "OK", null, Map.of());
    }

    private GuardDecision block(String code, String message) {
        return new GuardDecision(GuardStatus.BLOCK, code, message, Map.of());
    }
}
