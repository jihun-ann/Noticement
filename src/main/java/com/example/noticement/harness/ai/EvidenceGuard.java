package com.example.noticement.harness.ai;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.harness.GuardDecision;

public interface EvidenceGuard {
    GuardDecision validate(DocumentAnalysis analysis);
}
