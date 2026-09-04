package com.example.noticement.domain.notion;

import com.example.noticement.domain.document.ProcessingStatus;

import java.util.UUID;

public record PublishedPage(
        UUID documentId,
        String pageId,
        String pageUrl,
        ProcessingStatus status
) {}
