package com.reader.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    @Bean
    ObjectStore objectStore(StorageProperties properties) {
        return new LocalObjectStore(properties.root());
    }
}
