package com.reader.storage.s3;

import com.reader.storage.ObjectStore;
import com.reader.storage.StorageException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Supplier;
import org.springframework.core.io.Resource;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class S3ObjectStore implements ObjectStore, AutoCloseable {

    private final S3Client client;
    private final String bucket;

    public S3ObjectStore(S3Client client, String bucket) {
        this.client = client;
        this.bucket = bucket;
    }

    @Override
    public void put(String key, Path source) {
        call(() -> client.putObject(request -> request.bucket(bucket).key(key),
                RequestBody.fromFile(source)));
    }

    @Override
    public void put(String key, byte[] data) {
        call(() -> client.putObject(request -> request.bucket(bucket).key(key),
                RequestBody.fromBytes(data)));
    }

    @Override
    public Optional<Resource> get(String key) {
        return size(key).stream()
                .mapToObj(length -> (Resource) new S3ObjectResource(key, length, from -> read(key, from)))
                .findFirst();
    }

    private OptionalLong size(String key) {
        try {
            long length = client.headObject(request -> request.bucket(bucket).key(key)).contentLength();
            return OptionalLong.of(length);
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return OptionalLong.empty();
            }
            throw new StorageException(ex);
        } catch (SdkException ex) {
            throw new StorageException(ex);
        }
    }

    // 僅用批次刪除
    @Override
    public void deleteDirectory(String prefix) {
        if (!prefix.endsWith("/")) {
            throw new IllegalArgumentException("Prefix must end with slash: " + prefix);
        }
        ListObjectsV2Response page;
        String token = null;
        do {
            String current = token;
            page = call(() -> client.listObjectsV2(request ->
                    request.bucket(bucket).prefix(prefix).continuationToken(current)));
            deleteAll(page.contents().stream()
                    .map(object -> ObjectIdentifier.builder().key(object.key()).build())
                    .toList());
            token = page.nextContinuationToken();
        } while (Boolean.TRUE.equals(page.isTruncated()));
    }

    @Override
    public void close() {
        client.close();
    }

    private void deleteAll(List<ObjectIdentifier> objects) {
        if (objects.isEmpty()) {
            return;
        }
        DeleteObjectsResponse response = call(() -> client.deleteObjects(request ->
                request.bucket(bucket).delete(delete -> delete.objects(objects))));
        // 檢查個別錯誤
        if (!response.errors().isEmpty()) {
            throw new StorageException(new IllegalStateException(
                    "Failed to delete: " + response.errors().getFirst()));
        }
    }

    private InputStream read(String key, long from) {
        return call(() -> client.getObject(request -> {
            request.bucket(bucket).key(key);
            if (from > 0) {
                request.range("bytes=" + from + "-");
            }
        }));
    }

    private static <T> T call(Supplier<T> action) {
        try {
            return action.get();
        } catch (SdkException ex) {
            throw new StorageException(ex);
        }
    }
}
