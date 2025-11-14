package io.github.sinri.keel.utils.io;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.sinri.keel.facade.KeelInstance.Keel;


/**
 * A conversion utility to help move data from a Vert.x asynchronous stream to Java classic blocking IO.
 * <p>
 * Use this class to create a {@link WriteStream} that buffers data written to it so that a consumer
 * can use the {@link InputStream} API to read it.
 * <p>
 * The default queue size is 1000 writes, but it can be changed using {@link #setWriteQueueMaxSize(int)}
 *
 * @see <a href="https://github.com/cloudonix/vertx-java.io">original project by guss77 (MIT): WriteToInputStream</a>
 * @since 4.1.5
 */
class AsyncInputWriteStreamImpl extends InputStream implements WriteStream<Buffer>, AsyncInputWriteStream {

    private final ConcurrentLinkedQueue<PendingWrite> buffer = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean everFull = new AtomicBoolean();
    private final ConcurrentLinkedQueue<CountDownLatch> readsWaiting = new ConcurrentLinkedQueue<>();
    private final Context context;
    private Handler<Void> drainHandler = __ -> {
    };
    private Handler<Throwable> errorHandler = t -> {
    };
    private volatile int maxSize = 1000;
    private volatile int maxBufferSize = Integer.MAX_VALUE;
    private volatile boolean closed = false;
    private Promise<Void> writeOverPromise;

    public AsyncInputWriteStreamImpl(Vertx vertx) {
        context = vertx.getOrCreateContext();
    }

    public AsyncInputWriteStreamImpl() {
        this(Keel.getVertx());
    }

    @NotNull
    public Promise<Void> getWriteOverPromise() {
        return Objects.requireNonNull(writeOverPromise, "this stream has not been wrapped yet");
    }

    /**
     * Runs {@link #transferTo(OutputStream)} as a blocking task in the Vert.x context
     * The promise {@link AsyncInputWriteStreamImpl#writeOverPromise}
     * that will resolve when the {@linkplain WriteStream} has been closed
     * and all data successfully transfered to the {@linkplain OutputStream}, or reject if
     * there was either a {@linkplain WriteStream} error or an {@linkplain IOException}
     *
     * @param os Output stream to transfer the contents of this {@linkplain WriteStream} to
     */
    public void wrap(@NotNull OutputStream os) {
        this.writeOverPromise = Promise.promise();
        context.executeBlocking(() -> {
                   try (os) {
                       transferTo(os);
                       return null;
                   }
               })
               .onFailure(t -> {
                   if (errorHandler != null)
                       errorHandler.handle(t);
               })
               .onComplete(ar -> {
                   if (ar.failed()) {
                       this.writeOverPromise.fail(ar.cause());
                   } else {
                       this.writeOverPromise.complete();
                   }
               });
    }

    @Override
    public AsyncInputWriteStreamImpl drainHandler(Handler<Void> handler) {
        this.drainHandler = handler;
        return this;
    }

    /* WriteStream stuff */

    @Override
    public Future<Void> end() {
        return write(null);
    }

    @Override
    public AsyncInputWriteStreamImpl exceptionHandler(Handler<Throwable> handler) {
        // we don't have a way to propagate errors as we don't actually handle writing out and InputStream provides no feedback mechanism.
        errorHandler = handler;
        return this;
    }

    @Override
    public Future<Void> write(Buffer data) {
        if (closed)
            // accept all data and discard it, as unlike JDK9 Flow, we have no way to tell upstream to stop sending data
            return Future.succeededFuture();
        var promise = Promise.<Void>promise();
        if (data == null) // end of stream
            buffer.add(new PendingWrite(null, promise));
        else
            for (int start = 0; start < data.length(); ) {
                var buf = data.length() < maxBufferSize ? data : data.getBuffer(start, Math.min(data.length(), start + maxBufferSize));
                start += buf.length();
                buffer.add(new PendingWrite(buf, start < data.length() ? null : promise));
            }
        // flush waiting reads, if any
        for (var l = readsWaiting.poll(); l != null; l = readsWaiting.poll()) l.countDown();
        return promise.future();
    }

    @Override
    public AsyncInputWriteStreamImpl setWriteQueueMaxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    public AsyncInputWriteStreamImpl setMaxChunkSize(int maxSize) {
        maxBufferSize = maxSize;
        return this;
    }

    @Override
    public boolean writeQueueFull() {
        if (buffer.size() < maxSize)
            return false;
        everFull.compareAndSet(false, true);
        return true;
    }

    @Override
    synchronized public int read() throws IOException {
        while (true) {
            while (!buffer.isEmpty() && buffer.peek().shouldDiscard()) buffer.poll();
            if (!buffer.isEmpty())
                return buffer.peek().readNext();
            // set latch to signal we are waiting
            var latch = new CountDownLatch(1);
            readsWaiting.add(latch);
            if (buffer.isEmpty())
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new IOException("Failed to wait for data", e);
                }
            // now try to read again
        }
    }

    /* InputStream stuff */

    @Override
    synchronized public int read(byte[] b, int off, int len) throws IOException {
        if (b.length < off + len) // sanity first
            return 0;
        // we are going to be lazy here and not read more than one pending write, even if there are more available. The contract allows for that
        while (!buffer.isEmpty() && buffer.peek().shouldDiscard()) buffer.poll();
        if (!buffer.isEmpty())
            return buffer.peek().read(b, off, len);
        // we still wait if there is no data, but let read() implement the blocking
        int val = read();
        if (val < 0)
            return val;
        b[off] = (byte) (val & 0xFF);
        return 1 + Math.max(0, read(b, off + 1, len - 1));
    }

    @Override
    public int available() throws IOException {
        return buffer.stream().map(PendingWrite::available).reduce(0, (i, a) -> i += a);
    }

    @Override
    public void close() throws IOException {
        super.close();
        closed = true; // mark us closed, so that additional writes are NOPed
        // if we have any buffered data, flush it and trigger the write completions
        while (!buffer.isEmpty())
            buffer.poll().completion.tryComplete();
        // see if we need to call the drain handler to drain upstream
        if (everFull.compareAndSet(true, false))
            context.runOnContext(drainHandler::handle);
    }

    private class PendingWrite {
        Buffer data;
        Promise<Void> completion;
        int position = 0;

        private PendingWrite(Buffer data, Promise<Void> completion) {
            this.data = data;
            this.completion = Objects.requireNonNullElse(completion, Promise.promise());
        }

        public boolean shouldDiscard() {
            if (data != null && position >= data.length()) {
                completion.tryComplete();
                if (everFull.compareAndSet(true, false))
                    context.runOnContext(drainHandler::handle);
                return true;
            }
            return false;
        }

        public int available() {
            return data == null ? 0 : data.length();
        }

        public int readNext() {
            if (data == null) {
                this.completion.tryComplete();
                return -1;
            }
            int val = 0xFF & data.getByte(position++); // get byte's bitwise value, which is what InputStream#read() is supposed to return
            if (position > data.length())
                completion.tryComplete();
            return val;
        }

        public int read(byte[] b, int off, int len) {
            if (data == null || position >= data.length()) {
                completion.tryComplete();
                return data == null ? -1 : 0;
            }
            int max = Math.min(len, data.length() - position);
            data.getBytes(position, position + max, b, off);
            position += max;
            return max;
        }
    }
}