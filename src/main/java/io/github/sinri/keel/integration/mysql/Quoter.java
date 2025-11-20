package io.github.sinri.keel.integration.mysql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * MySQL引号处理类，用于将各种类型的数据转换为安全的MySQL引号格式
 *
 * @since 5.0.0
 */
public class Quoter {
    private final @NotNull String quoted;

    /**
     * 构造字符串引号处理器
     *
     * @param x             字符串值
     * @param withWildcards 是否包含通配符
     */
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
     * @param number 数字值
     */
    public Quoter(@Nullable Number number) {
        if (number == null) {
            quoted = "NULL";
        } else {
            quoted = number.toString();
        }
    }

    /**
     * 构造布尔值引号处理器
     * @param b 布尔值
     */
    public Quoter(boolean b) {
        if (b)
            quoted = "TRUE";
        else
            quoted = "FALSE";
    }

    /**
     * 构造字符串引号处理器
     * @param s 字符串值
     */
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
     * @param list 列表
     */
    public Quoter(@NotNull List<?> list) {
        StringBuilder q = new StringBuilder();
        for (Object y : list) {
            if (q.length() > 0) {
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
     * @param s 原始字符串
     * @return 转义后的字符串
     */
    public static @NotNull String escapeString(@NotNull String s) {
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
     * @param s 原始字符串
     * @return 转义后的字符串
     */
    public static @NotNull String escapeStringWithWildcards(@NotNull String s) {
        return escapeString(s)
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    /**
     * 为转义后的字符串添加引号
     * @param s 转义后的字符串
     * @return 带引号的字符串
     */
    public static @NotNull String quoteEscapedString(@NotNull String s) {
        return "'" + s + "'";
    }

    @Override
    public String toString() {
        return quoted;
    }
}
