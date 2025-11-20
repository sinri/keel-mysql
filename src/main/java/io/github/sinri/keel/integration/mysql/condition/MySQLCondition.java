package io.github.sinri.keel.integration.mysql.condition;

/**
 * MySQL条件接口，定义了SQL条件的通用接口
 *
 * @since 5.0.0
 */
public interface MySQLCondition {
    /**
     * 生成SQL的条件表达式文本
     * @return 生成的SQL条件表达式字符串
     */
    String toString();
}