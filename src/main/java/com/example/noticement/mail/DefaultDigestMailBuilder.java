package com.example.noticement.mail;

import com.example.noticement.domain.delivery.MailCommand;
import com.example.noticement.domain.delivery.WeeklyDigestEntry;
import com.example.noticement.domain.notion.PublishedPage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DefaultDigestMailBuilder implements DigestMailBuilder {

    private static final String TEMPLATE_VERSION = "v1";

    @Override
    public MailCommand build(String isoWeek, List<WeeklyDigestEntry> entries, PublishedPage weeklyPage, String recipientGroup, List<String> recipients) {
        String idempotencyKey = "mail:weekly:" + isoWeek + ":" + recipientGroup + ":" + TEMPLATE_VERSION;
        String subject = "[Tech Weekly] AI / Java / Security 주요 이슈 - " + isoWeek;

        String items = entries.isEmpty()
                ? "- (no items this week)"
                : entries.stream()
                        .map(entry -> "- " + entry.document().title() + ": " + entry.analysis().oneLineSummary())
                        .collect(Collectors.joining("\n"));

        String body = """
                이번 주 요약 (%d건)

                %s

                전체 분석:
                %s
                """.formatted(entries.size(), items, weeklyPage.pageUrl());

        return new MailCommand(MailCommand.TYPE_WEEKLY, idempotencyKey, subject, recipients, body, weeklyPage.pageUrl());
    }
}
