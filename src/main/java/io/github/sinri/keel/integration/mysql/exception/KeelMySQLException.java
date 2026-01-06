package io.github.sinri.keel.integration.mysql.exception;

import org.jspecify.annotations.NullMarked;

/**
 * Keel MySQL通用异常类，用于处理MySQL相关的一般性错误
 *
 * @since 5.0.0
 */
@NullMarked
public class KeelMySQLException extends Exception {
    /**
     * 构造包含消息的异常
     *
     * @param msg 异常消息
     */
    public KeelMySQLException(String msg) {
        super(msg);
    }

    /**
     * 构造包含消息和原因的异常
     * @param msg 异常消息
     * @param cause 异常原因
     */
    public KeelMySQLException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * 构造包含原因的异常
     * @param cause 异常原因
     */
    public KeelMySQLException(Throwable cause) {
        super(cause);
    }
}
