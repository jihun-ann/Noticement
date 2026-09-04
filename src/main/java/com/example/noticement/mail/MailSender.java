package com.example.noticement.mail;

import com.example.noticement.domain.delivery.MailCommand;

public interface MailSender {
    void send(MailCommand command);
}
