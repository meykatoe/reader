package com.reader.storage;

import com.reader.storage.s3.S3Clients;
import com.reader.storage.s3.S3ObjectStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    @Bean
    ObjectStore objectStore(StorageProperties properties) {
        return switch (properties.type()) {
            case LOCAL -> new LocalObjectStore(properties.root());
            case S3 -> new S3ObjectStore(
                    S3Clients.create(properties.s3()), properties.s3().bucket());
        };
    }
}
