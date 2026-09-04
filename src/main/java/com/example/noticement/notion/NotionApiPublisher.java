package com.example.noticement.notion;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.ProcessingStatus;
import com.example.noticement.domain.document.TechDocument;
import com.example.noticement.domain.notion.PublishedPage;
import com.example.noticement.persistence.entity.NotionPublishRecord;
import com.example.noticement.persistence.repository.NotionPublishRecordRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * §29 NotionPublisher의 기본 구현체. AGENT.md는 기본값을 NotionMcpPublisher(Notion MCP
 * 경유)로 명시하지만, 프로젝트에 MCP 클라이언트 SDK/서버 엔드포인트가 아직 없어 §29의
 * "향후 교체" 옵션 중 하나인 Notion REST API(NotionApiPublisher) 직접 호출로 구현한다.
 * MCP 어댑터로 교체가 필요해지면 이 클래스만 같은 NotionPublisher 인터페이스로 대체하면 된다.
 */
@Component
@EnableConfigurationProperties(NotionProperties.class)
public class NotionApiPublisher implements NotionPublisher {

    private static final String IDEMPOTENCY_VERSION = "v1"; // ponytail: DocumentAnalysis에 버전 필드가 생기면 고정값 대신 실제 analysisVersion 사용

    private final NotionProperties properties;
    private final NotionPageMapper pageMapper;
    private final NotionTemplateRenderer templateRenderer;
    private final NotionPublishRecordRepository repository;
    private final WebClient webClient;

    public NotionApiPublisher(
            NotionProperties properties,
            NotionPageMapper pageMapper,
            NotionTemplateRenderer templateRenderer,
            NotionPublishRecordRepository repository
    ) {
        this.properties = properties;
        this.pageMapper = pageMapper;
        this.templateRenderer = templateRenderer;
        this.repository = repository;
        this.webClient = WebClient.builder().build();
    }

    @Override
    @Transactional
    public PublishedPage publishDocument(TechDocument document, DocumentAnalysis analysis) {
        String idempotencyKey = "notion:" + document.id() + ":" + IDEMPOTENCY_VERSION;

        var existing = repository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent() && "NOTION_PUBLISHED".equals(existing.get().getStatus())) {
            NotionPublishRecord published = existing.get();
            return new PublishedPage(document.id(), published.getNotionPageId(), published.getNotionPageUrl(), ProcessingStatus.NOTION_PUBLISHED);
        }

        NotionPublishRecord record = existing.orElseGet(() ->
                new NotionPublishRecord(UUID.randomUUID(), document.id(), "DOCUMENT", idempotencyKey));

        Map<String, Object> body = Map.of(
                "parent", Map.of("database_id", properties.databaseId()),
                "properties", pageMapper.toProperties(document, analysis),
                "children", templateRenderer.renderBody(analysis)
        );

        try {
            CreatePageResponse response = webClient.post()
                    .uri(properties.apiBaseUrl() + "/pages")
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .header("Notion-Version", properties.apiVersion())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(CreatePageResponse.class)
                    .timeout(Duration.ofMillis(properties.timeoutMs()))
                    .block();

            record.markPublished(response.id(), response.url());
            repository.save(record);

            return new PublishedPage(document.id(), response.id(), response.url(), ProcessingStatus.NOTION_PUBLISHED);
        } catch (Exception e) {
            record.markFailed(e.getMessage());
            repository.save(record);
            throw new NotionPublishException("failed to publish document " + document.id() + " to Notion", e);
        }
    }

    private record CreatePageResponse(String id, String url) {}
}
