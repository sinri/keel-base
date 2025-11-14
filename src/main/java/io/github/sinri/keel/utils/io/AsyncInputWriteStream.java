package io.github.sinri.keel.utils.io;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;

/**
 * @since 4.1.5
 */
public interface AsyncInputWriteStream extends WriteStream<Buffer> {
    static AsyncInputWriteStream create() {
        return new AsyncInputWriteStreamImpl();
    }

    void wrap(@NotNull OutputStream os);

    @NotNull
    Promise<Void> getWriteOverPromise();

    @NotNull
    default Future<Void> writeOver() {
        return getWriteOverPromise().future();
    }
}
