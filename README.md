# Keel Base

## Vertx Logging Preparation

在初始化 Keel 框架之前，可以通过以下代码确保 Vert.x 日志提供者有默认的配置（使用 JUL）。

```java
void snippets() {
    String loggingProperty = System.getProperty("vertx.logger-delegate-factory-class-name");
    if (loggingProperty == null) {
        // 显式设置 Vert.x 日志提供者，避免自动探测失败导致 LoggerFactory 初始化异常
        // 必须在任何 Vert.x 类被加载之前设置此属性
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.JULLogDelegateFactory");
    }
}
```