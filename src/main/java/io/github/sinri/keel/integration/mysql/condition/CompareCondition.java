package io.github.sinri.keel.integration.mysql.condition;

import io.github.sinri.keel.integration.mysql.Quoter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/**
 * 比较条件类，用于构建各种比较类型的SQL条件表达式
 *
 * @since 5.0.0
 */
@NullMarked
public class CompareCondition implements MySQLCondition {
    public static final String OP_EQ = "=";
    public static final String OP_NEQ = "<>";
    public static final String OP_NULL_SAFE_EQ = "<=>";
    public static final String OP_GT = ">";
    public static final String OP_EGT = ">=";
    public static final String OP_LT = "<";
    public static final String OP_ELT = "<=";
    public static final String OP_IS = "IS";
    public static final String OP_LIKE = "LIKE";
    protected @Nullable String leftSide;
    protected @Nullable String operator;
    protected @Nullable String rightSide;
    protected boolean inverseOperator;

    public CompareCondition() {
        this.leftSide = null;
        this.operator = null;
        this.rightSide = null;
        this.inverseOperator = false;
    }

    public CompareCondition(String operator) {
        this.leftSide = null;
        this.operator = operator;
        this.rightSide = null;
        this.inverseOperator = false;
    }

    /**
     * 设置为等于操作符
     *
     * @return 自身实例
     */
    public CompareCondition beEqual() {
        this.operator = OP_EQ;
        return this;
    }

    /**
     * 设置为不等于操作符
     *
     * @return 自身实例
     */
    public CompareCondition beNotEqual() {
        this.operator = OP_NEQ;
        return this;
    }

    /**
     * 设置为空安全等于操作符
     *
     * @return 自身实例
     */
    public CompareCondition beEqualNullSafe() {
        this.operator = OP_NULL_SAFE_EQ;
        return this;
    }

    /**
     * 设置为大于操作符
     *
     * @return 自身实例
     */
    public CompareCondition beGreaterThan() {
        this.operator = OP_GT;
        return this;
    }

    /**
     * 设置为大于等于操作符
     *
     * @return 自身实例
     */
    public CompareCondition beEqualOrGreaterThan() {
        this.operator = OP_EGT;
        return this;
    }

    /**
     * 设置为小于操作符
     *
     * @return 自身实例
     */
    public CompareCondition beLessThan() {
        this.operator = OP_LT;
        return this;
    }

    /**
     * 设置为小于等于操作符
     *
     * @return 自身实例
     */
    public CompareCondition beEqualOrLessThan() {
        this.operator = OP_ELT;
        return this;
    }

    /**
     * 取反操作符
     *
     * @return 自身实例
     */
    public CompareCondition not() {
        this.inverseOperator = true;
        return this;
    }

    /**
     * 设置比较表达式（左操作数）
     *
     * @param leftSide 左操作数
     * @return 自身实例
     */
    public CompareCondition compareExpression(Object leftSide) {
        this.leftSide = leftSide.toString();
        return this;
    }

    /**
     * 设置比较值（左操作数）
     *
     * @param leftSide 左操作数
     * @return 自身实例
     */
    public CompareCondition compareValue(@Nullable Object leftSide) {
        this.leftSide = String.valueOf(new Quoter(String.valueOf(leftSide)));
        return this;
    }

    /**
     * 设置操作符
     *
     * @param operator 操作符
     * @return 自身实例
     */
    public CompareCondition operator(String operator) {
        this.operator = operator;
        return this;
    }

    /**
     * 设置比较表达式（右操作数）
     *
     * @param rightSide 右操作数
     * @return 自身实例
     */
    public CompareCondition againstExpression(String rightSide) {
        this.rightSide = rightSide;
        return this;
    }

    /**
     * 设置字面量值（右操作数）
     *
     * @param rightSide 右操作数
     * @return 自身实例
     */
    public CompareCondition againstLiteralValue(@Nullable Object rightSide) {
        this.rightSide = String.valueOf(new Quoter(String.valueOf(rightSide)));
        return this;
    }

    /**
     * 设置数值（右操作数）
     *
     * @param rightSide 右操作数
     * @return 自身实例
     */
    public CompareCondition againstNumericValue(Number rightSide) {
        if (rightSide instanceof BigDecimal) {
            this.rightSide = ((BigDecimal) rightSide).toPlainString();
        } else {
            this.rightSide = rightSide.toString();
        }
        return this;
    }

    /**
     * 设置为NULL比较
     *
     * @return 自身实例
     */
    public CompareCondition isNull() {
        this.operator = OP_IS;
        this.rightSide = "NULL";
        return this;
    }

    /**
     * 设置为TRUE比较
     *
     * @return 自身实例
     */
    public CompareCondition isTrue() {
        this.operator = OP_IS;
        this.rightSide = "TRUE";
        return this;
    }

    /**
     * 设置为FALSE比较
     *
     * @return 自身实例
     */
    public CompareCondition isFalse() {
        this.operator = OP_IS;
        this.rightSide = "FALSE";
        return this;
    }

    /**
     * 设置为UNKNOWN比较
     *
     * @return 自身实例
     */
    public CompareCondition isUnknown() {
        this.operator = OP_IS;
        this.rightSide = "UNKNOWN";
        return this;
    }

    /**
     * 设置为包含比较（LIKE）
     *
     * @param rightSide 包含的字符串
     * @return 自身实例
     */
    public CompareCondition contains(String rightSide) {
        this.operator = OP_LIKE;
        String x = Quoter.escapeStringWithWildcards(rightSide);
        this.rightSide = "'%" + x + "%'";
        return this;
    }

    /**
     * 设置为前缀比较（LIKE）
     *
     * @param rightSide 前缀字符串
     * @return 自身实例
     */
    public CompareCondition hasPrefix(String rightSide) {
        this.operator = "like";
        String x = Quoter.escapeStringWithWildcards(rightSide);
        this.rightSide = "'" + x + "%'";
        return this;
    }

    /**
     * 设置为后缀比较（LIKE）
     *
     * @param rightSide 后缀字符串
     * @return 自身实例
     */
    public CompareCondition hasSuffix(String rightSide) {
        this.operator = "like";
        String x = Quoter.escapeStringWithWildcards(rightSide);
        this.rightSide = "'%" + x + "'";
        return this;
    }

    /**
     * 设置表达式等于字面量值的快捷方法
     *
     * @param expression 表达式
     * @param value      值
     * @return 自身实例
     */
    public CompareCondition expressionEqualsLiteralValue(String expression, @Nullable Object value) {
        if (value == null) {
            return this.compareExpression(expression).isNull();
        }
        return this
                .compareExpression(expression)
                .beEqual()
                .againstLiteralValue(value);
    }

    /**
     * 设置表达式等于数值值的快捷方法
     *
     * @param expression 表达式
     * @param value      数值
     * @return 自身实例
     */
    public CompareCondition expressionEqualsNumericValue(String expression, Number value) {
        return this
                .compareExpression(expression)
                .beEqual()
                .againstNumericValue(value);
    }

    /**
     * 生成SQL的条件表达式文本
     */
    @Override
    public String toString() {
        String x = leftSide + " " + operator + " " + rightSide;
        if (inverseOperator) {
            x = "NOT (" + x + ")";
        }
        return x;
    }
}
