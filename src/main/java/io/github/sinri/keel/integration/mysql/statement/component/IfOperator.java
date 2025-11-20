package io.github.sinri.keel.integration.mysql.statement.component;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.integration.mysql.Quoter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * IF函数操作符类，用于构建MySQL IF函数
 * If expr1 is TRUE (expr1 is not equal to 0 and expr1 IS NOT NULL), IF() returns expr2. Otherwise, it returns expr3.
 * {@code IF(expr1,expr2,expr3) }
 *
 * @since 5.0.0
 */
@TechnicalPreview(since = "3.0.19")
public class IfOperator {
    private String conditionExpression;
    private String expressionForNonEmpty;
    private String expressionForEmpty;

    public IfOperator() {

    }

    public IfOperator setConditionNumber(@NotNull Number conditionNumber) {
        this.conditionExpression = String.valueOf(conditionNumber);
        return this;
    }

    public IfOperator setConditionExpression(@NotNull String conditionExpression) {
        this.conditionExpression = conditionExpression;
        return this;
    }

    public IfOperator setNumberForNonEmpty(@Nullable Number numberForNonEmpty) {
        this.expressionForNonEmpty = String.valueOf(numberForNonEmpty);
        return this;
    }

    public IfOperator setStringForNonEmpty(@Nullable String stringForNonEmpty) {
        this.expressionForNonEmpty = new Quoter(stringForNonEmpty).toString();
        return this;
    }

    public IfOperator setNullForNonEmpty() {
        this.expressionForNonEmpty = "null";
        return this;
    }

    public IfOperator setExpressionForNonEmpty(@NotNull String expressionForNonEmpty) {
        this.expressionForNonEmpty = expressionForNonEmpty;
        return this;
    }

    public IfOperator setNumberForEmpty(@Nullable Number numberForEmpty) {
        this.expressionForEmpty = String.valueOf(numberForEmpty);
        return this;
    }

    public IfOperator setStringForEmpty(@Nullable String stringForEmpty) {
        this.expressionForEmpty = new Quoter(stringForEmpty).toString();
        return this;
    }

    public IfOperator setNullForEmpty() {
        this.expressionForEmpty = "null";
        return this;
    }

    public IfOperator setExpressionForEmpty(@NotNull String expressionForEmpty) {
        this.expressionForEmpty = expressionForEmpty;
        return this;
    }

    @Override
    public String toString() {
        return "IF(" + conditionExpression + "," + expressionForNonEmpty + "," + expressionForEmpty + ")";
    }
}
