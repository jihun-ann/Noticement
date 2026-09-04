package com.example.noticement.mail;

import com.example.noticement.domain.delivery.MailCommand;
import com.example.noticement.harness.mail.MailMode;
import com.example.noticement.harness.mail.MailSendHarnessProperties;
import com.example.noticement.persistence.entity.MailDeliveryRecord;
import com.example.noticement.persistence.repository.MailDeliveryRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class DefaultMailSender implements MailSender {

    private static final Logger log = LoggerFactory.getLogger(DefaultMailSender.class);
    private static final java.util.Set<MailMode> SUPPRESSED_MODES = java.util.Set.of(MailMode.LOG_ONLY, MailMode.DRY_RUN);

    private final JavaMailSender javaMailSender;
    private final MailSendHarnessProperties properties;
    private final MailDeliveryRecordRepository repository;

    public DefaultMailSender(JavaMailSender javaMailSender, MailSendHarnessProperties properties, MailDeliveryRecordRepository repository) {
        this.javaMailSender = javaMailSender;
        this.properties = properties;
        this.repository = repository;
    }

    @Override
    @Transactional
    public void send(MailCommand command) {
        MailDeliveryRecord record = repository.findByIdempotencyKey(command.idempotencyKey())
                .orElseGet(() -> new MailDeliveryRecord(
                        UUID.randomUUID(),
                        command.deliveryType(),
                        command.subject(),
                        String.join(",", command.recipients()),
                        command.notionPageUrl(),
                        command.idempotencyKey()));

        try {
            if (SUPPRESSED_MODES.contains(properties.mode())) {
                log.info("[{}] mail suppressed subject='{}' recipients={}", properties.mode(), command.subject(), command.recipients());
                record.markSent("DRY_RUN");
            } else {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setSubject(command.subject());
                message.setTo(command.recipients().toArray(String[]::new));
                message.setText(command.bodyText());
                javaMailSender.send(message);
                record.markSent("SENT");
            }
            repository.save(record);
        } catch (Exception e) {
            record.markFailed(e.getMessage());
            repository.save(record);
            throw new MailSendException("failed to send mail " + command.idempotencyKey(), e);
        }
    }
}
