package com.reader.mail;

public interface Mailer {

    boolean isReady();

    void send(String to, String subject, String text);
}
