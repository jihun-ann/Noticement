package com.example.noticement.collector;

import com.example.noticement.domain.document.DocumentCategory;

public record SourceConfig(
        String id,
        String sourceType,
        String vendor,
        DocumentCategory category,
        String endpoint,
        boolean enabled,
        String schedule
) {}
