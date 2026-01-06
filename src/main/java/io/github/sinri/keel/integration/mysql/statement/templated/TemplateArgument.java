package io.github.sinri.keel.integration.mysql.statement.templated;


import io.github.sinri.keel.integration.mysql.Quoter;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * 模板参数类，用于定义SQL模板中的参数值
 *
 * @since 5.0.0
 */
@NullMarked
public class TemplateArgument {
    private final boolean asScalar;
    private final Collection<String> expressions;

    private TemplateArgument(String expression) {
        this.asScalar = true;
        this.expressions = List.of(expression);
    }

    private TemplateArgument(Collection<String> expressions) {
        this.asScalar = false;
        this.expressions = expressions;
    }

    /**
     * 创建表示NULL值的模板参数
     *
     * @return 模板参数实例
     */
    public static TemplateArgument forNull() {
        return forExpression("NULL");
    }

    /**
     * 创建表示数值的模板参数
     *
     * @param number 数值
     * @return 模板参数实例
     */
    public static TemplateArgument forNumber(Number number) {
        return forExpression(String.valueOf(number));
    }

    /**
     * 创建表示多个数值的模板参数
     *
     * @param numbers 数值集合
     * @return 模板参数实例
     */
    public static TemplateArgument forNumbers(Collection<? extends Number> numbers) {
        List<String> list = new ArrayList<>();
        numbers.forEach(number -> list.add(String.valueOf(number)));
        return forExpressions(list);
    }

    /**
     * 创建表示字符串的模板参数
     *
     * @param string 字符串
     * @return 模板参数实例
     */
    public static TemplateArgument forString(String string) {
        String s1 = Quoter.escapeString(string);
        //        System.out.println("S1 | "+s1);
        String s2 = Quoter.quoteEscapedString(s1);
        //        System.out.println("S2 | "+s2);
        return forExpression(s2);
    }

    /**
     * 创建表示多个字符串的模板参数
     *
     * @param strings 字符串集合
     * @return 模板参数实例
     */
    public static TemplateArgument forStrings(Collection<String> strings) {
        List<String> list = new ArrayList<>();
        strings.forEach(string -> list.add(Quoter.quoteEscapedString(Quoter.escapeString(string))));
        return forExpressions(list);
    }

    /**
     * 创建表示表达式的模板参数
     *
     * @param string 表达式字符串
     * @return 模板参数实例
     */
    public static TemplateArgument forExpression(String string) {
        return new TemplateArgument(string);
    }

    /**
     * 创建表示多个表达式的模板参数
     *
     * @param strings 表达式集合
     * @return 模板参数实例
     */
    public static TemplateArgument forExpressions(Collection<String> strings) {
        // if (strings.isEmpty()) throw new IllegalArgumentException();
        return new TemplateArgument(strings);
    }

    @Override
    public String toString() {
        if (this.asScalar) {
            for (String e : this.expressions) {
                return e;
            }
            throw new RuntimeException();
        } else {
            return "(" + String.join(",", expressions) + ")";
        }
    }
}
