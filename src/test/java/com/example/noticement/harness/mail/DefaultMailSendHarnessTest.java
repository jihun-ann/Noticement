package com.example.noticement.harness.mail;

import com.example.noticement.domain.delivery.MailCommand;
import com.example.noticement.harness.GuardStatus;
import com.example.noticement.persistence.entity.MailDeliveryRecord;
import com.example.noticement.persistence.repository.MailDeliveryRecordRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultMailSendHarnessTest {

    private final MailCommand validCommand = new MailCommand(
            MailCommand.TYPE_IMMEDIATE, "mail:immediate:doc-1:security", "subject",
            List.of("team@example.com"), "body", "https://notion.so/page"
    );

    private MailDeliveryRecordRepository repository() {
        MailDeliveryRecordRepository repository = mock(MailDeliveryRecordRepository.class);
        when(repository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        return repository;
    }

    private MailSendHarnessProperties defaultProperties() {
        return new MailSendHarnessProperties(
                MailMode.DRY_RUN, true, false, List.of("example.com"), 200, 20, 50, true
        );
    }

    @Test
    void allowsValidCommand() {
        var harness = new DefaultMailSendHarness(defaultProperties(), repository());
        assertEquals(GuardStatus.ALLOW, harness.validate(validCommand).status());
    }

    @Test
    void blocksWhenSubjectBlank() {
        var command = new MailCommand(validCommand.deliveryType(), validCommand.idempotencyKey(), " ",
                validCommand.recipients(), validCommand.bodyText(), validCommand.notionPageUrl());
        var harness = new DefaultMailSendHarness(defaultProperties(), repository());
        var decision = harness.validate(command);
        assertEquals(GuardStatus.BLOCK, decision.status());
        assertEquals("BLOCK_CONTENT_VALIDATION", decision.code());
    }

    @Test
    void blocksWhenNoRecipients() {
        var command = new MailCommand(validCommand.deliveryType(), validCommand.idempotencyKey(), validCommand.subject(),
                List.of(), validCommand.bodyText(), validCommand.notionPageUrl());
        var harness = new DefaultMailSendHarness(defaultProperties(), repository());
        var decision = harness.validate(command);
        assertEquals(GuardStatus.BLOCK, decision.status());
        assertEquals("BLOCK_RECIPIENT", decision.code());
    }

    @Test
    void blocksWhenRecipientNotAllowlisted() {
        var command = new MailCommand(validCommand.deliveryType(), validCommand.idempotencyKey(), validCommand.subject(),
                List.of("outsider@other.com"), validCommand.bodyText(), validCommand.notionPageUrl());
        var harness = new DefaultMailSendHarness(defaultProperties(), repository());
        var decision = harness.validate(command);
        assertEquals(GuardStatus.BLOCK, decision.status());
        assertEquals("BLOCK_RECIPIENT", decision.code());
    }

    @Test
    void allowsExternalRecipientWhenPolicyPermits() {
        var permissive = new MailSendHarnessProperties(MailMode.DRY_RUN, true, true, List.of(), 200, 20, 50, true);
        var command = new MailCommand(validCommand.deliveryType(), validCommand.idempotencyKey(), validCommand.subject(),
                List.of("outsider@other.com"), validCommand.bodyText(), validCommand.notionPageUrl());
        var harness = new DefaultMailSendHarness(permissive, repository());
        assertEquals(GuardStatus.ALLOW, harness.validate(command).status());
    }

    @Test
    void blocksWhenNotionUrlMissing() {
        var command = new MailCommand(validCommand.deliveryType(), validCommand.idempotencyKey(), validCommand.subject(),
                validCommand.recipients(), validCommand.bodyText(), "");
        var harness = new DefaultMailSendHarness(defaultProperties(), repository());
        var decision = harness.validate(command);
        assertEquals(GuardStatus.BLOCK, decision.status());
        assertEquals("BLOCK_NO_NOTION_URL", decision.code());
    }

    @Test
    void blocksWhenAlreadyDelivered() {
        MailDeliveryRecord existing = new MailDeliveryRecord(UUID.randomUUID(), "IMMEDIATE", "subject", "team@example.com",
                "https://notion.so/page", validCommand.idempotencyKey());
        existing.markSent("SENT");

        MailDeliveryRecordRepository repository = mock(MailDeliveryRecordRepository.class);
        when(repository.findByIdempotencyKey(validCommand.idempotencyKey())).thenReturn(Optional.of(existing));

        var harness = new DefaultMailSendHarness(defaultProperties(), repository);
        var decision = harness.validate(validCommand);
        assertEquals(GuardStatus.BLOCK, decision.status());
        assertEquals("BLOCK_DUPLICATE", decision.code());
    }
}
