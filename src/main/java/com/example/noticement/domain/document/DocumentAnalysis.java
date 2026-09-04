package com.example.noticement.domain.document;

import java.util.List;
import java.util.UUID;

public record DocumentAnalysis(
        UUID documentId,
        String oneLineSummary,
        String summary,
        List<String> keyPoints,
        List<String> breakingChanges,
        List<String> securityIssues,
        List<ActionItem> actionItems,
        int importanceScore,
        double confidence,
        List<Evidence> evidences
) {}
