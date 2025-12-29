package io.github.sinri.keel.base.json;

import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;


/**
 * 本接口定义了一类基于 JSON 对象的只读封装。
 * <p>
 * 本接口的实现类应提供一个构造函数，接受 {@link JsonObject} 类型的唯一参数。
 *
 * @since 5.0.0
 */
public interface UnmodifiableJsonifiableEntity
        extends JsonObjectReadable, JsonSerializable, Shareable {

    /**
     * 将 {@link JsonObject} 包装为指定类的实例，该类必须是 {@link UnmodifiableJsonifiableEntity} 的子类型。
     * <p>
     * 指定的类必须有一个接受 {@link JsonObject} 作为参数的构造函数。
     *
     * @param <U>        要包装 {@link JsonObject} 的类类型，必须扩展 {@link UnmodifiableJsonifiableEntity}
     * @param jsonObject 要包装的非空 {@link JsonObject}
     * @param clazz      结果对象的类类型，必须有一个接受 {@link JsonObject} 的构造函数
     * @return 包装提供的 {@link JsonObject} 的指定类实例
     * @throws NoSuchMethodException     如果指定的类没有所需的构造函数
     * @throws InvocationTargetException 如果构造函数抛出异常
     * @throws InstantiationException    如果指定的类无法实例化
     * @throws IllegalAccessException    如果构造函数不可访问
     */
    static <U extends UnmodifiableJsonifiableEntity> @NotNull U wrap(@NotNull JsonObject jsonObject, @NotNull Class<U> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return clazz.getConstructor(JsonObject.class).newInstance(jsonObject);
    }

    /**
     * 将 {@link JsonObject} 包装为 {@link UnmodifiableJsonifiableEntity} 的默认实现实例。
     * <p>
     * 此方法使用 {@link UnmodifiableJsonifiableEntityImpl} 作为默认实现。
     *
     * @param jsonObject 要包装的非空 {@link JsonObject}
     * @return 包装提供的 {@link JsonObject} 的 {@link UnmodifiableJsonifiableEntity} 实例
     */
    static @NotNull UnmodifiableJsonifiableEntity wrap(@NotNull JsonObject jsonObject) {
        return new UnmodifiableJsonifiableEntityImpl(jsonObject);
    }

    /**
     * 生成一个复制的 {@link JsonObject} 实例；不应是缓存值。
     * <p>
     * 默认情况下，它通过 {@link JsonSerializable#toJsonExpression()} 从 JSON 表达式重新生成。
     *
     * @return 由 {@link #toJsonExpression()} 生成的字符串组成的 JSON 对象
     */
    default @NotNull JsonObject cloneAsJsonObject() {
        return new JsonObject(toJsonExpression());
    }

}
