package com.example.noticement.notion;

import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.TechDocument;
import com.example.noticement.domain.notion.PublishedPage;

public interface NotionPublisher {
    PublishedPage publishDocument(TechDocument document, DocumentAnalysis analysis);
}
