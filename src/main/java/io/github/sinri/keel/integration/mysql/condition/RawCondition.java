package io.github.sinri.keel.integration.mysql.condition;


import org.jetbrains.annotations.NotNull;

/**
 * 原始条件类，用于直接存储和生成SQL条件表达式
 *
 * @since 5.0.0
 */
public class RawCondition implements MySQLCondition {
    @NotNull
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
    public RawCondition(@NotNull String rawConditionExpression) {
        this.rawConditionExpression = rawConditionExpression;
    }

    /**
     * 设置原始条件表达式
     * @param rawConditionExpression 原始条件表达式
     */
    public void setRawConditionExpression(@NotNull String rawConditionExpression) {
        this.rawConditionExpression = rawConditionExpression;
    }

    /**
     * 生成SQL的条件表达式文本
     * @return SQL条件表达式
     */
    @Override
    public String toString() {
        return rawConditionExpression;
    }
}
