package io.github.sinri.keel.base.json;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * 本接口定义了一类可作为 JSON 对象读取的实体。
 *
 * @since 5.0.0
 */
@NullMarked
public interface JsonObjectReadable extends Iterable<Map.Entry<String, Object>> {

    /**
     * 使用 JSON Pointer 从 JSON 结构中读取指定类型的值。
     * <p>
     * 此方法通过函数式接口动态构建 JSON Pointer，并根据返回的类型尝试读取对应的值。
     *
     * @param func 用于构建 JSON Pointer 并指定返回类型的函数
     * @param <T>  返回值的类型
     * @return 从 JSON Pointer 位置读取的值，如果值不存在或无法转换为指定类型则返回 null
     */
    @Nullable
    <T> T read(Function<JsonPointer, Class<T>> func);

    /**
     * 使用指定的 JSON Pointer 参数从 JSON 结构中读取字符串值。
     * <p>
     * 此方法使用提供的参数动态构建 JSON Pointer，并尝试在结果位置检索字符串值。
     *
     * @param args 用于构建 JSON Pointer 的字符串参数序列，每个参数代表 JSON 路径中的一个步骤
     * @return 在 JSON Pointer 位置找到的字符串值，如果值不存在或无法读取为字符串则返回 null
     */
    default @Nullable String readString(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return String.class;
        });
    }

    /**
     * {@link #readString(String...)} 方法的快捷方式，使用 {@link Objects#requireNonNull(Object)} 确保非空。
     *
     * @param args JSON Pointer 参数
     * @return 非空的字符串值
     * @throws NullPointerException 如果值为 null
     */
    default String readStringRequired(String... args) {
        var r = readString(args);
        Objects.requireNonNull(r);
        return r;
    }

    /**
     * 使用指定的 JSON Pointer 参数从 JSON 结构中读取数值。
     * <p>
     * 此方法使用提供的参数动态构建 JSON Pointer，并尝试在结果位置检索数值。
     *
     * @param args 用于构建 JSON Pointer 的字符串参数序列，每个参数代表 JSON 路径中的一个步骤
     * @return 在 JSON Pointer 位置找到的数值，如果值不存在或无法读取为数值则返回 null
     */
    default @Nullable Number readNumber(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return Number.class;
        });
    }

    /**
     * {@link #readNumber(String...)} 方法的快捷方式，使用 {@link Objects#requireNonNull(Object)} 确保非空。
     *
     * @param args JSON Pointer 参数
     * @return 非空的数值
     * @throws NullPointerException 如果值为 null
     */
    default Number readNumberRequired(String... args) {
        var r = readNumber(args);
        Objects.requireNonNull(r);
        return r;
    }

    /**
     * 使用指定的 JSON Pointer 参数从 JSON 结构中读取 Long 值。
     * <p>
     * 此方法先读取数值，然后将其转换为 Long 类型。
     *
     * @param args 用于构建 JSON Pointer 的字符串参数序列
     * @return 在 JSON Pointer 位置找到的 Long 值，如果值不存在或无法读取为数值则返回 null
     */
    default @Nullable Long readLong(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.longValue();
    }

    /**
     * {@link #readLong(String...)} 方法的快捷方式，使用 {@link Objects#requireNonNull(Object)} 确保非空。
     *
     * @param args JSON Pointer 参数
     * @return 非空的 Long 值
     * @throws NullPointerException 如果值为 null
     */
    default Long readLongRequired(String... args) {
        var r = readLong(args);
        Objects.requireNonNull(r);
        return r;
    }

    /**
     * 使用指定的 JSON Pointer 参数从 JSON 结构中读取 Integer 值。
     * <p>
     * 此方法先读取数值，然后将其转换为 Integer 类型。
     *
     * @param args 用于构建 JSON Pointer 的字符串参数序列
     * @return 在 JSON Pointer 位置找到的 Integer 值，如果值不存在或无法读取为数值则返回 null
     */
    default @Nullable Integer readInteger(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.intValue();
    }

    /**
     * {@link #readInteger(String...)} 方法的快捷方式，使用 {@link Objects#requireNonNull(Object)} 确保非空。
     *
     * @param args JSON Pointer 参数
     * @return 非空的 Integer 值
     * @throws NullPointerException 如果值为 null
     */
    default Integer readIntegerRequired(String... args) {
        var r = readInteger(args);
        Objects.requireNonNull(r);
        return r;
    }

    /**
     * 使用指定的 JSON Pointer 参数从 JSON 结构中读取 Float 值。
     * <p>
     * 此方法先读取数值，然后将其转换为 Float 类型。
     *
     * @param args 用于构建 JSON Pointer 的字符串参数序列
     * @return 在 JSON Pointer 位置找到的 Float 值，如果值不存在或无法读取为数值则返回 null
     */
    default @Nullable Float readFloat(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.floatValue();
    }

    /**
     * {@link #readFloat(String...)} 方法的快捷方式，使用 {@link Objects#requireNonNull(Object)} 确保非空。
     *
     * @param args JSON Pointer 参数
     * @return 非空的 Float 值
     * @throws NullPointerException 如果值为 null
     */
    default Float readFloatRequired(String... args) {
        var r = readFloat(args);
        Objects.requireNonNull(r);
        return r;
    }

    /**
     * 使用指定的 JSON Pointer 参数从 JSON 结构中读取 Double 值。
     * <p>
     * 此方法先读取数值，然后将其转换为 Double 类型。
     *
     * @param args 用于构建 JSON Pointer 的字符串参数序列
     * @return 在 JSON Pointer 位置找到的 Double 值，如果值不存在或无法读取为数值则返回 null
     */
    default @Nullable Double readDouble(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.doubleValue();
    }

    /**
     * {@link #readDouble(String...)} 方法的快捷方式，使用 {@link Objects#requireNonNull(Object)} 确保非空。
     *
     * @param args JSON Pointer 参数
     * @return 非空的 Double 值
     * @throws NullPointerException 如果值为 null
     */
    default Double readDoubleRequired(String... args) {
        var r = readDouble(args);
        Objects.requireNonNull(r);
        return r;
    }

    /**
     * 使用指定的 JSON Pointer 参数从 JSON 结构中读取 Boolean 值。
     * <p>
     * 此方法使用提供的参数动态构建 JSON Pointer，并尝试在结果位置检索 Boolean 值。
     *
     * @param args 用于构建 JSON Pointer 的字符串参数序列，每个参数代表 JSON 路径中的一个步骤
     * @return 在 JSON Pointer 位置找到的 Boolean 值，如果值不存在或无法读取为 Boolean 则返回 null
     */
    default @Nullable Boolean readBoolean(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return Boolean.class;
        });
    }

    /**
     * {@link #readBoolean(String...)} 方法的快捷方式，使用 {@link Objects#requireNonNull(Object)} 确保非空。
     *
     * @param args JSON Pointer 参数
     * @return 非空的 Boolean 值
     * @throws NullPointerException 如果值为 null
     */
    default Boolean readBooleanRequired(String... args) {
        var r = readBoolean(args);
        Objects.requireNonNull(r);
        return r;
    }

    /**
     * 使用指定的 JSON Pointer 参数从 JSON 结构中读取 JsonObject 值。
     * <p>
     * 此方法使用提供的参数动态构建 JSON Pointer，并尝试在结果位置检索 JsonObject 值。
     *
     * @param args 用于构建 JSON Pointer 的字符串参数序列，每个参数代表 JSON 路径中的一个步骤
     * @return 在 JSON Pointer 位置找到的 JsonObject 值，如果值不存在或无法读取为 JsonObject 则返回 null
     */
    default @Nullable JsonObject readJsonObject(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonObject.class;
        });
    }

    /**
     * {@link #readJsonObject(String...)} 方法的快捷方式，使用 {@link Objects#requireNonNull(Object)} 确保非空。
     *
     * @param args JSON Pointer 参数
     * @return 非空的 JsonObject 值
     * @throws NullPointerException 如果值为 null
     */
    default JsonObject readJsonObjectRequired(String... args) {
        var r = readJsonObject(args);
        Objects.requireNonNull(r);
        return r;
    }

    /**
     * 使用指定的 JSON Pointer 参数从 JSON 结构中读取 JsonArray 值。
     * <p>
     * 此方法使用提供的参数动态构建 JSON Pointer，并尝试在结果位置检索 JsonArray 值。
     *
     * @param args 用于构建 JSON Pointer 的字符串参数序列，每个参数代表 JSON 路径中的一个步骤
     * @return 在 JSON Pointer 位置找到的 JsonArray 值，如果值不存在或无法读取为 JsonArray 则返回 null
     */
    default @Nullable JsonArray readJsonArray(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
    }

    /**
     * {@link #readJsonArray(String...)} 方法的快捷方式，使用 {@link Objects#requireNonNull(Object)} 确保非空。
     *
     * @param args JSON Pointer 参数
     * @return 非空的 JsonArray 值
     * @throws NullPointerException 如果值为 null
     */
    default JsonArray readJsonArrayRequired(String... args) {
        var r = readJsonArray(args);
        Objects.requireNonNull(r);
        return r;
    }

    /**
     * 使用指定的 JSON Pointer 参数从 JSON 结构中读取 JsonObject 数组。
     * <p>
     * 此方法先读取 JsonArray，然后将其转换为 List&lt;JsonObject&gt;。
     * 如果数组中的元素不是有效 JsonObject 类型，将抛出 RuntimeException。
     *
     * @param args 用于构建 JSON Pointer 的字符串参数序列
     * @return 在 JSON Pointer 位置找到的 JsonObject 列表，如果值不存在则返回 null
     * @throws RuntimeException 如果数组中的元素不是 JsonObject 类型
     */
    default @Nullable List<JsonObject> readJsonObjectArray(String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<JsonObject> list = new ArrayList<>();
        array.forEach(x -> {
            if (x instanceof JsonObject jsonObject) {
                list.add(jsonObject);
            } else {
                throw new RuntimeException("NOT JSON OBJECT");
            }
        });
        return list;
    }

    /**
     * {@link #readJsonObjectArray(String...)} 方法的快捷方式，使用 {@link Objects#requireNonNull(Object)} 确保非空。
     *
     * @param args JSON Pointer 参数
     * @return 非空的 JsonObject 列表
     * @throws NullPointerException 如果值为 null
     */
    default List<JsonObject> readJsonObjectArrayRequired(String... args) {
        var r = readJsonObjectArray(args);
        Objects.requireNonNull(r);
        return r;
    }

    /**
     * 使用指定的 JSON Pointer 参数从 JSON 结构中读取字符串数组。
     * <p>
     * 此方法先读取 JsonArray，然后将其转换为 List&lt;String&gt;。
     * 数组中的每个元素必须都是非 null 字符串。
     *
     * @param args 用于构建 JSON Pointer 的字符串参数序列
     * @return 在 JSON Pointer 位置找到的字符串列表，如果值不存在则返回 null
     */
    default @Nullable List<String> readStringArray(String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<String> list = new ArrayList<>();
        array.forEach(x -> {
            if (x instanceof String s) {
                list.add(s);
            } else {
                throw new RuntimeException("Not String");
            }
        });
        return list;
    }

    /**
     * {@link #readStringArray(String...)} 方法的快捷方式，使用 {@link Objects#requireNonNull(Object)} 确保非空。
     *
     * @param args JSON Pointer 参数
     * @return 非空的字符串列表
     * @throws NullPointerException 如果值为 null
     */
    default List<String> readStringArrayRequired(String... args) {
        var r = readStringArray(args);
        Objects.requireNonNull(r);
        return r;
    }

    /**
     * 使用指定的 JSON Pointer 参数从 JSON 结构中读取 Integer 数组。
     * <p>
     * 此方法先读取 JsonArray，然后将其转换为 List&lt;Integer&gt;。
     * 如果数组中的元素不是 Number 类型，将抛出 RuntimeException。
     * null 值将被转换为 0。
     *
     * @param args 用于构建 JSON Pointer 的字符串参数序列
     * @return 在 JSON Pointer 位置找到的 Integer 列表，如果值不存在则返回 null
     * @throws RuntimeException 如果数组中的元素不是 Number 类型
     */
    default @Nullable List<Integer> readIntegerArray(String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<Integer> list = new ArrayList<>();
        array.forEach(x -> {
            if (x instanceof Number number) {
                list.add(number.intValue());
            } else {
                throw new RuntimeException("Not Integer");
            }
        });
        return list;
    }

    /**
     * {@link #readIntegerArray(String...)} 方法的快捷方式，使用 {@link Objects#requireNonNull(Object)} 确保非空。
     *
     * @param args JSON Pointer 参数
     * @return 非空的 Integer 列表
     * @throws NullPointerException 如果值为 null
     */
    default List<Integer> readIntegerArrayRequired(String... args) {
        var r = readIntegerArray(args);
        Objects.requireNonNull(r);
        return r;
    }

    /**
     * 使用指定的 JSON Pointer 参数从 JSON 结构中读取 Long 数组。
     * <p>
     * 此方法先读取 JsonArray，然后将其转换为 List&lt;Long&gt;。
     * 如果数组中的元素不是 Number 类型，将抛出 RuntimeException。
     * null 值将被转换为 0L。
     *
     * @param args 用于构建 JSON Pointer 的字符串参数序列
     * @return 在 JSON Pointer 位置找到的 Long 列表，如果值不存在则返回 null
     * @throws RuntimeException 如果数组中的元素不是 Number 类型
     */
    default @Nullable List<Long> readLongArray(String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<Long> list = new ArrayList<>();
        array.forEach(x -> {
            if (x instanceof Number number) {
                list.add(number.longValue());
            } else {
                throw new RuntimeException("Not Long");
            }
        });
        return list;
    }

    /**
     * {@link #readLongArray(String...)} 方法的快捷方式，使用 {@link Objects#requireNonNull(Object)} 确保非空。
     *
     * @param args JSON Pointer 参数
     * @return 非空的 Long 列表
     * @throws NullPointerException 如果值为 null
     */
    default List<Long> readLongArrayRequired(String... args) {
        var r = readLongArray(args);
        Objects.requireNonNull(r);
        return r;
    }

    /**
     * 使用指定的 JSON Pointer 参数从 JSON 结构中读取 Float 数组。
     * <p>
     * 此方法先读取 JsonArray，然后将其转换为 List&lt;Float&gt;。
     * 如果数组中的元素不是 Number 类型，将抛出 RuntimeException。
     * null 值将被转换为 0.0f。
     *
     * @param args 用于构建 JSON Pointer 的字符串参数序列
     * @return 在 JSON Pointer 位置找到的 Float 列表，如果值不存在则返回 null
     * @throws RuntimeException 如果数组中的元素不是 Number 类型
     */
    default @Nullable List<Float> readFloatArray(String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<Float> list = new ArrayList<>();
        array.forEach(x -> {
            if (x instanceof Number number) {
                list.add(number.floatValue());
            } else {
                throw new RuntimeException("Not Float");
            }
        });
        return list;
    }

    /**
     * {@link #readFloatArray(String...)} 方法的快捷方式，使用 {@link Objects#requireNonNull(Object)} 确保非空。
     *
     * @param args JSON Pointer 参数
     * @return 非空的 Float 列表
     * @throws NullPointerException 如果值为 null
     */
    default List<Float> readFloatArrayRequired(String... args) {
        var r = readFloatArray(args);
        Objects.requireNonNull(r);
        return r;
    }

    /**
     * 使用指定的 JSON Pointer 参数从 JSON 结构中读取 Double 数组。
     * <p>
     * 此方法先读取 JsonArray，然后将其转换为 List&lt;Double&gt;。
     * 如果数组中的元素不是 Number 类型，将抛出 RuntimeException。
     * null 值将被转换为 0.0。
     *
     * @param args 用于构建 JSON Pointer 的字符串参数序列
     * @return 在 JSON Pointer 位置找到的 Double 列表，如果值不存在则返回 null
     * @throws RuntimeException 如果数组中的元素不是 Number 类型
     */
    default @Nullable List<Double> readDoubleArray(String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<Double> list = new ArrayList<>();
        array.forEach(x -> {
            if (x instanceof Number number) {
                list.add(number.doubleValue());
            } else {
                throw new RuntimeException("Not Double");
            }
        });
        return list;
    }

    /**
     * {@link #readDoubleArray(String...)} 方法的快捷方式，使用 {@link Objects#requireNonNull(Object)} 确保非空。
     *
     * @param args JSON Pointer 参数
     * @return 非空的 Double 列表
     * @throws NullPointerException 如果值为 null
     */
    default List<Double> readDoubleArrayRequired(String... args) {
        var r = readDoubleArray(args);
        Objects.requireNonNull(r);
        return r;
    }

    /**
     * 使用指定的 JSON Pointer 参数从 JSON 结构中读取任意类型的值。
     * <p>
     * 此方法使用提供的参数动态构建 JSON Pointer，并尝试在结果位置检索值。
     *
     * @param args 用于构建 JSON Pointer 的字符串参数序列，每个参数代表 JSON 路径中的一个步骤
     * @return 在 JSON Pointer 位置找到的值，如果值不存在则返回 null
     */
    default @Nullable Object readValue(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return Object.class;
        });
    }

    /**
     * {@link #readValue(String...)} 方法的快捷方式，使用 {@link Objects#requireNonNull(Object)} 确保非空。
     *
     * @param args JSON Pointer 参数
     * @return 非空的值
     * @throws NullPointerException 如果值为 null
     */
    default Object readValueRequired(String... args) {
        var r = readValue(args);
        Objects.requireNonNull(r);
        return r;
    }

    /**
     * 使用 Jackson 的 {@link JsonObject#mapTo(Class)} 从 JSON 对象中读取实体。
     *
     * @param cClass 要读取的实体的类
     * @param args   用于形成 JSON Pointer 的参数，用于在更大的结构中定位 JSON 对象
     * @param <C>    要读取的实体的类型
     * @return 从 JSON 对象中读取的实体，如果读取失败则返回 null
     */
    default <C> @Nullable C readEntity(Class<C> cClass, String... args) {
        JsonObject jsonObject = readJsonObject(args);
        if (jsonObject == null) {
            return null;
        }
        try {
            return jsonObject.mapTo(cClass);
        } catch (Throwable t) {
            return null;
        }
    }


    /**
     * 判断 JSON 对象是否为空。
     *
     * @return 如果 JSON 对象为空（不包含任何键值对）则返回 true，否则返回 false
     */
    boolean isEmpty();

    /**
     * 返回 JSON 对象中所有键值对的迭代器。
     * <p>
     * 此方法实现了 {@link Iterable} 接口，允许使用增强型 for 循环遍历 JSON 对象。
     *
     * @return 包含所有键值对的迭代器
     */
    @Override
    Iterator<Map.Entry<String, Object>> iterator();
}