package com.example.noticement.domain.delivery;

import java.util.List;

public record MailCommand(
        String deliveryType,
        String idempotencyKey,
        String subject,
        List<String> recipients,
        String bodyText,
        String notionPageUrl
) {
    public static final String TYPE_IMMEDIATE = "IMMEDIATE";
    public static final String TYPE_WEEKLY = "WEEKLY";
}
