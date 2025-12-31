package io.github.sinri.keel.integration.mysql.condition;

import io.github.sinri.keel.integration.mysql.Quoter;
import io.github.sinri.keel.integration.mysql.exception.KeelSQLGenerateError;
import io.github.sinri.keel.integration.mysql.statement.mixin.ReadStatementMixin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * 范围条件类，用于构建IN/NOT IN类型的SQL条件表达式
 *
 * @since 5.0.0
 */
public class AmongstCondition implements MySQLCondition {
    public static final String OP_IN = "IN";
    protected final @NotNull List<@NotNull String> targetSet;
    protected String element;
    protected boolean inverseOperator;

    public AmongstCondition() {
        this.inverseOperator = false;
        this.targetSet = new ArrayList<>();
    }

    public @NotNull AmongstCondition not() {
        this.inverseOperator = true;
        return this;
    }

    public @NotNull AmongstCondition elementAsExpression(@NotNull String element) {
        this.element = element;
        return this;
    }

    public @NotNull AmongstCondition elementAsValue(@Nullable String element) {
        this.element = new Quoter(element).toString();
        return this;
    }

    public @NotNull AmongstCondition elementAsValue(@Nullable Number element) {
        this.element = new Quoter(element).toString();
        return this;
    }

    public @NotNull AmongstCondition amongstLiteralValueList(@NotNull Iterable<?> targets) {
        for (Object next : targets) {
            this.amongstLiteralValue(next);
        }
        return this;
    }

    public @NotNull AmongstCondition amongstNumericValueList(@NotNull Iterable<? extends Number> targets) {
        for (Number next : targets) {
            this.amongstNumericValue(next);
        }
        return this;
    }

    protected @NotNull AmongstCondition amongstLiteralValue(@Nullable Object value) {
        if (value == null) {
            this.targetSet.add("NULL");
        } else {
            this.targetSet.add(new Quoter(String.valueOf(value)).toString());
        }
        return this;
    }


    protected @NotNull AmongstCondition amongstNumericValue(@Nullable Number value) {
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

    protected @NotNull AmongstCondition amongstExpression(@NotNull String value) {
        this.targetSet.add(Objects.requireNonNull(value));
        return this;
    }

    public @NotNull AmongstCondition amongstExpressionList(@NotNull List<@NotNull String> values) {
        values.forEach(x -> this.amongstExpression(Objects.requireNonNull(x)));
        return this;
    }

    /**
     * @param readStatement A READ Statement, such as SELECT.
     */
    public @NotNull AmongstCondition amongstReadStatement(@NotNull ReadStatementMixin readStatement) {
        return this.amongstExpression(readStatement.toString());
    }

    /**
     * 生成SQL的比较条件表达式文本。如果出错，则抛出 KeelSQLGenerateError 异常。
     *
     * @throws KeelSQLGenerateError sql generate error
     */
    @Override
    public @NotNull String toString() {
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
