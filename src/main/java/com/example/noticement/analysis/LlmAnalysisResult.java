package com.example.noticement.analysis;

import com.example.noticement.domain.document.ActionItem;
import com.example.noticement.domain.document.Evidence;

import java.util.List;

/**
 * LLM이 직접 생성하는 원시 분석 결과. documentId는 포함하지 않는다 - LLM이
 * 스스로 지어낼 수 없는 신뢰 경계 값이므로 항상 호출측이 채워 넣는다.
 */
public record LlmAnalysisResult(
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
