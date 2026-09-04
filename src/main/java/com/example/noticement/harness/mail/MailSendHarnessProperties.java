package com.example.noticement.harness.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "harness.mail")
public record MailSendHarnessProperties(
        MailMode mode,
        boolean requireNotionUrl,
        boolean allowExternalRecipients,
        List<String> recipientAllowlist,
        int maxSubjectLength,
        int maxRecipientsPerMessage,
        int maxMessagesPerRun,
        boolean deduplicate
) {}
