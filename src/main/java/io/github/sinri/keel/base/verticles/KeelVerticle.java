package io.github.sinri.keel.base.verticles;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * Keel 体系下的 {@link Verticle} 强化标准接口。
 * <p>
 * Note: a possibility to base this class on {@link Deployable}.
 *
 * @since 5.0.0
 * @deprecated use {@link KeelVerticleBase}
 */
@Deprecated(since = "5.0.0")
@NullMarked
public interface KeelVerticle extends Verticle {

    String CONFIG_KEY_OF_VERTICLE_IDENTITY = "verticle_identity";

    /**
     * 创建一个即时执行的 Keel Verticle 实例。
     * <p>
     * 此方法创建一个 {@link InstantKeelVerticle} 实例，该实例在启动时会执行提供的函数。
     *
     * @param verticleStartFunc 在 verticle 启动时执行的函数
     * @return 即时执行的 Keel Verticle 实例
     * @see KeelVerticleBase#wrap(Function, Function)
     * @see KeelVerticleBase#wrap(Function)
     */
    static KeelVerticle instant(Function<KeelVerticle, Future<Void>> verticleStartFunc) {
        return new InstantKeelVerticle(verticleStartFunc);
    }

    //    /**
    //     * 仅在本类对应 Verticle 部署后能有效返回 Vertx 实例。
    //     *
    //     * @return Vertx 实例。
    //     */
    //    Vertx getVertx();

    /**
     * 仅在本类对应 Verticle 部署后能获取当前 verticle 的线程模型。
     *
     * @return 当前上下文的线程模型
     */
    ThreadingModel contextThreadModel();

    /**
     * 仅在本类对应 Verticle 部署后能获取当前 verticle 的部署唯一标识。
     *
     * @return 部署唯一标识
     */
    String deploymentID();

    /**
     * 仅在本类对应 Verticle 部署后能获取当前 verticle 的配置。
     * <p>
     * 作为 {@link AbstractVerticle#config()} 的声明。
     *
     * @return 包含当前 verticle 配置的 {@link JsonObject}
     */
    @Nullable JsonObject config();

    /**
     * 仅在本类对应 Verticle 部署后能获取当前 verticle 的信息。
     *
     * @return 包含当前 verticle 信息的 {@link JsonObject}，包含以下字段：
     *         <ul>
     *         <li>class: 当前 verticle 的完全限定类名</li>
     *         <li>config: 当前 verticle 的配置，作为 {@link JsonObject}</li>
     *         <li>deployment_id: 当前 verticle 的部署唯一标识</li>
     *         <li>thread_model: 当前上下文的线程模型，或 {@code null} 如果尚未部署</li>
     *         </ul>
     */
    default JsonObject getVerticleInfo() {
        ThreadingModel threadingModel = contextThreadModel();
        return new JsonObject()
                .put("class", this.getClass().getName())
                .put("config", this.config())
                .put("deployment_id", this.deploymentID())
                .put("thread_model", threadingModel.name());
    }

    /**
     * 在指定Vertx实例下部署当前 verticle 并使用指定的部署选项。
     *
     * @param vertx             Vertx 实例
     * @param deploymentOptions 部署选项
     * @return 一个异步完成，如果部署成功则返回部署唯一标识，如果部署失败则返回异常
     */
    default Future<String> deployMe(Vertx vertx, DeploymentOptions deploymentOptions) {
        if (getRunningState() != KeelVerticleRunningStateEnum.BEFORE_RUNNING) {
            return Future.failedFuture(new IllegalStateException("current verticle status is " + getRunningState()));
        }
        return vertx.deployVerticle(this, deploymentOptions);
    }

    /**
     * 在当前已部署的verticle对应Vertx实例下解除部署。
     *
     * @return 一个异步完成，如果解除部署成功则返回，如果解除部署失败则返回异常
     */
    default Future<Void> undeployMe() {
        String deploymentID = deploymentID();
        return getVertx().undeploy(deploymentID);
    }

    /**
     * 获取当前 verticle 实例的唯一标识或 "身份"。
     * <p>
     * 身份由检查配置中的特定键（{@link #CONFIG_KEY_OF_VERTICLE_IDENTITY}）确定。
     * 如果配置中未找到该键或值为 null，则使用 verticle 的完全限定类名作为标识。
     * 最终返回的格式为：{标识}@{部署唯一标识}。
     *
     * @return 当前 verticle 实例的身份，格式为 "标识@部署ID"
     */
    default String verticleIdentity() {
        String mark = Objects.requireNonNullElseGet(
                Objects.requireNonNullElse(config(), new JsonObject())
                       .getString(CONFIG_KEY_OF_VERTICLE_IDENTITY),
                () -> this.getClass().getName());
        return String.format("%s@%s", mark, deploymentID());
    }

    /**
     * 获取当前 verticle 的运行状态。
     *
     * @return 当前 verticle 的运行状态。
     */
    KeelVerticleRunningStateEnum getRunningState();
}
