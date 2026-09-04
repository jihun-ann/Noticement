package com.example.noticement.mail;

import com.example.noticement.domain.delivery.MailCommand;
import com.example.noticement.domain.delivery.WeeklyDigestEntry;
import com.example.noticement.domain.notion.PublishedPage;

import java.util.List;

public interface DigestMailBuilder {
    MailCommand build(String isoWeek, List<WeeklyDigestEntry> entries, PublishedPage weeklyPage, String recipientGroup, List<String> recipients);
}
