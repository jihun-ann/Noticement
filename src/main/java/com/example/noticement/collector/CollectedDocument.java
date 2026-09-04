package com.example.noticement.collector;

import java.time.Instant;

public record CollectedDocument(
        String sourceId,
        String sourceUrl,
        String title,
        String rawContent,
        Instant publishedAt
) {}
