package com.reader.storage;

import com.reader.storage.s3.S3Clients;
import com.reader.storage.s3.S3ObjectStore;
import java.net.URI;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

// 需有測試服務
@EnabledIfEnvironmentVariable(named = "S3_TEST_ENDPOINT", matches = ".+")
class S3ObjectStoreTest extends ObjectStoreContract {

    @Override
    protected ObjectStore store() {
        StorageProperties.S3 settings = new StorageProperties.S3(
                URI.create(System.getenv("S3_TEST_ENDPOINT")),
                System.getenv().getOrDefault("S3_TEST_REGION", "us-east-1"),
                System.getenv("S3_TEST_BUCKET"),
                System.getenv("S3_TEST_ACCESS_KEY"),
                System.getenv("S3_TEST_SECRET_KEY"));
        return new S3ObjectStore(S3Clients.create(settings), settings.bucket());
    }
}
