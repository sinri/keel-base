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

    public static void register() {
        // 注册序列化器
        DatabindCodec.mapper().registerModule(new SimpleModule()
                .addSerializer(JsonSerializable.class, new JsonifiableSerializer()));
    }

    @Override
    public void serialize(JsonSerializable value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(value.toJsonExpression());
        gen.writeTree(jsonNode);
    }
}
