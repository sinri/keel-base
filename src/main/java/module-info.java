module io.github.sinri.keel.base {
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive io.github.sinri.keel.logger.api;
    requires transitive io.vertx.config;
    requires transitive io.vertx.core;
    requires transitive io.vertx.core.logging;
    requires transitive org.jetbrains.annotations;

    // Export public APIs, except io.github.sinri.keel.base.internal
    exports io.github.sinri.keel.base;
    exports io.github.sinri.keel.base.annotations;
    exports io.github.sinri.keel.base.async;
    exports io.github.sinri.keel.base.configuration;
    exports io.github.sinri.keel.base.json;
    exports io.github.sinri.keel.base.utils;
    exports io.github.sinri.keel.base.utils.io;
    exports io.github.sinri.keel.base.verticles;
    exports io.github.sinri.keel.base.logger.adapter;
    exports io.github.sinri.keel.base.logger.factory;
    exports io.github.sinri.keel.base.logger.logger;
    exports io.github.sinri.keel.base.logger.metric;

    // Open packages for reflection-based serialization (Jackson or others)
    opens io.github.sinri.keel.base.json;
}