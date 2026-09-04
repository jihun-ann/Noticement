package com.example.noticement.analysis;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.TechDocument;

public interface DocumentAnalyzer {
    DocumentAnalysis analyze(TechDocument document);
}
