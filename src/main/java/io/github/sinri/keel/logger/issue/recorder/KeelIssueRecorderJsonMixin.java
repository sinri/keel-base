package io.github.sinri.keel.logger.issue.recorder;

import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

/**
 * A mixin interface providing extensions to the core issue recording functionality, specifically tailored
 * for handling issues with additional JSON context information. This interface is designed to facilitate
 * logging messages or exceptions with JSON-based context data.
 *
 * @param <T> The type of the {@link KeelIssueRecord} used by this recorder.
 * @since 4.0.1
 */
interface KeelIssueRecorderJsonMixin<T extends KeelIssueRecord<T>> extends KeelIssueRecorderCore<T> {

    default void exception(@Nonnull Throwable throwable, @Nonnull String message, Handler<JsonObject> contextHandler) {
        exception(throwable, t -> {
            t.message(message);
            t.context(contextHandler);
        });
    }

    default void debug(@Nonnull String message, Handler<JsonObject> contextHandler) {
        debug(t -> t.message(message).context(contextHandler));
    }

    default void info(@Nonnull String message, Handler<JsonObject> contextHandler) {
        info(t -> t.message(message).context(contextHandler));
    }

    default void notice(@Nonnull String message, Handler<JsonObject> contextHandler) {
        notice(t -> t.message(message).context(contextHandler));
    }

    default void warning(@Nonnull String message, Handler<JsonObject> contextHandler) {
        warning(t -> t.message(message).context(contextHandler));
    }

    default void error(@Nonnull String message, Handler<JsonObject> contextHandler) {
        error(t -> t.message(message).context(contextHandler));
    }

    default void fatal(@Nonnull String message, Handler<JsonObject> contextHandler) {
        fatal(t -> t.message(message).context(contextHandler));
    }
}
