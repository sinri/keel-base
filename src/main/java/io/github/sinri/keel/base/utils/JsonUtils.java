package io.github.sinri.keel.base.utils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class JsonUtils {
    /**
     * Private constructor to prevent instantiation.
     */
    private JsonUtils() {
        // Utility class, no instance needed
    }

    /**
     * @since 2.4
     */
    @NotNull
    private static JsonArray getSortedJsonArray(@NotNull JsonArray array) {
        List<Object> list = new ArrayList<>();
        array.forEach(list::add);
        list.sort(Comparator.comparing(Object::toString));
        return new JsonArray(list);
    }

    /**
     * @since 2.4
     */
    @NotNull
    public static String getJsonForArrayWhoseItemsSorted(@NotNull JsonArray array) {
        return getSortedJsonArray(array).toString();
    }

    /**
     * @since 2.4
     */
    @NotNull
    private static JsonObject getSortedJsonObject(@NotNull JsonObject object) {
        JsonObject result = new JsonObject();
        List<String> keyList = new ArrayList<>(object.getMap().keySet());
        keyList.sort(Comparator.naturalOrder());
        keyList.forEach(key -> {
            Object value = object.getValue(key);
            if (value instanceof JsonObject) {
                result.put(key, getSortedJsonObject((JsonObject) value));
            } else if (value instanceof JsonArray) {
                result.put(key, getSortedJsonArray((JsonArray) value));
            } else {
                result.put(key, value);
            }
        });
        return result;
    }

    /**
     * @since 2.4
     */
    @NotNull
    public static String getJsonForObjectWhoseItemKeysSorted(@NotNull JsonObject object) {
        return getSortedJsonObject(object).toString();
    }

    public static void filterStackTrace(
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
     * @since 3.0.0
     */
    @NotNull
    public static String renderJsonToStringBlock(@Nullable String name, @Nullable Object object) {
        if (object == null) {
            return "null";
        }
        return renderJsonItem(name, object, 0, null);
    }

    /**
     * @param key    Key of entry amongst the entries, or the index amongst the array.
     * @param object Value of entry amongst the entries, or the item amongst the array.
     * @return rendered string block ended with NEW_LINE.
     */
    @NotNull
    private static String renderJsonItem(@Nullable String key, @Nullable Object object, int indentation, @Nullable String typeMark) {
        StringBuilder subBlock = new StringBuilder();
        if (indentation > 1) {
            subBlock.append(" ".repeat(indentation - 2));
            subBlock.append(typeMark).append(" ");
        } else {
            subBlock.append(" ".repeat(Math.max(0, indentation)));
        }

        if (key != null) {
            subBlock.append(key).append(": ");
        }
        if (object instanceof JsonObject) {
            subBlock.append("\n");
            ((JsonObject) object).forEach(entry -> subBlock.append(
                    renderJsonItem(entry.getKey(), entry.getValue(), indentation + 2, "+")
            ));
        } else if (object instanceof JsonArray) {
            subBlock.append("\n");
            for (int i = 0; i < ((JsonArray) object).size(); i++) {
                subBlock.append(
                        renderJsonItem(i + "", ((JsonArray) object).getValue(i), indentation + 2, "-")
                );
            }
        } else {
            subBlock.append(object).append("\n");
        }
        return subBlock.toString();
    }
}
