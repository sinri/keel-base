package io.github.sinri.keel.base.configuration;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.vertx.core.json.JsonObject;

import java.util.Properties;

public class ConfigEntity extends JsonifiableDataUnitImpl {
    public ConfigEntity() {
        super();
    }

    public ConfigEntity(JsonObject json) {
        super(json);
    }

    public ConfigEntity(Properties properties) {
        super(propertiesToJsonObject(properties));
    }

    private static JsonObject propertiesToJsonObject(Properties properties) {
        // todo: convert properties to json object,
        //  such as:
        //  input: ```a.b.c=d```
        //  to be
        //  output: ```{"a":{"b":{"c":"d"}}}```

        JsonObject root = new JsonObject();
        if (properties == null) return root;

        properties.forEach((key, value) -> {
            String keyStr = String.valueOf(key);
            String[] parts = keyStr.split("\\.");

            JsonObject current = root;
            for (int i = 0; i < parts.length - 1; i++) {
                String part = parts[i];
                JsonObject next = current.getJsonObject(part);
                if (next == null) {
                    next = new JsonObject();
                    current.put(part, next);
                }
                current = next;
            }

            current.put(parts[parts.length - 1], value);
        });

        return root;
    }
}
