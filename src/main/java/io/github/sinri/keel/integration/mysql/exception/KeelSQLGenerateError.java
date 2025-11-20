package io.github.sinri.keel.integration.mysql.exception;

/**
 * SQL生成错误异常类，用于处理SQL语句生成过程中的错误
 *
 * @since 5.0.0
 */
public class KeelSQLGenerateError extends RuntimeException {
    /**
     * 构造SQL生成错误异常
     *
     * @param s 错误消息
     */
    public KeelSQLGenerateError(String s) {
        super(s);
    }
}
