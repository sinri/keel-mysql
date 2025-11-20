package io.github.sinri.keel.integration.mysql.statement.templated;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;


/**
 * 模板参数映射类，专门用于将模板参数名称映射到相应的{@link TemplateArgument}实例
 * 此便利方法类提供了将不同类型的值（null、数字、字符串、表达式）绑定到
 * 给定参数名称的便捷方法。
 *
 * @since 5.0.0
 */
public class TemplateArgumentMapping extends HashMap<String, TemplateArgument> {
    /**
     * 绑定NULL值参数
     *
     * @param argumentName 参数名称
     * @return 自身实例
     */
    public TemplateArgumentMapping bindNull(@NotNull String argumentName) {
        this.put(argumentName, TemplateArgument.forNull());
        return this;
    }

    /**
     * 绑定数值参数
     * @param argumentName 参数名称
     * @param number 数值
     * @return 自身实例
     */
    public TemplateArgumentMapping bindNumber(@NotNull String argumentName, @NotNull Number number) {
        this.put(argumentName, TemplateArgument.forNumber(number));
        return this;
    }

    /**
     * 绑定多个数值参数
     * @param argumentName 参数名称
     * @param numbers 数值集合
     * @return 自身实例
     */
    public TemplateArgumentMapping bindNumbers(@NotNull String argumentName, @NotNull Collection<? extends Number> numbers) {
        this.put(argumentName, TemplateArgument.forNumbers(numbers));
        return this;
    }

    /**
     * 绑定字符串参数
     * @param argumentName 参数名称
     * @param string 字符串
     * @return 自身实例
     */
    public TemplateArgumentMapping bindString(@NotNull String argumentName, @NotNull String string) {
        this.put(argumentName, TemplateArgument.forString(string));
        return this;
    }

    /**
     * 绑定多个字符串参数
     * @param argumentName 参数名称
     * @param strings 字符串集合
     * @return 自身实例
     */
    public TemplateArgumentMapping bindStrings(@NotNull String argumentName, @NotNull Collection<String> strings) {
        this.put(argumentName, TemplateArgument.forStrings(strings));
        return this;
    }

    /**
     * 绑定表达式参数
     * @param argumentName 参数名称
     * @param expression 表达式字符串
     * @return 自身实例
     */
    public TemplateArgumentMapping bindExpression(@NotNull String argumentName, @NotNull String expression) {
        this.put(argumentName, TemplateArgument.forExpression(expression));
        return this;
    }

    /**
     * 绑定多个表达式参数
     * @param argumentName 参数名称
     * @param expressions 表达式集合
     * @return 自身实例
     */
    public TemplateArgumentMapping bindExpressions(@NotNull String argumentName, @NotNull Collection<String> expressions) {
        this.put(argumentName, TemplateArgument.forExpressions(expressions));
        return this;
    }

    /**
     * 绑定行注释开始符号
     * @param argumentName 参数名称
     * @param commentFromHere 是否从此处开始注释
     * @return 自身实例
     */
    public TemplateArgumentMapping bindLineCommentStarting(@NotNull String argumentName, boolean commentFromHere) {
        this.put(argumentName, TemplateArgument.forExpression((commentFromHere ? "-- " : " ")));
        return this;
    }
}
