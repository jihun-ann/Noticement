package com.example.noticement.domain.document;

import java.time.Instant;
import java.util.UUID;

public record TechDocument(
        UUID id,
        String sourceId,
        String sourceUrl,
        String title,
        String vendor,
        DocumentCategory category,
        Instant publishedAt,
        Instant collectedAt,
        String normalizedContent,
        String contentHash
) {}
