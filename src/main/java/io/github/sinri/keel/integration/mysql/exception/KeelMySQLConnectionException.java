package io.github.sinri.keel.integration.mysql.exception;

import org.jspecify.annotations.NullMarked;

/**
 * MySQL连接异常类，专门处理MySQL连接相关的错误
 *
 * @since 5.0.0
 */
@NullMarked
public class KeelMySQLConnectionException extends KeelMySQLException {
    /**
     * 构造包含消息的连接异常
     *
     * @param msg 异常消息
     */
    public KeelMySQLConnectionException(String msg) {
        super(msg);
    }

    /**
     * 构造包含消息和原因的连接异常
     * @param msg 异常消息
     * @param cause 异常原因
     */
    public KeelMySQLConnectionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * 构造包含原因的连接异常
     * @param cause 异常原因
     */
    public KeelMySQLConnectionException(Throwable cause) {
        super(cause);
    }
}
