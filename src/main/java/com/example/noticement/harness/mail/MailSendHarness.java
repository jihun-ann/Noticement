package com.example.noticement.harness.mail;

import com.example.noticement.domain.delivery.MailCommand;
import com.example.noticement.harness.GuardDecision;

public interface MailSendHarness {
    GuardDecision validate(MailCommand command);
}
