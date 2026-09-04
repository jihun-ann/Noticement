package com.example.noticement.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notion_publish")
public class NotionPublishRecord {

    @Id
    private UUID id;

    @Column(name = "document_id")
    private UUID documentId;

    @Column(name = "publish_type", nullable = false)
    private String publishType;

    @Column(name = "notion_page_id")
    private String notionPageId;

    @Column(name = "notion_page_url")
    private String notionPageUrl;

    @Column(nullable = false)
    private String status;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected NotionPublishRecord() {
    }

    public NotionPublishRecord(UUID id, UUID documentId, String publishType, String idempotencyKey) {
        this.id = id;
        this.documentId = documentId;
        this.publishType = publishType;
        this.idempotencyKey = idempotencyKey;
        this.retryCount = 0;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public String getPublishType() {
        return publishType;
    }

    public String getNotionPageId() {
        return notionPageId;
    }

    public String getNotionPageUrl() {
        return notionPageUrl;
    }

    public String getStatus() {
        return status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markPublished(String notionPageId, String notionPageUrl) {
        this.notionPageId = notionPageId;
        this.notionPageUrl = notionPageUrl;
        this.status = "NOTION_PUBLISHED";
        this.publishedAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = "NOTION_FAILED";
        this.errorMessage = errorMessage;
        this.retryCount += 1;
    }
}
