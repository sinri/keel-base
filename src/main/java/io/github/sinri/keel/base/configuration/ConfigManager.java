package io.github.sinri.keel.base.configuration;

import io.github.sinri.keel.base.VertxHolder;
import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class ConfigManager implements VertxHolder {

    private final Vertx vertx;
    private final ConfigRetriever retriever;
    private final AtomicReference<UnmodifiableJsonifiableEntity> cachedConfigRef = new AtomicReference<>();

    public ConfigManager(Vertx vertx, ConfigStoreOptions configStoreOptions) {
        this.vertx = vertx;
        ConfigRetrieverOptions retrieverOptions = new ConfigRetrieverOptions()
                .addStore(configStoreOptions);
        this.retriever = ConfigRetriever.create(vertx, retrieverOptions);
        this.retriever.listen(configChange -> {
            var x = configChange.getNewConfiguration();
            refreshConfigCache(x);
        });
        this.retriever.getConfig()
                      .andThen(ar -> {
                          if (ar.succeeded()) {
                              System.out.println("config loaded: " + ar.result());
                              refreshConfigCache(ar.result());
                          } else {
                              System.out.println("config load failed: " + ar.cause().getMessage());
                          }
                      });
    }

    public ConfigManager(Vertx vertx) {
        this(
                vertx,
                new ConfigStoreOptions()
                        .setType("file")
                        .setFormat("properties")
                        .setConfig(new JsonObject()
                                .put("path", "config.properties")
                                .put("hierarchical", true)
                        )
        );
    }

    private void refreshConfigCache(JsonObject newConfig) {
        var y = UnmodifiableJsonifiableEntity.wrap(newConfig);
        cachedConfigRef.set(y);
        //System.out.println("Config changed: \n" + y.toFormattedJsonExpression());
    }

    @Override
    public @NotNull Vertx getVertx() {
        return vertx;
    }

    public ConfigRetriever getRetriever() {
        return retriever;
    }

    public @Nullable String readCachedConfig(String... keyChain) {
        UnmodifiableJsonifiableEntity cachedConfig = cachedConfigRef.get();
        if (cachedConfig == null) {
            throw new RuntimeException("No cached config!");
        }
        return cachedConfig.readString(keyChain);
    }

    public Future<String> readConfig(String... keyChain) {
        return retriever.getConfig()
                        .compose(jsonObject -> {
                            refreshConfigCache(jsonObject);
                            String s = readCachedConfig(keyChain);
                            return Future.succeededFuture(s);
                        });
    }
}
