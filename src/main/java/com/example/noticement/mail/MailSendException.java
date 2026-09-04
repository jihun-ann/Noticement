package com.example.noticement.mail;

public class MailSendException extends RuntimeException {
    public MailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
