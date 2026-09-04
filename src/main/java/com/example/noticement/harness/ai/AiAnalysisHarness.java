package com.example.noticement.harness.ai;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.TechDocument;
import com.example.noticement.harness.GuardDecision;

public interface AiAnalysisHarness {
    GuardDecision validate(TechDocument document, DocumentAnalysis analysis);
}
