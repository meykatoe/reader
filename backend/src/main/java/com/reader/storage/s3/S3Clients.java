package com.reader.storage.s3;

import com.reader.storage.StorageProperties;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public final class S3Clients {

    private S3Clients() {
    }

    public static S3Client create(StorageProperties.S3 settings) {
        requireComplete(settings);
        return S3Client.builder()
                .endpointOverride(settings.endpoint())
                .region(Region.of(settings.region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(settings.accessKey(), settings.secretKey())))
                .forcePathStyle(true)
                // 相容第三方服務
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .httpClientBuilder(UrlConnectionHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(10))
                        .socketTimeout(Duration.ofSeconds(60)))
                .build();
    }

    private static void requireComplete(StorageProperties.S3 settings) {
        Map<String, Object> required = new LinkedHashMap<>();
        required.put("S3_ENDPOINT", settings.endpoint());
        required.put("S3_REGION", settings.region());
        required.put("S3_BUCKET", settings.bucket());
        required.put("S3_ACCESS_KEY", settings.accessKey());
        required.put("S3_SECRET_KEY", settings.secretKey());
        List<String> missing = required.entrySet().stream()
                .filter(entry -> entry.getValue() == null
                        || !StringUtils.hasText(entry.getValue().toString()))
                .map(Map.Entry::getKey)
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("缺少設定：" + String.join(", ", missing));
        }
    }
}
