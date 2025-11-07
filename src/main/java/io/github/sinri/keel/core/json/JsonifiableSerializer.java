package io.github.sinri.keel.core.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.vertx.core.json.jackson.DatabindCodec;

import java.io.IOException;

/**
 * Implements Jackson Databind Serializer for {@link JsonSerializable}.
 * <p>
 * Must call {@link JsonifiableSerializer#register()} before using any classes which implements
 * {@link JsonSerializable}.
 * <p>
 * As of 4.1.1, the serializer support scope moved from {@link UnmodifiableJsonifiableEntity} to
 * {@link JsonSerializable}.
 *
 * @since 4.1.0
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
