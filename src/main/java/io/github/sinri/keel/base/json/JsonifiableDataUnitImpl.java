package io.github.sinri.keel.base.json;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * 接口 {@link JsonifiableDataUnit} 的基本实现。
 * <p>
 * 自定义数据类可通过继承本类快速实现。
 *
 * @since 5.0.0
 */
public class JsonifiableDataUnitImpl implements JsonifiableDataUnit {
    @NotNull
    private JsonObject jsonObject;

    /**
     * 使用指定的 JSON 对象构造一个数据单元实例。
     *
     * @param jsonObject 用于构造数据单元的非空 JSON 对象
     */
    public JsonifiableDataUnitImpl(@NotNull JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    /**
     * 构造一个空的数据单元实例。
     * <p>
     * 内部使用空的 JSON 对象进行初始化。
     */
    public JsonifiableDataUnitImpl() {
        this.jsonObject = new JsonObject();
    }

    /**
     * 返回内部包装的 JSON 对象。
     * <p>
     * 注意：此方法返回的是内部 JSON 对象的直接引用，而非副本。
     *
     * @return 内部包装的 JSON 对象
     */
    @NotNull
    @Override
    public JsonObject toJsonObject() {
        return jsonObject;
    }

    /**
     * 使用给定的 JSON 对象重新加载数据。
     * <p>
     * 此方法会用参数中的 JSON 对象完全替换当前实例的内部 JSON 对象。
     *
     * @param jsonObject 用于重载数据的 JSON 对象
     */
    @Override
    public void reloadData(@NotNull JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    /**
     * 将 JSON 对象编码为紧凑的 JSON 字符串表达式。
     *
     * @return JSON 对象的紧凑字符串表示
     */
    @Override
    public @NotNull String toJsonExpression() {
        return jsonObject.encode();
    }

    /**
     * 将 JSON 对象编码为格式化的 JSON 字符串表达式。
     * <p>
     * 格式化后的字符串包含缩进和换行，便于阅读。
     *
     * @return JSON 对象的格式化字符串表示
     */
    @Override
    public @NotNull String toFormattedJsonExpression() {
        return jsonObject.encodePrettily();
    }

    /**
     * 返回数据单元的字符串表示。
     * <p>
     * 此方法返回与 {@link #toJsonExpression()} 相同的结果。
     *
     * @return 数据单元的 JSON 表达式字符串
     */
    @Override
    public String toString() {
        return toJsonExpression();
    }

    @Nullable
    public final <T> T read(@NotNull Function<@NotNull JsonPointer, @NotNull Class<T>> func) {
        try {
            JsonPointer jsonPointer = JsonPointer.create();
            Class<T> tClass = func.apply(jsonPointer);
            Object o = jsonPointer.queryJson(jsonObject);
            if (o == null) {
                return null;
            }
            return tClass.cast(o);
        } catch (ClassCastException castException) {
            return null;
        }
    }
}
