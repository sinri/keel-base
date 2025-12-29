package io.github.sinri.keel.base.json;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;


/**
 * 接口 {@link UnmodifiableJsonifiableEntity} 的实现。
 * <p>
 * 自定义数据读取类可通过继承本类快速实现。
 *
 * @since 5.0.0
 */
public class UnmodifiableJsonifiableEntityImpl implements UnmodifiableJsonifiableEntity {
    private final @NotNull JsonObject jsonObject;

    /**
     * 使用指定的 JSON 对象构造一个不可修改的 JSON 实体实例。
     * <p>
     * 构造函数会调用 {@link #purify(JsonObject)} 方法对输入的 JSON 对象进行净化处理。
     *
     * @param jsonObject 用于构造实体的非空 JSON 对象
     */
    public UnmodifiableJsonifiableEntityImpl(@NotNull JsonObject jsonObject) {
        this.jsonObject = purify(jsonObject);
    }

    /**
     * 在构造函数中使用的净化器，可用于去除原始 JsonObject 中的脏字段（如涉密内容字段）、制作深拷贝等。
     * <p>
     * 本类中的内嵌字段{@link UnmodifiableJsonifiableEntityImpl#jsonObject}为经本方法净化后的实例。
     *
     * @param raw 原始的 JsonObject.
     * @return 净化后的 JsonObject.
     */
    @NotNull
    protected JsonObject purify(@NotNull JsonObject raw) {
        return raw;
    }

    /**
     * 将 JSON 对象编码为紧凑的 JSON 字符串表达式。
     *
     * @return JSON 对象的紧凑字符串表示
     */
    @Override
    public final @NotNull String toJsonExpression() {
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
     * 返回 JSON 对象的字符串表示。
     * <p>
     * 此方法返回与 {@link #toJsonExpression()} 相同的结果。
     *
     * @return JSON 对象的字符串表示
     */
    @NotNull
    @Override
    public final String toString() {
        return toJsonExpression();
    }

    /**
     * 使用 JSON Pointer 从 JSON 对象中读取指定类型的值。
     * <p>
     * 此方法通过函数式接口动态构建 JSON Pointer，并根据返回的类型尝试读取对应的值。
     * 如果类型转换失败，将返回 null。
     *
     * @param func 用于构建 JSON Pointer 并指定返回类型的函数
     * @param <T>  返回值的类型
     * @return 从 JSON Pointer 位置读取的值，如果值不存在或无法转换为指定类型则返回 null
     */
    @Override
    public @Nullable <T> T read(@NotNull Function<@NotNull JsonPointer, @NotNull Class<T>> func) {
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

    /**
     * 返回 JSON 对象中所有键值对的迭代器。
     * <p>
     * 此方法实现了 {@link Iterable} 接口，允许使用增强型 for 循环遍历 JSON 对象。
     *
     * @return 包含所有键值对的迭代器
     */
    @NotNull
    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return jsonObject.iterator();
    }

    /**
     * 判断 JSON 对象是否为空。
     *
     * @return 如果 JSON 对象为空（不包含任何键值对）则返回 true，否则返回 false
     */
    @Override
    public boolean isEmpty() {
        return jsonObject.isEmpty();
    }

    /**
     * 创建一个拷贝。
     * <p>
     * 操作拷贝得的实例，不对当前实例造成影响。
     *
     * @return 拷贝得的实例
     */
    @Override
    @NotNull
    public UnmodifiableJsonifiableEntityImpl copy() {
        return new UnmodifiableJsonifiableEntityImpl(cloneAsJsonObject());
    }
}
