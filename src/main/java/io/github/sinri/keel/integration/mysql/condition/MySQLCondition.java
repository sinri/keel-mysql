package io.github.sinri.keel.integration.mysql.condition;


import org.jspecify.annotations.NullMarked;

/**
 * MySQL条件接口，定义了SQL条件的通用接口。
 * <p>
 * <strong>安全说明：</strong>条件实现类（{@code CompareCondition}、{@code AmongstCondition}、
 * {@code RawCondition} 等）中的列名、表达式等参数会被直接拼接进生成的 SQL 片段，不做转义或校验。
 * 调用方必须确保这些参数来自可信来源，切勿将不可信的用户输入作为标识符或表达式传入。
 *
 * @since 5.0.0
 */
@NullMarked
public interface MySQLCondition {
    /**
     * 生成SQL的条件表达式文本
     *
     * @return 生成的SQL条件表达式字符串
     */
    String toString();
}