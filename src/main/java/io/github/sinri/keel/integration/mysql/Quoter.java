package io.github.sinri.keel.integration.mysql;


import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * MySQL引号处理类，用于将各种类型的数据转换为安全的MySQL引号格式
 * <p>
 * 待废弃：请改用 {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter}
 * 处理 MySQL 表达式字面量。
 *
 * @since 5.0.0
 * @deprecated 请改用 {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter}。
 */
@NullMarked
@Deprecated(since = "5.0.4")
public class Quoter {
    private final String quoted;

    /**
     * 构造字符串引号处理器
     * <p>
     * 请改用 {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter}：
     * {@code null} 使用 {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteNull()}，
     * {@code withWildcards == true} 使用
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteLikePattern(String)}，
     * 否则使用
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteLiteral(String)}。
     *
     * @param x             字符串值
     * @param withWildcards 是否包含通配符
     * @deprecated 请改用 {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter} 的
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteNull()}、
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteLikePattern(String)} 或
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteLiteral(String)}。
     */
    @Deprecated(since = "5.0.4")
    public Quoter(@Nullable String x, boolean withWildcards) {
        if (x == null) {
            quoted = "NULL";
        } else {
            if (withWildcards) {
                quoted = quoteEscapedString(escapeStringWithWildcards(x));
            } else {
                quoted = quoteEscapedString(escapeString(x));
            }
        }
    }

    /**
     * 构造数字引号处理器
     * <p>
     * 请改用 {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter}：
     * {@code null} 使用 {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteNull()}，
     * 否则使用
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteNumeric(Number)}。
     *
     * @param number 数字值
     * @deprecated 请改用 {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter} 的
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteNull()} 或
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteNumeric(Number)}。
     */
    @Deprecated(since = "5.0.4")
    public Quoter(@Nullable Number number) {
        if (number == null) {
            quoted = "NULL";
        } else {
            quoted = number.toString();
        }
    }

    /**
     * 构造布尔值引号处理器
     * <p>
     * 请改用
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteBoolean(boolean)}。
     *
     * @param b 布尔值
     * @deprecated 请改用
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteBoolean(boolean)}。
     */
    @Deprecated(since = "5.0.4")
    public Quoter(boolean b) {
        if (b)
            quoted = "TRUE";
        else
            quoted = "FALSE";
    }

    /**
     * 构造字符串引号处理器
     * <p>
     * 请改用 {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter}：
     * {@code null} 使用 {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteNull()}，
     * 否则使用
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteLiteral(String)}。
     *
     * @param s 字符串值
     * @deprecated 请改用 {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter} 的
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteNull()} 或
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteLiteral(String)}。
     */
    @Deprecated(since = "5.0.4")
    public Quoter(@Nullable String s) {
        if (s == null) {
            quoted = "NULL";
        } else {
            // if (y instanceof String) or else
            quoted = quoteEscapedString(escapeString(s));
        }
    }

    /**
     * 构造列表引号处理器
     * <p>
     * 请改用
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteList(List)}。
     *
     * @param list 列表
     * @deprecated 请改用
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteList(List)}。
     */
    @Deprecated(since = "5.0.4")
    public Quoter(List<?> list) {
        StringBuilder q = new StringBuilder();
        for (Object y : list) {
            if (!q.isEmpty()) {
                q.append(",");
            }
            if (y instanceof Number) {
                q.append(new Quoter((Number) y));
            } else {
                q.append(new Quoter(y.toString()));
            }
        }
        quoted = "(" + q + ")";
    }

    /**
     * 转义字符串中的特殊字符
     * <p>
     * 若目标是生成可直接拼入 SQL 的字符串字面量，请改用
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteLiteral(String)}。
     * <p>
     * 新实现会按 {@link io.github.sinri.keel.integration.mysql.statement.quoter.MySQLEscapeContext}
     * 感知字符集与 {@code sql_mode}，并直接返回带引号的表达式片段。
     *
     * @param s 原始字符串
     * @return 转义后的字符串
     * @deprecated 请改用
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteLiteral(String)}。
     */
    @Deprecated(since = "5.0.4")
    public static String escapeString(String s) {
        return s.replace("\\", "\\\\")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace(new String(Character.toChars(26)), "\\Z")
                .replace(new String(Character.toChars(0)), "\\0")
                .replace("'", "\\'")
                .replace("\"", "\\\"");
    }

    /**
     * 转义字符串中的特殊字符和通配符
     * <p>
     * 若目标是生成 LIKE 模式字面量，请改用
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteLikePattern(String)}。
     * <p>
     * 若仍需在外部自行拼接 {@code '%'} 前缀或后缀，可先调用
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteLikePattern(String)}，
     * 再按需组合前后缀。
     *
     * @param s 原始字符串
     * @return 转义后的字符串
     * @deprecated 请改用
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteLikePattern(String)}。
     */
    @Deprecated(since = "5.0.4")
    public static String escapeStringWithWildcards(String s) {
        return escapeString(s)
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    /**
     * 为转义后的字符串添加引号
     * <p>
     * 若输入仍是原始值，请改用
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteLiteral(String)}，
     * 由新实现统一完成转义与加引号。
     * <p>
     * 旧用法 {@code quoteEscapedString(escapeString(s))} 应整体替换为
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteLiteral(String)}。
     *
     * @param s 转义后的字符串
     * @return 带引号的字符串
     * @deprecated 请改用
     * {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter#quoteLiteral(String)}。
     */
    @Deprecated(since = "5.0.4")
    public static String quoteEscapedString(String s) {
        return "'" + s + "'";
    }

    /**
     * 返回构造时生成的 MySQL 表达式片段。
     * <p>
     * 请直接调用 {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter} 上对应的
     * {@code quote*} 方法获取结果，而不再通过“构造后 {@code toString()}”间接取值。
     *
     * @return MySQL 表达式片段
     * @deprecated 请改用 {@link io.github.sinri.keel.integration.mysql.statement.quoter.Quoter} 的
     * {@code quote*} 方法直接获取表达式字符串。
     */
    @Deprecated(since = "5.0.4")
    @Override
    public String toString() {
        return quoted;
    }
}
