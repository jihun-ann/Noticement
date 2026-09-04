package com.example.noticement.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mail_delivery")
public class MailDeliveryRecord {

    @Id
    private UUID id;

    @Column(name = "delivery_type", nullable = false)
    private String deliveryType;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String recipient;

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

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MailDeliveryRecord() {
    }

    public MailDeliveryRecord(UUID id, String deliveryType, String subject, String recipient, String notionPageUrl, String idempotencyKey) {
        this.id = id;
        this.deliveryType = deliveryType;
        this.subject = subject;
        this.recipient = recipient;
        this.notionPageUrl = notionPageUrl;
        this.idempotencyKey = idempotencyKey;
        this.status = "PENDING";
        this.retryCount = 0;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public String getSubject() {
        return subject;
    }

    public String getRecipient() {
        return recipient;
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

    public Instant getSentAt() {
        return sentAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markSent(String status) {
        this.status = status;
        this.sentAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = "MAIL_FAILED";
        this.errorMessage = errorMessage;
        this.retryCount += 1;
    }
}
