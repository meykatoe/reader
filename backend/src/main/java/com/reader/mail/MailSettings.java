package com.reader.mail;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.StringUtils;

@ConfigurationProperties("reader.mail")
public record MailSettings(
        String host,
        @DefaultValue("587") int port,
        String username,
        String password,
        String from,
        @DefaultValue("starttls") Security security,
        String publicUrl) {

    public enum Security { STARTTLS, SSL, NONE }

    public boolean canSend() {
        return StringUtils.hasText(host) && StringUtils.hasText(from);
    }

    // 只用設定值
    public String link(String path) {
        URI base = URI.create(publicUrl.strip());
        if (base.getHost() == null) {
            throw new IllegalStateException("PUBLIC_URL must be an absolute URL");
        }
        return base.toString().replaceAll("/+$", "") + path;
    }

    public boolean hasPublicUrl() {
        return StringUtils.hasText(publicUrl);
    }

    // 隱藏密碼
    @Override
    public String toString() {
        return "MailSettings[host=" + host + ", port=" + port + ", security=" + security + "]";
    }
}
