package io.github.sinri.keel.integration.mysql.statement.component;

import io.github.sinri.keel.core.utils.value.ValueBox;
import io.github.sinri.keel.integration.mysql.condition.*;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;


/**
 * 条件组件类，用于构建复杂的SQL条件表达式
 *
 * @since 5.0.0
 */
@NullMarked
public class ConditionsComponent {
    protected final List<MySQLCondition> conditions;

    /**
     * 构造条件组件
     */
    public ConditionsComponent() {
        conditions = new ArrayList<>();
    }

    /**
     * 复制构造条件组件
     *
     * @param another 另一个条件组件
     */
    public ConditionsComponent(ConditionsComponent another) {
        this.conditions = new ArrayList<>(another.conditions);
    }

    /**
     * 检查是否为空
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return this.conditions.isEmpty();
    }

    /**
     * 添加表达式等于字面值的条件
     *
     * @param expression 表达式（不会被引用，可能是字段、函数等）
     * @param value      值（会被引用，数字或字符串）
     * @return 自身实例
     */
    public ConditionsComponent expressionEqualsLiteralValue(String expression, Object value) {
        return this.comparison(compareCondition -> compareCondition
                .compareExpression(expression)
                .operator(CompareCondition.OP_EQ)
                .againstLiteralValue(value)
        );
    }

    /**
     * 添加表达式等于字面值的条件（仅在值不为空时）
     *
     * @param expression 表达式
     * @param valueBox   值包装器
     * @return 自身实例
     */
    public ConditionsComponent expressionEqualsLiteralValueIfNonNull(String expression, ValueBox<Object> valueBox) {
        if (valueBox.isValueSetAndNotNull()) {
            return expressionEqualsLiteralValue(expression, valueBox.getNonNullValue());
        } else {
            return this;
        }
    }

    /**
     * 添加表达式不等于字面值的条件
     *
     * @param expression 表达式
     * @param value      值
     * @return 自身实例
     */
    public ConditionsComponent expressionNotLiteralValue(String expression, Object value) {
        return this.comparison(compareCondition -> compareCondition
                .compareExpression(expression)
                .operator(CompareCondition.OP_NEQ)
                .againstLiteralValue(value)
        );
    }

    /**
     * 添加表达式不等于字面值的条件（仅在值不为空时）
     *
     * @param expression 表达式
     * @param valueBox   值包装器
     * @return 自身实例
     */
    public ConditionsComponent expressionNotLiteralValueIfNonNull(String expression, ValueBox<Object> valueBox) {
        if (valueBox.isValueSetAndNotNull()) {
            return expressionNotLiteralValue(expression, valueBox.getNonNullValue());
        } else {
            return this;
        }
    }

    /**
     * 添加表达式等于数值条件的条件
     *
     * @param expression 表达式
     * @param value      数值
     * @return 自身实例
     */
    public ConditionsComponent expressionEqualsNumericValue(String expression, Number value) {
        return this.comparison(compareCondition -> compareCondition
                .compareExpression(expression)
                .operator(CompareCondition.OP_EQ)
                .againstNumericValue(value)
        );
    }

    /**
     * 添加表达式等于数值条件的条件（仅在值不为空时）
     *
     * @param expression 表达式
     * @param valueBox   值包装器
     * @return 自身实例
     */
    public ConditionsComponent expressionEqualsNumericValueIfNonNull(String expression, ValueBox<Number> valueBox) {
        if (valueBox.isValueSetAndNotNull()) {
            return expressionEqualsNumericValue(expression, valueBox.getNonNullValue());
        } else {
            return this;
        }
    }

    /**
     * 添加表达式不等于数值条件的条件
     *
     * @param expression 表达式
     * @param value      数值
     * @return 自身实例
     */
    public ConditionsComponent expressionNotNumericValue(String expression, Number value) {
        return this.comparison(compareCondition -> compareCondition
                .compareExpression(expression)
                .operator(CompareCondition.OP_NEQ)
                .againstNumericValue(value)
        );
    }

    /**
     * 添加表达式不等于数值条件的条件（仅在值不为空时）
     *
     * @param expression 表达式
     * @param valueBox   值包装器
     * @return 自身实例
     */
    public ConditionsComponent expressionNotNumericValueIfNonNull(String expression, ValueBox<Number> valueBox) {
        if (valueBox.isValueSetAndNotNull()) {
            return expressionNotNumericValue(expression, valueBox.getNonNullValue());
        } else {
            return this;
        }
    }

    /**
     * 添加表达式为NULL的条件
     *
     * @param expression 表达式
     * @return 自身实例
     */
    public ConditionsComponent expressionIsNull(String expression) {
        return this.comparison(compareCondition -> compareCondition
                .compareExpression(expression)
                .isNull()
        );
    }

    /**
     * 添加表达式不为NULL的条件
     *
     * @param expression 表达式
     * @return 自身实例
     */
    public ConditionsComponent expressionIsNotNull(String expression) {
        return this.comparison(compareCondition -> compareCondition
                .compareExpression(expression)
                .not()
                .isNull()
        );
    }

    /**
     * 添加比较条件
     *
     * @param function 比较条件构建函数
     * @return 自身实例
     */
    public ConditionsComponent comparison(Function<CompareCondition, @Nullable CompareCondition> function) {
        CompareCondition condition = function.apply(new CompareCondition());
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    /**
     * 添加带操作符的比较条件
     *
     * @param operator 操作符
     * @param function 比较条件构建函数
     * @return 自身实例
     */
    public ConditionsComponent comparison(String operator, Function<CompareCondition, @Nullable CompareCondition> function) {
        CompareCondition condition = function.apply(new CompareCondition(operator));
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    /**
     * 添加表达式在字面值列表中的条件
     *
     * @param expression 表达式
     * @param values     值列表
     * @return 自身实例
     */
    public ConditionsComponent expressionAmongLiteralValues(String expression, Collection<?> values) {
        return this.among(amongstCondition -> amongstCondition
                .elementAsExpression(expression)
                .amongstLiteralValueList(values)
        );
    }

    /**
     * 添加表达式在字面值列表中的条件（非空检查）
     *
     * @param expression 表达式
     * @param values     值列表
     * @return 自身实例
     */
    public ConditionsComponent expressionAmongLiteralValuesIfNotEmpty(String expression, Collection<?> values) {
        if (values.isEmpty()) return this;
        return expressionAmongLiteralValues(expression, values);
    }

    /**
     * 添加表达式在数值列表中的条件
     *
     * @param expression 表达式
     * @param values     数值列表
     * @return 自身实例
     */
    public ConditionsComponent expressionAmongNumericValues(String expression, Collection<? extends Number> values) {
        return this.among(amongstCondition -> amongstCondition
                .elementAsExpression(expression)
                .amongstNumericValueList(values)
        );
    }

    /**
     * 添加表达式在数值列表中的条件（非空检查）
     *
     * @param expression 表达式
     * @param values     数值列表
     * @return 自身实例
     */
    public ConditionsComponent expressionAmongNumericValuesIfNotEmpty(String expression, Collection<? extends Number> values) {
        if (values.isEmpty()) return this;
        return expressionAmongNumericValues(expression, values);
    }

    /**
     * 添加表达式不在字面值列表中的条件
     *
     * @param expression 表达式
     * @param values     值列表
     * @return 自身实例
     */
    public ConditionsComponent expressionNotInLiteralValues(String expression, Collection<?> values) {
        return this.among(amongstCondition -> amongstCondition
                .elementAsExpression(expression)
                .not()
                .amongstLiteralValueList(values)
        );
    }

    /**
     * 添加表达式不在字面值列表中的条件（非空检查）
     *
     * @param expression 表达式
     * @param values     值列表
     * @return 自身实例
     */
    public ConditionsComponent expressionNotInLiteralValuesIfNotEmpty(String expression, Collection<?> values) {
        if (values.isEmpty()) return this;
        return expressionNotInLiteralValues(expression, values);
    }

    /**
     * 添加表达式不在数值列表中的条件
     *
     * @param expression 表达式
     * @param values     数值列表
     * @return 自身实例
     */
    public ConditionsComponent expressionNotInNumericValues(String expression, Collection<? extends Number> values) {
        return this.among(amongstCondition -> amongstCondition
                .elementAsExpression(expression)
                .not()
                .amongstNumericValueList(values)
        );
    }

    /**
     * 添加表达式不在数值列表中的条件（非空检查）
     *
     * @param expression 表达式
     * @param values     数值列表
     * @return 自身实例
     */
    public ConditionsComponent expressionNotInNumericValuesIfNotEmpty(String expression, Collection<? extends Number> values) {
        if (values.isEmpty()) return this;
        return expressionNotInNumericValues(expression, values);
    }

    /**
     * 添加包含条件
     *
     * @param function 包含条件构建函数
     * @return 自身实例
     */
    public ConditionsComponent among(Function<AmongstCondition, @Nullable AmongstCondition> function) {
        AmongstCondition condition = function.apply(new AmongstCondition());
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    /**
     * 添加交集条件（AND）
     *
     * @param function 组条件构建函数
     * @return 自身实例
     */
    public ConditionsComponent intersection(Function<GroupCondition, @Nullable GroupCondition> function) {
        GroupCondition condition = function.apply(new GroupCondition(GroupCondition.JUNCTION_FOR_AND));
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    /**
     * 添加并集条件（OR）
     *
     * @param function 组条件构建函数
     * @return 自身实例
     */
    public ConditionsComponent union(Function<GroupCondition, @Nullable GroupCondition> function) {
        GroupCondition condition = function.apply(new GroupCondition(GroupCondition.JUNCTION_FOR_OR));
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    /**
     * 添加原始条件
     *
     * @param raw 原始条件字符串
     * @return 自身实例
     */
    public ConditionsComponent raw(String raw) {
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
