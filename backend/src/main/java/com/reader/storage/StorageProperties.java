package com.reader.storage;

import java.net.URI;
import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("reader.storage")
public record StorageProperties(
        Path root,
        @DefaultValue("local") Type type,
        @DefaultValue S3 s3) {

    public enum Type { LOCAL, S3 }

    public record S3(
            URI endpoint,
            String region,
            String bucket,
            String accessKey,
            String secretKey) {

        // 避免洩漏金鑰
        @Override
        public String toString() {
            return "S3[endpoint=" + endpoint + ", region=" + region + ", bucket=" + bucket + "]";
        }
    }
}
