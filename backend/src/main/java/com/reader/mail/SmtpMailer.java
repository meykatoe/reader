package com.reader.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@EnableConfigurationProperties(MailSettings.class)
public class SmtpMailer implements Mailer {

    private static final Logger log = LoggerFactory.getLogger(SmtpMailer.class);
    private static final String TIMEOUT_MILLIS = "10000";

    private final MailSettings settings;
    private final JavaMailSenderImpl sender;

    public SmtpMailer(MailSettings settings) {
        this.settings = settings;
        this.sender = createSender(settings);
        if (!settings.canSend()) {
            log.warn("Mail is not configured, registration and password reset are unavailable");
        }
    }

    @Override
    public boolean isReady() {
        return settings.canSend();
    }

    @Override
    public void send(String to, String subject, String text) {
        if (!isReady()) {
            throw new MailException(new IllegalStateException("Mail is not configured"));
        }
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(settings.from());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            sender.send(message);
        } catch (MessagingException | org.springframework.mail.MailException ex) {
            throw new MailException(ex);
        }
    }

    private static JavaMailSenderImpl createSender(MailSettings settings) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(settings.host());
        sender.setPort(settings.port());
        sender.setDefaultEncoding(StandardCharsets.UTF_8.name());
        boolean authenticate = StringUtils.hasText(settings.username());
        if (authenticate) {
            sender.setUsername(settings.username());
            sender.setPassword(settings.password());
        }
        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", String.valueOf(authenticate));
        properties.put("mail.smtp.connectiontimeout", TIMEOUT_MILLIS);
        properties.put("mail.smtp.timeout", TIMEOUT_MILLIS);
        properties.put("mail.smtp.writetimeout", TIMEOUT_MILLIS);
        switch (settings.security()) {
            // 禁止明文
            case STARTTLS -> {
                properties.put("mail.smtp.starttls.enable", "true");
                properties.put("mail.smtp.starttls.required", "true");
            }
            case SSL -> properties.put("mail.smtp.ssl.enable", "true");
            case NONE -> {
            }
        }
        properties.put("mail.smtp.ssl.checkserveridentity", "true");
        return sender;
    }
}
