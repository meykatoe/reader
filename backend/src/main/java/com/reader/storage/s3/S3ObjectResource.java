package com.reader.storage.s3;

import java.io.InputStream;
import java.util.function.LongFunction;
import org.springframework.core.io.AbstractResource;

class S3ObjectResource extends AbstractResource {

    private final String key;
    private final long length;
    private final LongFunction<InputStream> opener;

    S3ObjectResource(String key, long length, LongFunction<InputStream> opener) {
        this.key = key;
        this.length = length;
        this.opener = opener;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public long contentLength() {
        return length;
    }

    @Override
    public InputStream getInputStream() {
        return new LazyRangeInputStream(length, opener);
    }

    @Override
    public String getDescription() {
        return "S3 object " + key;
    }
}
