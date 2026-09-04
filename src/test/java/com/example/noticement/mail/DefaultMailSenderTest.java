package com.example.noticement.mail;

import com.example.noticement.domain.delivery.MailCommand;
import com.example.noticement.harness.mail.MailMode;
import com.example.noticement.harness.mail.MailSendHarnessProperties;
import com.example.noticement.persistence.entity.MailDeliveryRecord;
import com.example.noticement.persistence.repository.MailDeliveryRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultMailSenderTest {

    private final MailCommand command = new MailCommand(
            MailCommand.TYPE_IMMEDIATE, "mail:immediate:doc-1:security", "subject",
            List.of("team@example.com"), "body", "https://notion.so/page"
    );

    private MailSendHarnessProperties properties(MailMode mode) {
        return new MailSendHarnessProperties(mode, true, false, List.of("example.com"), 200, 20, 50, true);
    }

    private MailDeliveryRecordRepository repository() {
        MailDeliveryRecordRepository repository = mock(MailDeliveryRecordRepository.class);
        when(repository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        return repository;
    }

    @Test
    void dryRunModeSuppressesActualSendButRecordsDelivery() {
        JavaMailSender javaMailSender = mock(JavaMailSender.class);
        MailDeliveryRecordRepository repository = repository();

        new DefaultMailSender(javaMailSender, properties(MailMode.DRY_RUN), repository).send(command);

        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
        verify(repository).save(any(MailDeliveryRecord.class));
    }

    @Test
    void liveModeSendsMessageAndRecordsDelivery() {
        JavaMailSender javaMailSender = mock(JavaMailSender.class);
        MailDeliveryRecordRepository repository = repository();

        new DefaultMailSender(javaMailSender, properties(MailMode.LIVE), repository).send(command);

        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(repository).save(any(MailDeliveryRecord.class));
    }

    @Test
    void failureMarksRecordFailedAndRethrows() {
        JavaMailSender javaMailSender = mock(JavaMailSender.class);
        org.mockito.Mockito.doThrow(new RuntimeException("smtp down")).when(javaMailSender).send(any(SimpleMailMessage.class));
        MailDeliveryRecordRepository repository = repository();

        try {
            new DefaultMailSender(javaMailSender, properties(MailMode.LIVE), repository).send(command);
        } catch (MailSendException e) {
            assertEquals("failed to send mail " + command.idempotencyKey(), e.getMessage());
            verify(repository).save(any(MailDeliveryRecord.class));
            return;
        }
        throw new AssertionError("expected MailSendException");
    }
}
