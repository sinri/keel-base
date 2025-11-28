package io.github.sinri.keel.base.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.vertx.core.json.jackson.DatabindCodec;

import java.io.IOException;

/**
 * 针对 {@link JsonSerializable} 接口的兼容实体，实现 Jackson Databind Serializer。
 * <p>
 * 必须在程序运行之初，{@link JsonSerializable} 的子类工作之前，调用 {@link JsonifiableSerializer#register()} 。
 *
 * @since 5.0.0
 */
public class JsonifiableSerializer extends JsonSerializer<JsonSerializable> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 注册序列化器到 Jackson 的 DatabindCodec 中。
     * <p>
     * 此方法必须在程序运行之初，任何 {@link JsonSerializable} 子类进行序列化操作之前调用。
     * <p>
     * 多次调用此方法是安全的，不会产生副作用。
     */
    public static void register() {
        // 注册序列化器
        DatabindCodec.mapper().registerModule(new SimpleModule()
                .addSerializer(JsonSerializable.class, new JsonifiableSerializer()));
    }

    /**
     * 将 {@link JsonSerializable} 对象序列化为 JSON。
     * <p>
     * 此方法通过调用对象的 {@link JsonSerializable#toJsonExpression()} 方法获取 JSON 字符串表达式，
     * 然后将其解析为 {@link JsonNode}，最后写入到 {@link JsonGenerator} 中。
     *
     * @param value       待序列化的 {@link JsonSerializable} 对象
     * @param gen         用于生成 JSON 的生成器
     * @param serializers 序列化提供者
     * @throws IOException 当序列化过程中发生 I/O 错误时抛出
     */
    @Override
    public void serialize(JsonSerializable value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(value.toJsonExpression());
        gen.writeTree(jsonNode);
    }
}
