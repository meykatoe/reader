package com.reader.storage.s3;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.LongFunction;
import software.amazon.awssdk.core.ResponseInputStream;

// 讀取時才請求
class LazyRangeInputStream extends InputStream {

    private final long length;
    private final LongFunction<InputStream> opener;
    private InputStream delegate;
    private long position;

    LazyRangeInputStream(long length, LongFunction<InputStream> opener) {
        this.length = length;
        this.opener = opener;
    }

    @Override
    public long skip(long count) throws IOException {
        if (delegate != null) {
            long skipped = delegate.skip(count);
            position += skipped;
            return skipped;
        }
        // 僅移動起點
        long skipped = Math.max(0, Math.min(count, length - position));
        position += skipped;
        return skipped;
    }

    @Override
    public int read() throws IOException {
        if (position >= length) {
            return -1;
        }
        int value = open().read();
        if (value >= 0) {
            position++;
        }
        return value;
    }

    @Override
    public int read(byte[] buffer, int offset, int size) throws IOException {
        if (position >= length) {
            return -1;
        }
        int count = open().read(buffer, offset, size);
        if (count > 0) {
            position += count;
        }
        return count;
    }

    @Override
    public void close() throws IOException {
        if (delegate == null) {
            return;
        }
        // 未讀完直接中斷
        if (position < length && delegate instanceof ResponseInputStream<?> response) {
            response.abort();
        }
        delegate.close();
    }

    private InputStream open() {
        if (delegate == null) {
            delegate = opener.apply(position);
        }
        return delegate;
    }
}
