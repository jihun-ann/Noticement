package com.example.noticement.mail;

import com.example.noticement.domain.delivery.MailCommand;
import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.TechDocument;
import com.example.noticement.domain.notion.PublishedPage;

import java.util.List;

public interface ImmediateAlertMailBuilder {
    MailCommand build(TechDocument document, DocumentAnalysis analysis, PublishedPage page, String recipientGroup, List<String> recipients);
}
