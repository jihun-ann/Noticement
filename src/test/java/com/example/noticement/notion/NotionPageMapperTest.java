package com.example.noticement.notion;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.DocumentCategory;
import com.example.noticement.domain.document.TechDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotionPageMapperTest {

    private final NotionPageMapper mapper = new NotionPageMapper();

    @Test
    @SuppressWarnings("unchecked")
    void mapsCoreFieldsAndExtractsDistinctCves() {
        TechDocument document = new TechDocument(
                UUID.randomUUID(), "src", "https://example.com/advisory", "Spring Boot 3.3.5 release",
                "VMware", DocumentCategory.SECURITY, Instant.now(), Instant.now(), "content", "hash123"
        );
        DocumentAnalysis analysis = new DocumentAnalysis(
                document.id(), "one line", "summary", List.of(), List.of(),
                List.of("Fixed CVE-2024-1234 and also mentions CVE-2024-1234 again", "New CVE-2023-5678 disclosed"),
                List.of(), 90, 0.95, List.of()
        );

        Map<String, Object> properties = mapper.toProperties(document, analysis);

        Map<String, Object> title = (Map<String, Object>) properties.get("Title");
        List<Map<String, Object>> titleTexts = (List<Map<String, Object>>) title.get("title");
        Map<String, Object> titleText = (Map<String, Object>) titleTexts.get(0).get("text");
        assertEquals("Spring Boot 3.3.5 release", titleText.get("content"));

        Map<String, Object> cve = (Map<String, Object>) properties.get("CVE");
        List<Map<String, Object>> cveTexts = (List<Map<String, Object>>) cve.get("rich_text");
        String cveContent = (String) ((Map<String, Object>) cveTexts.get(0).get("text")).get("content");
        assertTrue(cveContent.contains("CVE-2024-1234"));
        assertTrue(cveContent.contains("CVE-2023-5678"));
        assertEquals(2, cveContent.split(", ").length);
    }
}
