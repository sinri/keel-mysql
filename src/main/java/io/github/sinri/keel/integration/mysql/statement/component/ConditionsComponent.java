package io.github.sinri.keel.integration.mysql.statement.component;

import io.github.sinri.keel.core.utils.value.ValueBox;
import io.github.sinri.keel.integration.mysql.condition.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;


/**
 * @since 1.9 all the callback function could return null safely. by Sinri 2020-02-07
 */
public class ConditionsComponent {
    protected final List<MySQLCondition> conditions;

    public ConditionsComponent() {
        conditions = new ArrayList<>();
    }

    public ConditionsComponent(@NotNull ConditionsComponent another) {
        this.conditions = new ArrayList<>(another.conditions);
    }

    public boolean isEmpty() {
        return this.conditions.isEmpty();
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @param value      Be quoted, number, string.
     * @since 3.1.8
     */
    public ConditionsComponent expressionEqualsLiteralValue(@NotNull String expression, @NotNull Object value) {
        return this.comparison(compareCondition -> compareCondition
                .compareExpression(expression)
                .operator(CompareCondition.OP_EQ)
                .againstLiteralValue(value)
        );
    }

    /**
     * @since 4.1.0
     */
    public ConditionsComponent expressionEqualsLiteralValueIfNonNull(@NotNull String expression, @NotNull ValueBox<Object> valueBox) {
        if (valueBox.isValueSetAndNotNull()) {
            return expressionEqualsLiteralValue(expression, valueBox.getNonNullValue());
        } else {
            return this;
        }
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @param value      Be quoted after stringify. BigDecimal would not be plain string.
     * @since 3.1.8
     */
    public ConditionsComponent expressionNotLiteralValue(@NotNull String expression, @NotNull Object value) {
        return this.comparison(compareCondition -> compareCondition
                .compareExpression(expression)
                .operator(CompareCondition.OP_NEQ)
                .againstLiteralValue(value)
        );
    }

    /**
     * @since 4.1.0
     */
    public ConditionsComponent expressionNotLiteralValueIfNonNull(@NotNull String expression, @NotNull ValueBox<Object> valueBox) {
        if (valueBox.isValueSetAndNotNull()) {
            return expressionNotLiteralValue(expression, valueBox.getNonNullValue());
        } else {
            return this;
        }
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @param value      Be quoted, numeric.
     * @since 3.1.8
     */
    public ConditionsComponent expressionEqualsNumericValue(@NotNull String expression, @NotNull Number value) {
        return this.comparison(compareCondition -> compareCondition
                .compareExpression(expression)
                .operator(CompareCondition.OP_EQ)
                .againstNumericValue(value)
        );
    }

    /**
     * @since 4.1.0
     */
    public ConditionsComponent expressionEqualsNumericValueIfNonNull(@NotNull String expression, @NotNull ValueBox<Number> valueBox) {
        if (valueBox.isValueSetAndNotNull()) {
            return expressionEqualsNumericValue(expression, valueBox.getNonNullValue());
        } else {
            return this;
        }
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @param value      Be quoted, numeric. BigDecimal would not be plain string.
     * @since 3.1.8
     */
    public ConditionsComponent expressionNotNumericValue(@NotNull String expression, @NotNull Number value) {
        return this.comparison(compareCondition -> compareCondition
                .compareExpression(expression)
                .operator(CompareCondition.OP_NEQ)
                .againstNumericValue(value)
        );
    }

    public ConditionsComponent expressionNotNumericValueIfNonNull(@NotNull String expression, @NotNull ValueBox<Number> valueBox) {
        if (valueBox.isValueSetAndNotNull()) {
            return expressionNotNumericValue(expression, valueBox.getNonNullValue());
        } else {
            return this;
        }
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @since 3.1.8
     */
    public ConditionsComponent expressionIsNull(@NotNull String expression) {
        return this.comparison(compareCondition -> compareCondition
                .compareExpression(expression)
                .isNull()
        );
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @since 3.1.8
     */
    public ConditionsComponent expressionIsNotNull(@NotNull String expression) {
        return this.comparison(compareCondition -> compareCondition
                .compareExpression(expression)
                .not()
                .isNull()
        );
    }


    public ConditionsComponent comparison(@NotNull Function<CompareCondition, CompareCondition> function) {
        CompareCondition condition = function.apply(new CompareCondition());
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    public ConditionsComponent comparison(@NotNull String operator, @NotNull Function<CompareCondition, CompareCondition> function) {
        CompareCondition condition = function.apply(new CompareCondition(operator));
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @param values     Be quoted each, as number or string.
     * @since 3.1.8
     */
    public ConditionsComponent expressionAmongLiteralValues(@NotNull String expression, @NotNull Collection<?> values) {
        return this.among(amongstCondition -> amongstCondition
                .elementAsExpression(expression)
                .amongstLiteralValueList(values)
        );
    }

    /**
     * @since 4.1.0
     */
    public ConditionsComponent expressionAmongLiteralValuesIfNotEmpty(@NotNull String expression, @NotNull Collection<?> values) {
        if (values.isEmpty()) return this;
        return expressionAmongLiteralValues(expression, values);
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @param values     Be quoted each, as number or string.
     * @since 3.1.8
     */
    public ConditionsComponent expressionAmongNumericValues(@NotNull String expression, @NotNull Collection<? extends Number> values) {
        return this.among(amongstCondition -> amongstCondition
                .elementAsExpression(expression)
                .amongstNumericValueList(values)
        );
    }

    /**
     * @since 4.1.0
     */
    public ConditionsComponent expressionAmongNumericValuesIfNotEmpty(@NotNull String expression, @NotNull Collection<? extends Number> values) {
        if (values.isEmpty()) return this;
        return expressionAmongNumericValues(expression, values);
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @param values     Be quoted each, as number or string.
     * @since 3.1.8
     */
    public ConditionsComponent expressionNotInLiteralValues(@NotNull String expression, @NotNull Collection<?> values) {
        return this.among(amongstCondition -> amongstCondition
                .elementAsExpression(expression)
                .not()
                .amongstLiteralValueList(values)
        );
    }

    /**
     * @since 4.1.0
     */
    public ConditionsComponent expressionNotInLiteralValuesIfNotEmpty(@NotNull String expression, @NotNull Collection<?> values) {
        if (values.isEmpty()) return this;
        return expressionNotInLiteralValues(expression, values);
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @param values     Be quoted each, as number or string.
     * @since 3.1.8
     */
    public ConditionsComponent expressionNotInNumericValues(@NotNull String expression, @NotNull Collection<? extends Number> values) {
        return this.among(amongstCondition -> amongstCondition
                .elementAsExpression(expression)
                .not()
                .amongstNumericValueList(values)
        );
    }

    /**
     * @since 4.1.0
     */
    public ConditionsComponent expressionNotInNumericValuesIfNotEmpty(@NotNull String expression, @NotNull Collection<? extends Number> values) {
        if (values.isEmpty()) return this;
        return expressionNotInNumericValues(expression, values);
    }

    public ConditionsComponent among(@NotNull Function<AmongstCondition, AmongstCondition> function) {
        AmongstCondition condition = function.apply(new AmongstCondition());
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    public ConditionsComponent intersection(@NotNull Function<GroupCondition, GroupCondition> function) {
        GroupCondition condition = function.apply(new GroupCondition(GroupCondition.JUNCTION_FOR_AND));
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    public ConditionsComponent union(@NotNull Function<GroupCondition, GroupCondition> function) {
        GroupCondition condition = function.apply(new GroupCondition(GroupCondition.JUNCTION_FOR_OR));
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    public ConditionsComponent raw(@NotNull String raw) {
        if (!raw.isBlank()) {
            conditions.add(new RawCondition(raw));
        }
        return this;
    }

    @Override
    public String toString() {
        if (conditions.isEmpty()) return "";
        return String.join(" and ", conditions.stream().map(MySQLCondition::toString).toList());
    }
}
