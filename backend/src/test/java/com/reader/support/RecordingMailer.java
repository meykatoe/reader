package com.reader.support;

import com.reader.mail.MailException;
import com.reader.mail.Mailer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

// 不實際寄信
public class RecordingMailer implements Mailer {

    private static final Pattern CODE = Pattern.compile("(?<!\\d)\\d{6}(?!\\d)");
    private static final Pattern TOKEN = Pattern.compile("#token=([A-Za-z0-9_-]+)");

    public record Sent(String to, String subject, String text) {

        public String code() {
            return find(CODE, text, 0);
        }

        public String token() {
            return find(TOKEN, text, 1);
        }

        private static String find(Pattern pattern, String text, int group) {
            Matcher matcher = pattern.matcher(text);
            return matcher.find() ? matcher.group(group) : null;
        }
    }

    private final List<Sent> sent = new CopyOnWriteArrayList<>();
    private volatile boolean failing;

    @TestConfiguration
    public static class Config {

        @Bean
        @Primary
        RecordingMailer recordingMailer() {
            return new RecordingMailer();
        }
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void send(String to, String subject, String text) {
        if (failing) {
            throw new MailException(new IllegalStateException("smtp down"));
        }
        sent.add(new Sent(to, subject, text));
    }

    public void failing(boolean value) {
        failing = value;
    }

    public List<Sent> sentTo(String address) {
        return sent.stream().filter(mail -> mail.to().equals(address)).toList();
    }
}
