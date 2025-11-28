package io.github.sinri.keel.base.json;

import io.github.sinri.keel.logger.api.LoggingStackSpecification;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * 被 JSON 化的异常对象。
 * <p>
 * 需要提前注册 JSON CODEC ，如执行 {@link JsonifiableSerializer#register()}，才能正常运作。
 *
 * @since 5.0.0
 */
public class JsonifiedThrowable extends JsonifiableDataUnitImpl {

    /**
     * 私有构造函数，用于内部创建实例。
     */
    private JsonifiedThrowable() {
        super();
    }

    /**
     * 将异常对象包装为 JSON 化的异常对象。
     * <p>
     * 使用默认的可忽略堆栈包集合和省略忽略堆栈选项。
     *
     * @param throwable 要包装的异常对象
     * @return JSON 化的异常对象
     */
    @NotNull
    public static JsonifiedThrowable wrap(@NotNull Throwable throwable) {
        return wrap(throwable, LoggingStackSpecification.IgnorableCallStackPackage, true);
    }

    /**
     * 将异常对象包装为 JSON 化的异常对象。
     * <p>
     * 此方法会递归处理异常链（cause），将整个异常链转换为 JSON 结构。
     * 堆栈跟踪信息会根据可忽略的包集合进行过滤和压缩。
     *
     * @param throwable                要包装的异常对象
     * @param ignorableStackPackageSet 可忽略的堆栈包集合，匹配这些包的堆栈项将被过滤
     * @param omitIgnoredStack         是否完全省略被忽略的堆栈项，如果为 false 则用摘要项替代
     * @return JSON 化的异常对象，包含异常链信息
     */
    @NotNull
    public static JsonifiedThrowable wrap(
            @NotNull Throwable throwable,
            @NotNull Set<String> ignorableStackPackageSet,
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
     * 过滤堆栈跟踪并压缩为堆栈项列表。
     * <p>
     * 根据可忽略的包集合过滤堆栈跟踪，将被忽略的连续堆栈项压缩为摘要项。
     *
     * @param stackTrace               堆栈跟踪元素数组
     * @param ignorableStackPackageSet 可忽略的堆栈包集合
     * @param omitIgnoredStack         是否完全省略被忽略的堆栈项
     * @return 过滤和压缩后的堆栈项列表
     */
    @NotNull
    private static List<JsonifiedCallStackItem> filterStackTraceAndReduce(
            @Nullable StackTraceElement[] stackTrace,
            @NotNull Set<String> ignorableStackPackageSet,
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

    /**
     * 过滤堆栈跟踪，将堆栈项分类处理。
     * <p>
     * 根据可忽略的包集合，将堆栈跟踪分为两类：
     * 1. 可忽略的堆栈项：连续的可忽略项会被压缩，通过 ignoredStackTraceItemsConsumer 处理
     * 2. 不可忽略的堆栈项：通过 stackTraceItemConsumer 处理
     *
     * @param stackTrace                     堆栈跟踪元素数组
     * @param ignorableStackPackageSet       可忽略的堆栈包集合
     * @param ignoredStackTraceItemsConsumer 处理被忽略的堆栈项摘要的回调（包名，数量）
     * @param stackTraceItemConsumer         处理不可忽略的堆栈项的回调
     */
    private static void filterStackTrace(
            @Nullable StackTraceElement[] stackTrace,
            @NotNull Set<String> ignorableStackPackageSet,
            @NotNull BiConsumer<String, Integer> ignoredStackTraceItemsConsumer,
            @NotNull Consumer<StackTraceElement> stackTraceItemConsumer
    ) {
        if (stackTrace != null) {
            String ignoringClassPackage = null;
            int ignoringCount = 0;
            for (StackTraceElement stackTranceItem : stackTrace) {
                if (stackTranceItem == null) continue;
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

    /**
     * 获取异常类的全限定名。
     *
     * @return 异常类的全限定名，不会为 null
     */
    @NotNull
    public String getThrowableClass() {
        return Objects.requireNonNull(readString("class"));
    }

    /**
     * 获取异常消息。
     *
     * @return 异常消息，如果异常没有消息则返回 null
     */
    @Nullable
    public String getThrowableMessage() {
        return readString("message");
    }

    /**
     * 获取异常的堆栈跟踪列表。
     * <p>
     * 返回的列表包含堆栈项，每个项可能是调用堆栈项或忽略堆栈摘要项。
     *
     * @return 堆栈跟踪项列表，不会为 null
     */
    @NotNull
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

    /**
     * 获取导致此异常的原因异常。
     * <p>
     * 如果原因异常存在，返回其 JSON 化的异常对象；否则返回 null。
     *
     * @return 原因异常的 JSON 化对象，如果不存在则返回 null
     */
    @Nullable
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

    /**
     * JSON 化的调用堆栈项。
     * <p>
     * 可以是实际的调用堆栈项，也可以是被忽略堆栈的摘要项。
     */
    public static class JsonifiedCallStackItem extends JsonifiableDataUnitImpl {
        /**
         * 使用 JSON 对象构造堆栈项。
         *
         * @param jsonObject 包含堆栈项信息的 JSON 对象
         */
        private JsonifiedCallStackItem(@NotNull JsonObject jsonObject) {
            super(jsonObject);
        }

        /**
         * 构造一个被忽略堆栈的摘要项。
         * <p>
         * 用于表示连续被忽略的堆栈项，包含包名和忽略数量。
         *
         * @param ignoringClassPackage 被忽略的类包名
         * @param ignoringCount        被忽略的堆栈项数量
         */
        private JsonifiedCallStackItem(@NotNull String ignoringClassPackage, int ignoringCount) {
            super(new JsonObject()
                    .put("type", "ignored")
                    .put("package", ignoringClassPackage)
                    .put("count", ignoringCount));
        }

        /**
         * 从堆栈跟踪元素构造调用堆栈项。
         *
         * @param stackTranceItem 堆栈跟踪元素
         */
        private JsonifiedCallStackItem(@NotNull StackTraceElement stackTranceItem) {
            super(new JsonObject()
                    .put("type", "call")
                    .put("class", stackTranceItem.getClassName())
                    .put("method", stackTranceItem.getMethodName())
                    .put("file", stackTranceItem.getFileName())
                    .put("line", stackTranceItem.getLineNumber()));
        }

        /**
         * 获取堆栈项类型。
         * <p>
         * 可能的值：
         * <ul>
         *   <li>"call" - 实际的调用堆栈项</li>
         *   <li>"ignored" - 被忽略堆栈的摘要项</li>
         * </ul>
         *
         * @return 堆栈项类型
         */
        public String getType() {
            return readString("type");
        }

        /**
         * 获取被忽略的类包名。
         * <p>
         * 仅当类型为 "ignored" 时有效。
         *
         * @return 被忽略的类包名
         */
        public String getPackage() {
            return readString("package");
        }

        /**
         * 获取被忽略的堆栈项数量。
         * <p>
         * 仅当类型为 "ignored" 时有效。
         *
         * @return 被忽略的堆栈项数量（字符串形式）
         */
        public String getIgnoredStackCount() {
            return readString("count");
        }

        /**
         * 获取调用堆栈的类名。
         * <p>
         * 仅当类型为 "call" 时有效。
         *
         * @return 调用堆栈的类名
         */
        public String getCallStackClass() {
            return readString("class");
        }

        /**
         * 获取调用堆栈的方法名。
         * <p>
         * 仅当类型为 "call" 时有效。
         *
         * @return 调用堆栈的方法名
         */
        public String getCallStackMethod() {
            return readString("method");
        }

        /**
         * 获取调用堆栈的文件名。
         * <p>
         * 仅当类型为 "call" 时有效。
         *
         * @return 调用堆栈的文件名
         */
        public String getCallStackFile() {
            return readString("file");
        }

        /**
         * 获取调用堆栈的行号。
         * <p>
         * 仅当类型为 "call" 时有效。
         *
         * @return 调用堆栈的行号（字符串形式）
         */
        public String getCallStackLine() {
            return readString("line");
        }
    }
}
