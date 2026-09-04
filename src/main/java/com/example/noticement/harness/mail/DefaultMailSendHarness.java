package com.example.noticement.harness.mail;

import com.example.noticement.domain.delivery.MailCommand;
import com.example.noticement.harness.GuardDecision;
import com.example.noticement.harness.GuardStatus;
import com.example.noticement.persistence.repository.MailDeliveryRecordRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@EnableConfigurationProperties(MailSendHarnessProperties.class)
public class DefaultMailSendHarness implements MailSendHarness {

    private static final Set<String> DELIVERED_STATUSES = Set.of("SENT", "DRY_RUN");

    private final MailSendHarnessProperties properties;
    private final MailDeliveryRecordRepository repository;

    public DefaultMailSendHarness(MailSendHarnessProperties properties, MailDeliveryRecordRepository repository) {
        this.properties = properties;
        this.repository = repository;
    }

    @Override
    public GuardDecision validate(MailCommand command) {
        if (command.subject() == null || command.subject().isBlank()) {
            return block("BLOCK_CONTENT_VALIDATION", "subject is blank");
        }
        if (command.subject().length() > properties.maxSubjectLength()) {
            return block("BLOCK_CONTENT_VALIDATION", "subject exceeds max length " + properties.maxSubjectLength());
        }
        if (command.bodyText() == null || command.bodyText().isBlank()) {
            return block("BLOCK_CONTENT_VALIDATION", "body is blank");
        }
        if (command.recipients() == null || command.recipients().isEmpty()) {
            return block("BLOCK_RECIPIENT", "no recipients specified");
        }
        if (command.recipients().size() > properties.maxRecipientsPerMessage()) {
            return block("BLOCK_RECIPIENT", "recipient count exceeds max " + properties.maxRecipientsPerMessage());
        }
        if (!properties.allowExternalRecipients()) {
            for (String recipient : command.recipients()) {
                if (!isAllowlisted(recipient)) {
                    return block("BLOCK_RECIPIENT", "recipient not in allowlist: " + recipient);
                }
            }
        }
        if (properties.requireNotionUrl() && (command.notionPageUrl() == null || command.notionPageUrl().isBlank())) {
            return block("BLOCK_NO_NOTION_URL", "mail requires a published Notion URL");
        }
        if (properties.deduplicate() && isDuplicate(command.idempotencyKey())) {
            return block("BLOCK_DUPLICATE", "mail already delivered for idempotency key " + command.idempotencyKey());
        }
        return allow();
    }

    // ponytail: allowlist match is exact-address-or-domain string comparison, no wildcard/regex support.
    // upgrade if recipient groups need pattern-based allowlisting.
    private boolean isAllowlisted(String recipient) {
        String domain = recipient.substring(recipient.indexOf('@') + 1).toLowerCase();
        return properties.recipientAllowlist().stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(recipient) || allowed.equalsIgnoreCase(domain));
    }

    private boolean isDuplicate(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey)
                .filter(record -> DELIVERED_STATUSES.contains(record.getStatus()))
                .isPresent();
    }

    private GuardDecision allow() {
        return new GuardDecision(GuardStatus.ALLOW, "OK", null, Map.of());
    }

    private GuardDecision block(String code, String message) {
        return new GuardDecision(GuardStatus.BLOCK, code, message, Map.of());
    }
}
