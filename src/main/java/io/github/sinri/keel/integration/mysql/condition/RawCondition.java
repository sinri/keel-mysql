package io.github.sinri.keel.integration.mysql.condition;


import org.jspecify.annotations.NullMarked;

/**
 * 原始条件类，用于直接存储和生成SQL条件表达式。
 * <p>
 * <strong>安全说明：</strong>传入的表达式会被原样拼入 SQL，不做任何转义。
 * 调用方必须确保表达式内容完全可信，切勿传入不可信的用户输入。
 *
 * @since 5.0.0
 */
@NullMarked
public class RawCondition implements MySQLCondition {
    private String rawConditionExpression;

    /**
     * 构造空的原始条件
     */
    public RawCondition() {
        this.rawConditionExpression = "";
    }

    /**
     * 构造具有指定表达式的原始条件
     *
     * @param rawConditionExpression 原始条件表达式
     */
    public RawCondition(String rawConditionExpression) {
        this.rawConditionExpression = rawConditionExpression;
    }

    /**
     * 设置原始条件表达式
     *
     * @param rawConditionExpression 原始条件表达式
     */
    public void setRawConditionExpression(String rawConditionExpression) {
        this.rawConditionExpression = rawConditionExpression;
    }

    /**
     * 生成SQL的条件表达式文本
     *
     * @return SQL条件表达式
     */
    @Override
    public String toString() {
        return rawConditionExpression;
    }
}
