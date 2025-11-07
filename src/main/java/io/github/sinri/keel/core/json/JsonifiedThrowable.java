package io.github.sinri.keel.core.json;

import io.github.sinri.keel.utils.StackUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * A JSON CODEC REGISTER action is required, i.e.
 * {@link JsonifiableSerializer#register()}, to ensure this class to work
 * correctly.
 * <p>
 * As of 4.1.5, it extends {@link JsonifiableDataUnitImpl} to provide a non generic JSON data class.
 *
 * @since 4.1.0
 *
 */
public class JsonifiedThrowable extends JsonifiableDataUnitImpl {

    private JsonifiedThrowable() {
        super();
    }

    public static JsonifiedThrowable wrap(@Nonnull Throwable throwable) {
        return wrap(throwable, StackUtils.IgnorableCallStackPackage, true);
    }

    @Nonnull
    public static JsonifiedThrowable wrap(
            @Nonnull Throwable throwable,
            @Nonnull Set<String> ignorableStackPackageSet,
            boolean omitIgnoredStack
    ) {
        JsonifiedThrowable x = new JsonifiedThrowable();
        x.ensureEntry("class", throwable.getClass().getName());
        x.ensureEntry("message", throwable.getMessage());
        x.ensureEntry("stack", new JsonArray(filterStackTraceAndReduce(
                throwable.getStackTrace(),
                ignorableStackPackageSet,
                omitIgnoredStack)));
        x.ensureEntry("cause", null);

        JsonifiedThrowable upper = x;
        Throwable cause = throwable.getCause();
        while (cause != null) {
            JsonifiedThrowable current = new JsonifiedThrowable();
            current.ensureEntry("class", cause.getClass().getName());
            current.ensureEntry("message", cause.getMessage());
            current.ensureEntry("stack", new JsonArray(filterStackTraceAndReduce(cause.getStackTrace(), ignorableStackPackageSet,
                    omitIgnoredStack)));
            current.ensureEntry("cause", null);
            upper.ensureEntry("cause", current);
            upper = current;

            cause = cause.getCause();
        }
        return x;
    }

    /**
     * @since 2.9 original name: buildStackChainText
     * @since 3.0.0 become private and renamed to filterStackTraceToJsonArray
     */
    @Nonnull
    private static List<JsonifiedCallStackItem> filterStackTraceAndReduce(
            @Nullable StackTraceElement[] stackTrace,
            @Nonnull Set<String> ignorableStackPackageSet,
            boolean omitIgnoredStack
    ) {
        List<JsonifiedCallStackItem> items = new ArrayList<>();

        filterStackTrace(
                stackTrace,
                ignorableStackPackageSet,
                (ignoringClassPackage, ignoringCount) -> {
                    if (!omitIgnoredStack) {
                        items.add(new JsonifiedCallStackItem(ignoringClassPackage, ignoringCount));
                    }
                },
                stackTranceItem -> items.add(new JsonifiedCallStackItem(stackTranceItem))
        );

        return items;
    }

    public static void main(String[] args) {
        JsonifiableSerializer.register();
        try {
            try {
                throw new NullPointerException("1");
            } catch (Exception e) {
                throw new Exception("2", e);
            }
        } catch (Throwable throwable) {
            var jt = JsonifiedThrowable.wrap(throwable);
            System.out.println(jt.toJsonObject().encodePrettily());
        }
    }

    private static void filterStackTrace(
            @Nullable StackTraceElement[] stackTrace,
            @Nonnull Set<String> ignorableStackPackageSet,
            @Nonnull BiConsumer<String, Integer> ignoredStackTraceItemsConsumer,
            @Nonnull Consumer<StackTraceElement> stackTraceItemConsumer
    ) {
        if (stackTrace != null) {
            String ignoringClassPackage = null;
            int ignoringCount = 0;
            for (StackTraceElement stackTranceItem : stackTrace) {
                String className = stackTranceItem.getClassName();
                String matchedClassPackage = null;
                for (var cp : ignorableStackPackageSet) {
                    if (className.startsWith(cp)) {
                        matchedClassPackage = cp;
                        break;
                    }
                }
                if (matchedClassPackage == null) {
                    if (ignoringCount > 0) {
                        ignoredStackTraceItemsConsumer.accept(ignoringClassPackage, ignoringCount);
                        ignoringClassPackage = null;
                        ignoringCount = 0;
                    }

                    stackTraceItemConsumer.accept(stackTranceItem);
                } else {
                    if (ignoringCount > 0) {
                        if (Objects.equals(ignoringClassPackage, matchedClassPackage)) {
                            ignoringCount += 1;
                        } else {
                            ignoredStackTraceItemsConsumer.accept(ignoringClassPackage, ignoringCount);
                            ignoringClassPackage = matchedClassPackage;
                            ignoringCount = 1;
                        }
                    } else {
                        ignoringClassPackage = matchedClassPackage;
                        ignoringCount = 1;
                    }
                }
            }
            if (ignoringCount > 0) {
                ignoredStackTraceItemsConsumer.accept(ignoringClassPackage, ignoringCount);
            }
        }
    }

    public String getThrowableClass() {
        return readString("class");
    }

    public String getThrowableMessage() {
        return readString("message");
    }

    @Nonnull
    public List<JsonifiedCallStackItem> getThrowableStack() {
        List<JsonifiedCallStackItem> items = new ArrayList<>();
        var a = readJsonArray("stack");
        if (a != null) {
            a.forEach(x -> {
                if (x instanceof JsonifiedCallStackItem) {
                    items.add((JsonifiedCallStackItem) x);
                } else if (x instanceof JsonObject) {
                    items.add(new JsonifiedCallStackItem((JsonObject) x));
                }
            });
        }
        return items;
    }

    public JsonifiedThrowable getThrowableCause() {
        Object cause = readValue("cause");
        if (cause instanceof JsonifiedThrowable) {
            return (JsonifiedThrowable) cause;
        } else if (cause instanceof JsonObject) {
            JsonifiedThrowable jsonifiedThrowable = new JsonifiedThrowable();
            jsonifiedThrowable.reloadData((JsonObject) cause);
            return jsonifiedThrowable;
        }
        return null;
    }

    public static class JsonifiedCallStackItem extends JsonifiableDataUnitImpl {
        private JsonifiedCallStackItem(JsonObject jsonObject) {
            super(jsonObject);
        }

        private JsonifiedCallStackItem(String ignoringClassPackage, Integer ignoringCount) {
            super(new JsonObject()
                    .put("type", "ignored")
                    .put("package", ignoringClassPackage)
                    .put("count", ignoringCount));
        }

        private JsonifiedCallStackItem(StackTraceElement stackTranceItem) {
            super(new JsonObject()
                    .put("type", "call")
                    .put("class", stackTranceItem.getClassName())
                    .put("method", stackTranceItem.getMethodName())
                    .put("file", stackTranceItem.getFileName())
                    .put("line", stackTranceItem.getLineNumber()));
        }

        public String getType() {
            return readString("type");
        }

        public String getPackage() {
            return readString("package");
        }

        public String getIgnoredStackCount() {
            return readString("count");
        }

        public String getCallStackClass() {
            return readString("class");
        }

        public String getCallStackMethod() {
            return readString("method");
        }

        public String getCallStackFile() {
            return readString("file");
        }

        public String getCallStackLine() {
            return readString("line");
        }
    }
}
