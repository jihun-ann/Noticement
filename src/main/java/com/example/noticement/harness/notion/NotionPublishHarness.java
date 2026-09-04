package com.example.noticement.harness.notion;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.TechDocument;
import com.example.noticement.harness.GuardDecision;

public interface NotionPublishHarness {
    GuardDecision validate(TechDocument document, DocumentAnalysis analysis);
}
