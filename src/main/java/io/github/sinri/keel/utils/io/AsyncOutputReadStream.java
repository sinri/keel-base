package io.github.sinri.keel.utils.io;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;

import javax.annotation.Nonnull;
import java.io.InputStream;

/**
 * @since 4.1.5
 */
public interface AsyncOutputReadStream extends ReadStream<Buffer> {
    static AsyncOutputReadStream create() {
        return new AsyncOutputReadStreamImpl();
    }

    void wrap(@Nonnull InputStream inputStream);

    @Nonnull
    Promise<Long> getReadOverPromise();

    @Nonnull
    default Future<Long> readOver() {
        return getReadOverPromise().future();
    }
}
