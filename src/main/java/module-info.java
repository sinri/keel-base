module io.github.sinri.keel.base {
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires io.github.sinri.keel.logger.api;
    requires io.vertx.config;
    requires io.vertx.core;
    requires io.vertx.core.logging;
    requires org.jetbrains.annotations;

    // Export public APIs, except io.github.sinri.keel.base.internal
    exports io.github.sinri.keel.base;
    exports io.github.sinri.keel.base.annotations;
    exports io.github.sinri.keel.base.async;
    exports io.github.sinri.keel.base.configuration;
    exports io.github.sinri.keel.base.json;
    exports io.github.sinri.keel.base.utils;
    exports io.github.sinri.keel.base.utils.io;
    exports io.github.sinri.keel.base.utils.cron;
    exports io.github.sinri.keel.base.verticles;

    // Open packages for reflection-based serialization (Jackson)
    opens io.github.sinri.keel.base.json to com.fasterxml.jackson.databind;
}