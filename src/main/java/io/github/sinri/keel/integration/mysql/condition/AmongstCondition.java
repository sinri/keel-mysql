package io.github.sinri.keel.integration.mysql.condition;

import io.github.sinri.keel.integration.mysql.Quoter;
import io.github.sinri.keel.integration.mysql.exception.KeelSQLGenerateError;
import io.github.sinri.keel.integration.mysql.statement.mixin.ReadStatementMixin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * 范围条件类，用于构建IN/NOT IN类型的SQL条件表达式
 *
 * @since 5.0.0
 */
@NullMarked
public class AmongstCondition implements MySQLCondition {
    public static final String OP_IN = "IN";
    protected final List<String> targetSet;
    protected @Nullable String element;
    protected boolean inverseOperator;

    public AmongstCondition() {
        this.inverseOperator = false;
        this.targetSet = new ArrayList<>();
    }

    public AmongstCondition not() {
        this.inverseOperator = true;
        return this;
    }

    public AmongstCondition elementAsExpression(String element) {
        this.element = element;
        return this;
    }

    public AmongstCondition elementAsValue(@Nullable String element) {
        this.element = new Quoter(element).toString();
        return this;
    }

    public AmongstCondition elementAsValue(@Nullable Number element) {
        this.element = new Quoter(element).toString();
        return this;
    }

    public AmongstCondition amongstLiteralValueList(Iterable<?> targets) {
        for (Object next : targets) {
            this.amongstLiteralValue(next);
        }
        return this;
    }

    public AmongstCondition amongstNumericValueList(Iterable<? extends Number> targets) {
        for (Number next : targets) {
            this.amongstNumericValue(next);
        }
        return this;
    }

    protected AmongstCondition amongstLiteralValue(@Nullable Object value) {
        if (value == null) {
            this.targetSet.add("NULL");
        } else {
            this.targetSet.add(new Quoter(String.valueOf(value)).toString());
        }
        return this;
    }


    protected AmongstCondition amongstNumericValue(@Nullable Number value) {
        if (value == null) {
            this.targetSet.add("NULL");
        } else {
            if (value instanceof BigDecimal) {
                this.targetSet.add(((BigDecimal) value).toPlainString());
            } else {
                this.targetSet.add(value.toString());
            }
        }
        return this;
    }

    protected AmongstCondition amongstExpression(String value) {
        this.targetSet.add(Objects.requireNonNull(value));
        return this;
    }

    public AmongstCondition amongstExpressionList(List<String> values) {
        values.forEach(x -> this.amongstExpression(Objects.requireNonNull(x)));
        return this;
    }

    /**
     * @param readStatement A READ Statement, such as SELECT.
     */
    public AmongstCondition amongstReadStatement(ReadStatementMixin readStatement) {
        return this.amongstExpression(readStatement.toString());
    }

    /**
     * 生成SQL的比较条件表达式文本。如果出错，则抛出 KeelSQLGenerateError 异常。
     *
     * @throws KeelSQLGenerateError sql generate error
     */
    @Override
    public String toString() {
        if (targetSet.isEmpty()) {
            throw new KeelSQLGenerateError("AmongstCondition Target Set Empty");
        }

        String s = element;
        if (inverseOperator) {
            s += " NOT";
        }
        s += " " + OP_IN + " (" + String.join(",", targetSet) + ")";
        return s;
    }
}
