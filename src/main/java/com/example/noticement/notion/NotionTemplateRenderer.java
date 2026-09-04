package com.example.noticement.notion;

import com.example.noticement.domain.document.ActionItem;
import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.Evidence;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * §28 본문 마크다운 템플릿을 Notion block children으로 렌더링한다.
 * DocumentAnalysis에 실제로 값이 있는 섹션만 렌더링하고, 대응하는 필드가
 * 없는 헤딩("개발자 영향", "관련 외부 지표")은 채울 데이터가 없어 생략한다.
 */
@Component
public class NotionTemplateRenderer {

    public List<Map<String, Object>> renderBody(DocumentAnalysis analysis) {
        List<Map<String, Object>> blocks = new ArrayList<>();

        addSection(blocks, "한줄 요약", List.of(analysis.oneLineSummary()));
        addSection(blocks, "중요도", List.of(analysis.importanceScore() + " / 100 (confidence " + analysis.confidence() + ")"));
        addSection(blocks, "핵심 내용", List.of(analysis.summary()));
        addBulletSection(blocks, "주요 변경점", analysis.keyPoints());
        addBulletSection(blocks, "Breaking Change", analysis.breakingChanges());
        addBulletSection(blocks, "Security", analysis.securityIssues());
        addBulletSection(blocks, "권장 대응", analysis.actionItems().stream().map(ActionItem::description).toList());
        addBulletSection(blocks, "원문 / 근거", analysis.evidences().stream()
                .map(e -> e.claim() + " — " + e.sourceUrl())
                .toList());

        return blocks;
    }

    private void addSection(List<Map<String, Object>> blocks, String heading, List<String> paragraphs) {
        if (paragraphs.stream().allMatch(p -> p == null || p.isBlank())) {
            return;
        }
        blocks.add(heading2(heading));
        for (String paragraph : paragraphs) {
            if (paragraph != null && !paragraph.isBlank()) {
                blocks.add(paragraph(paragraph));
            }
        }
    }

    private void addBulletSection(List<Map<String, Object>> blocks, String heading, List<String> items) {
        if (items.isEmpty()) {
            return;
        }
        blocks.add(heading2(heading));
        for (String item : items) {
            blocks.add(bullet(item));
        }
    }

    private Map<String, Object> heading2(String content) {
        return Map.of(
                "object", "block",
                "type", "heading_2",
                "heading_2", Map.of("rich_text", List.of(textFragment(content)))
        );
    }

    private Map<String, Object> paragraph(String content) {
        return Map.of(
                "object", "block",
                "type", "paragraph",
                "paragraph", Map.of("rich_text", List.of(textFragment(content)))
        );
    }

    private Map<String, Object> bullet(String content) {
        return Map.of(
                "object", "block",
                "type", "bulleted_list_item",
                "bulleted_list_item", Map.of("rich_text", List.of(textFragment(content)))
        );
    }

    private Map<String, Object> textFragment(String content) {
        return Map.of("type", "text", "text", Map.of("content", content));
    }
}
