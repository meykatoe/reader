package com.reader.storage;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("reader.storage")
public record StorageProperties(Path root) {
}
