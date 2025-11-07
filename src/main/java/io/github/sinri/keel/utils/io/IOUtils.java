package io.github.sinri.keel.utils.io;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {
    private IOUtils() {
    }
    @TechnicalPreview(since = "4.1.5")
    public static AsyncOutputReadStream toReadStream(@Nonnull InputStream inputStream, @Nonnull Handler<ReadStream<Buffer>> handler) {
        var readStream = AsyncOutputReadStream.create();
        readStream.pause();
        handler.handle(readStream);
        readStream.resume();
        readStream.wrap(inputStream);
        return readStream;
    }

    @TechnicalPreview(since = "4.1.5")
    public static AsyncInputWriteStream toWriteStream(@Nonnull OutputStream outputStream, @Nonnull Handler<WriteStream<Buffer>> handler) {
        var writeStream = AsyncInputWriteStream.create();
        handler.handle(writeStream);
        writeStream.wrap(outputStream);
        return writeStream;
    }
}
