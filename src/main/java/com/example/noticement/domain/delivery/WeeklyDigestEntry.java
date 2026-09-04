package com.example.noticement.domain.delivery;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.TechDocument;

public record WeeklyDigestEntry(TechDocument document, DocumentAnalysis analysis) {}
