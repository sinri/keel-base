package io.github.sinri.keel.facade;


import io.github.sinri.keel.logger.api.event.Event2LogRender;
import io.github.sinri.keel.logger.base.factory.StdoutLoggingFactory;
import io.github.sinri.keel.utils.StringUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

class KeelStdoutLoggingFactory extends StdoutLoggingFactory {
    public KeelStdoutLoggingFactory() {
        super(new KeelEvent2LogRender(), null);
    }

    static class KeelEvent2LogRender implements Event2LogRender {
        @Override
        public String renderThrowable(@Nonnull Throwable throwable) {
            return StringUtils.renderThrowableChain(throwable);
        }

        @Override
        public String renderClassification(@Nonnull List<String> classification) {
            return new JsonArray(classification).encode();
        }

        @Override
        public String renderContext(@Nonnull Map<String, Object> context) {
            return new JsonObject(context).encodePrettily();
        }
    }
}
