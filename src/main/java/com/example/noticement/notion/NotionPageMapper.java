package com.example.noticement.notion;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.TechDocument;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * §28 Database Property 목록을 Notion API "properties" 페이로드로 변환한다.
 * 대상 Notion Database의 실제 property 이름/타입과 정확히 일치해야 하므로,
 * 운영 전 실제 워크스페이스 스키마에 맞춰 이름을 검증해야 한다.
 */
@Component
public class NotionPageMapper {

    private static final Pattern CVE_PATTERN = Pattern.compile("CVE-\\d{4}-\\d{4,7}", Pattern.CASE_INSENSITIVE);

    public Map<String, Object> toProperties(TechDocument document, DocumentAnalysis analysis) {
        Map<String, Object> properties = new LinkedHashMap<>();

        properties.put("Title", titleProperty(document.title()));
        properties.put("Category", selectProperty(document.category().name()));
        properties.put("Vendor", richTextProperty(document.vendor() == null ? "" : document.vendor()));
        properties.put("Importance", numberProperty(analysis.importanceScore()));
        properties.put("Source URL", urlProperty(document.sourceUrl()));
        properties.put("CVE", richTextProperty(extractCves(analysis)));
        properties.put("Status", selectProperty("PUBLISHED"));
        properties.put("Tags", multiSelectProperty(document.category().name()));
        properties.put("Content Hash", richTextProperty(document.contentHash()));

        if (document.publishedAt() != null) {
            properties.put("Published At", dateProperty(document.publishedAt().toString()));
        }
        properties.put("Collected At", dateProperty(document.collectedAt().toString()));

        return properties;
    }

    private String extractCves(DocumentAnalysis analysis) {
        return analysis.securityIssues().stream()
                .map(CVE_PATTERN::matcher)
                .flatMap(Matcher::results)
                .map(m -> m.group())
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private Map<String, Object> titleProperty(String content) {
        return Map.of("title", java.util.List.of(textFragment(content)));
    }

    private Map<String, Object> richTextProperty(String content) {
        return Map.of("rich_text", java.util.List.of(textFragment(content)));
    }

    private Map<String, Object> selectProperty(String name) {
        return Map.of("select", Map.of("name", name));
    }

    private Map<String, Object> multiSelectProperty(String name) {
        return Map.of("multi_select", java.util.List.of(Map.of("name", name)));
    }

    private Map<String, Object> numberProperty(int value) {
        return Map.of("number", value);
    }

    private Map<String, Object> urlProperty(String url) {
        return Map.of("url", url);
    }

    private Map<String, Object> dateProperty(String isoInstant) {
        return Map.of("date", Map.of("start", isoInstant));
    }

    private Map<String, Object> textFragment(String content) {
        return Map.of("text", Map.of("content", content == null ? "" : content));
    }
}
