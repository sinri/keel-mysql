package io.github.sinri.keel.integration.mysql;


import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * MySQL引号处理类，用于将各种类型的数据转换为安全的MySQL引号格式
 *
 * @since 5.0.0
 */
@NullMarked
public class Quoter {
    public static final String SQL_MODE_NO_BACKSLASH_ESCAPES = "NO_BACKSLASH_ESCAPES";
    private static final EscapeContext DEFAULT_ESCAPE_CONTEXT = new EscapeContext("utf8mb4", null);
    private final String quoted;

    /**
     * 构造字符串引号处理器。
     * <p>
     * 当已知 MySQL 会话字符集与 SQL 模式时，应优先使用此构造器以匹配服务端字面量解析行为。
     *
     * @param x             字符串值
     * @param withWildcards 是否包含通配符
     * @param escapeContext 转义上下文
     */
    public Quoter(@Nullable String x, boolean withWildcards, EscapeContext escapeContext) {
        Objects.requireNonNull(escapeContext, "escapeContext");
        if (x == null) {
            quoted = "NULL";
        } else if (withWildcards) {
            quoted = quoteEscapedString(escapeStringWithWildcards(x, escapeContext));
        } else {
            quoted = quoteEscapedString(escapeString(x, escapeContext));
        }
    }

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
     * 构造字符串引号处理器。
     *
     * @param s             字符串值
     * @param escapeContext 转义上下文
     */
    public Quoter(@Nullable String s, EscapeContext escapeContext) {
        Objects.requireNonNull(escapeContext, "escapeContext");
        if (s == null) {
            quoted = "NULL";
        } else {
            quoted = quoteEscapedString(escapeString(s, escapeContext));
        }
    }

    /**
     * 构造数字引号处理器
     *
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
     *
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
     *
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
     * 构造列表引号处理器。
     *
     * @param list          列表
     * @param escapeContext 转义上下文
     */
    public Quoter(List<?> list, EscapeContext escapeContext) {
        Objects.requireNonNull(escapeContext, "escapeContext");
        StringBuilder q = new StringBuilder();
        for (Object y : list) {
            if (!q.isEmpty()) {
                q.append(",");
            }
            if (y instanceof Number) {
                q.append(new Quoter((Number) y));
            } else {
                q.append(new Quoter(y.toString(), escapeContext));
            }
        }
        quoted = "(" + q + ")";
    }

    /**
     * 构造列表引号处理器
     *
     * @param list 列表
     */
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
     *
     * @param s 原始字符串
     * @return 转义后的字符串
     */
    public static String escapeString(String s) {
        return escapeString(s, DEFAULT_ESCAPE_CONTEXT);
    }

    /**
     * 按给定 MySQL 会话上下文转义字符串中的特殊字符。
     *
     * @param s             原始字符串
     * @param escapeContext 转义上下文
     * @return 转义后的字符串
     */
    public static String escapeString(String s, EscapeContext escapeContext) {
        Objects.requireNonNull(escapeContext, "escapeContext");
        if (escapeContext.usesNoBackslashEscapes()) {
            return s.replace("'", "''");
        }
        validateBackslashEscapingSafeCharset(escapeContext.characterSet());
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
     *
     * @param s 原始字符串
     * @return 转义后的字符串
     */
    public static String escapeStringWithWildcards(String s) {
        return escapeStringWithWildcards(s, DEFAULT_ESCAPE_CONTEXT);
    }

    /**
     * 按给定 MySQL 会话上下文转义字符串中的特殊字符和 LIKE 通配符。
     *
     * @param s             原始字符串
     * @param escapeContext 转义上下文
     * @return 转义后的字符串
     */
    public static String escapeStringWithWildcards(String s, EscapeContext escapeContext) {
        String escapedPattern = s.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return escapeString(escapedPattern, escapeContext);
    }

    /**
     * 判断给定字符集是否适合使用反斜杠字符串转义。
     *
     * @param characterSet 连接字符集
     * @return 是否安全
     */
    public static boolean isBackslashEscapingSafeCharset(@Nullable String characterSet) {
        if (characterSet == null || characterSet.isBlank()) {
            return true;
        }

        String normalized = characterSet.trim()
                                        .replace('-', '_')
                                        .toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "big5", "cp932", "gb18030", "gbk", "sjis", "shift_jis" -> false;
            default -> true;
        };
    }

    /**
     * 校验给定字符集是否适合使用反斜杠字符串转义。
     *
     * @param characterSet 连接字符集
     * @throws IllegalArgumentException 当字符集存在已知反斜杠转义歧义时
     */
    public static void validateBackslashEscapingSafeCharset(@Nullable String characterSet) {
        if (!isBackslashEscapingSafeCharset(characterSet)) {
            throw new IllegalArgumentException(
                    "MySQL character_set_connection is not safe for backslash string escaping: " + characterSet);
        }
    }

    /**
     * MySQL 字符串字面量转义上下文。
     * <p>
     * {@code characterSet} 对应 {@code @@session.character_set_connection}；
     * {@code sqlMode} 对应 {@code @@session.sql_mode}。
     *
     * @param characterSet 连接字符集
     * @param sqlMode      SQL模式
     */
    public record EscapeContext(@Nullable String characterSet, @Nullable String sqlMode) {
        public boolean usesNoBackslashEscapes() {
            if (sqlMode == null || sqlMode.isBlank()) {
                return false;
            }
            for (String mode : sqlMode.split(",")) {
                if (SQL_MODE_NO_BACKSLASH_ESCAPES.equalsIgnoreCase(mode.trim())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 为转义后的字符串添加引号
     *
     * @param s 转义后的字符串
     * @return 带引号的字符串
     */
    public static String quoteEscapedString(String s) {
        return "'" + s + "'";
    }

    @Override
    public String toString() {
        return quoted;
    }
}
