package io.github.sinri.keel.base.json;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.ClusterSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

/**
 * 本接口定义了一类基于 JSON 对象的数据实体，可读写、转换、重载。
 *
 * @since 5.0.0
 */
public interface JsonifiableDataUnit
        extends JsonObjectConvertible, JsonObjectReloadable, JsonObjectWritable,
        UnmodifiableJsonifiableEntity, ClusterSerializable {

    /**
     * 在 JSON 对象中创建或替换键值对。
     * <p>
     * 如果指定的键已存在，则替换其值；如果不存在，则创建新的键值对。
     *
     * @param key   要创建或替换的键
     * @param value 要设置的值，可以为 null
     */
    @Override
    default void ensureEntry(@NotNull String key, Object value) {
        toJsonObject().put(key, value);
    }

    /**
     * 从 JSON 对象中移除指定的键值对。
     *
     * @param key 要移除的键
     */
    @Override
    default void removeEntry(@NotNull String key) {
        this.toJsonObject().remove(key);
    }

    /**
     * 判断 JSON 对象是否为空。
     *
     * @return 如果 JSON 对象为空（不包含任何键值对）则返回 true，否则返回 false
     */
    @Override
    default boolean isEmpty() {
        return toJsonObject().isEmpty();
    }

    /**
     * 返回 JSON 对象中所有键值对的迭代器。
     * <p>
     * 此方法实现了 {@link Iterable} 接口，允许使用增强型 for 循环遍历 JSON 对象。
     *
     * @return 包含所有键值对的迭代器
     */
    @Override
    @NotNull
    default Iterator<Map.Entry<String, Object>> iterator() {
        return toJsonObject().iterator();
    }

    /**
     * 将 JSON 对象序列化并写入缓冲区。
     * <p>
     * 此方法实现了 {@link ClusterSerializable} 接口，用于集群序列化。
     *
     * @param buffer 要写入的缓冲区
     */
    @Override
    default void writeToBuffer(Buffer buffer) {
        this.toJsonObject().writeToBuffer(buffer);
    }

    /**
     * 从缓冲区中读取序列化的 JSON 对象并重载数据。
     * <p>
     * 此方法遵循 {@link JsonObject#readFromBuffer(int, Buffer)} 的规范。
     * 实现 {@link ClusterSerializable} 接口，用于集群反序列化。
     *
     * @param pos    开始读取缓冲区的位置
     * @param buffer 包含序列化字节的缓冲区
     * @return 下一个要读取的字节位置
     */
    @Override
    default int readFromBuffer(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        int start = pos + 4;
        Buffer buf = buffer.getBuffer(start, start + length);
        this.reloadData(new JsonObject(buf));
        return pos + length + 4;
    }

    /**
     * 将 JSON 对象编码为格式化的 JSON 字符串表达式。
     * <p>
     * 格式化后的字符串包含缩进和换行，便于阅读。
     *
     * @return JSON 对象的格式化字符串表示
     */
    @NotNull
    @Override
    default String toFormattedJsonExpression() {
        return toJsonObject().encodePrettily();
    }

    /**
     * 将 JSON 对象编码为紧凑的 JSON 字符串表达式。
     *
     * @return JSON 对象的紧凑字符串表示
     */
    @NotNull
    @Override
    default String toJsonExpression() {
        return toJsonObject().encode();
    }
}
