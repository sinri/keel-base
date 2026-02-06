package io.github.sinri.keel.base.async;

/**
 * 本接口定义了可重复调用的任务的露出，用于在异步控制流中进行任务终止。
 *
 * @since 5.0.0
 */
public interface RepeatedlyCallTask {
    /**
     * 决定在本执行块运行结束返回之后，停止执行任务流。
     */
    void stop();
}
