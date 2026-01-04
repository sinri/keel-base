package io.github.sinri.keel.base.json;

import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;

/**
 * 本接口定义了一类可通过 JSON 对象重载数据的实体。
 *
 * @since 4.1.1
 */
@NullMarked
public interface JsonObjectReloadable {
    /**
     * 使用给定的 JSON 对象重新加载实体的数据。
     * <p>
     * 此方法会用参数中的 JSON 对象完全替换或重建当前实体的内部状态。
     * 调用此方法后，实体的状态应与给定的 JSON 对象保持一致。
     *
     * @param jsonObject 用于重载数据的 JSON 对象，通常由 {@link JsonObjectConvertible#toJsonObject()} 方法生成
     */
    void reloadData(JsonObject jsonObject);

}
