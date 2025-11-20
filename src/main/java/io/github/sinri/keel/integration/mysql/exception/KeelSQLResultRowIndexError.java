package io.github.sinri.keel.integration.mysql.exception;

/**
 * SQL结果行索引错误异常类，用于处理结果行索引访问的错误
 *
 * @since 5.0.0
 */
public class KeelSQLResultRowIndexError extends Exception {
    /**
     * 构造包含消息的结果行索引错误
     *
     * @param message 错误消息
     */
    public KeelSQLResultRowIndexError(String message) {
        super(message);
    }

    /**
     * 构造包含消息和原因的结果行索引错误
     * @param message 错误消息
     * @param throwable 异常原因
     */
    public KeelSQLResultRowIndexError(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * 构造包含原因的结果行索引错误
     * @param throwable 异常原因
     */
    public KeelSQLResultRowIndexError(Throwable throwable) {
        super(throwable);
    }
}
