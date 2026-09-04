package com.example.noticement.mail;

import com.example.noticement.domain.delivery.MailCommand;
import com.example.noticement.domain.document.ActionItem;
import com.example.noticement.domain.document.DocumentAnalysis;
import com.example.noticement.domain.document.TechDocument;
import com.example.noticement.domain.notion.PublishedPage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DefaultImmediateAlertMailBuilder implements ImmediateAlertMailBuilder {

    @Override
    public MailCommand build(TechDocument document, DocumentAnalysis analysis, PublishedPage page, String recipientGroup, List<String> recipients) {
        String idempotencyKey = "mail:immediate:" + document.id() + ":" + recipientGroup;
        String subject = "[Tech Alert] " + document.title();

        String actionItems = analysis.actionItems().isEmpty()
                ? "- (no specific action required)"
                : analysis.actionItems().stream()
                        .map(ActionItem::description)
                        .map(description -> "- " + description)
                        .collect(Collectors.joining("\n"));

        String body = """
                %s

                %s

                대응 필요:
                %s

                상세 내용:
                %s
                """.formatted(document.title(), analysis.oneLineSummary(), actionItems, page.pageUrl());

        return new MailCommand(MailCommand.TYPE_IMMEDIATE, idempotencyKey, subject, recipients, body, page.pageUrl());
    }
}
