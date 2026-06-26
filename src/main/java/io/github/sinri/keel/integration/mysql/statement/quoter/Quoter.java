package io.github.sinri.keel.integration.mysql.statement.quoter;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 将各种类型的值转换为安全的 MySQL 语法下的 Expression 构成格式。
 */
@TechnicalPreview(since = "5.0.4")
public final class Quoter {
    private final MySQLEscapeContext escapeContext;

    public Quoter(MySQLEscapeContext escapeContext) {
        this.escapeContext = Objects.requireNonNull(escapeContext, "escapeContext");
    }

    public Quoter() {
        this(MySQLEscapeContext.DEFAULT);
    }

    public String quoteLiteral(String value) {
        return quoteEscapedString(escapeString(value));
    }

    public String quoteLikePattern(String value) {
        return quoteEscapedString(escapeString(escapeLikeWildcards(value)));
    }

    /**
     * 构造 LIKE 的 contains 模式：{@code '%escaped%'}。
     *
     * @param value 匹配子串
     * @return 带 {@code %} 通配符前后缀的 LIKE 模式字面量
     */
    public String quoteLikeContains(String value) {
        return "'%" + escapeString(escapeLikeWildcards(value)) + "%'";
    }

    /**
     * 构造 LIKE 的前缀模式：{@code 'escaped%'}。
     *
     * @param value 匹配前缀
     * @return 带 {@code %} 通配符后缀的 LIKE 模式字面量
     */
    public String quoteLikePrefix(String value) {
        return "'" + escapeString(escapeLikeWildcards(value)) + "%'";
    }

    /**
     * 构造 LIKE 的后缀模式：{@code '%escaped'}。
     *
     * @param value 匹配后缀
     * @return 带 {@code %} 通配符前缀的 LIKE 模式字面量
     */
    public String quoteLikeSuffix(String value) {
        return "'%" + escapeString(escapeLikeWildcards(value)) + "'";
    }

    public String quoteNull() {
        return "NULL";
    }

    public String quoteNumeric(Number number) {
        return Objects.requireNonNull(number, "number").toString();
    }

    public String quoteBoolean(boolean value) {
        return value ? "TRUE" : "FALSE";
    }


    public <T> String quoteList(List<T> list) {
        Objects.requireNonNull(list, "list");
        StringBuilder q = new StringBuilder();
        for (T item : list) {
            if (!q.isEmpty()) {
                q.append(",");
            }
            q.append(quoteValue(item));
        }
        return "(" + q + ")";
    }

    /**
     * 将任意值转换为 MySQL 表达式片段。
     * <p>
     * {@code null} 输出 {@link #quoteNull()}；{@link Number} 输出 {@link #quoteNumeric(Number)}；
     * {@link Boolean} 输出 {@link #quoteBoolean(boolean)}；其余按字符串字面量
     * {@link #quoteLiteral(String)} 处理。
     *
     * @param value 任意值
     * @return MySQL 表达式片段
     */
    public String quoteValue(@Nullable Object value) {
        if (value == null) {
            return quoteNull();
        }
        if (value instanceof Number number) {
            return quoteNumeric(number);
        }
        if (value instanceof Boolean bool) {
            return quoteBoolean(bool);
        }
        return quoteLiteral(value.toString());
    }

    private static String escapeLikeWildcards(String value) {
        return value.replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
    }

    private String escapeString(String value) {
        Objects.requireNonNull(value, "value");
        if (escapeContext.usesNoBackslashEscapes()) {
            return value.replace("'", "''");
        }
        validateBackslashEscapingSafeCharset(escapeContext.characterSet());
        return value.replace("\\", "\\\\")
                    .replace("\b", "\\b")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
                    .replace(new String(Character.toChars(26)), "\\Z")
                    .replace(new String(Character.toChars(0)), "\\0")
                    .replace("'", "\\'")
                    .replace("\"", "\\\"");
    }

    private String quoteEscapedString(String escaped) {
        return "'" + escaped + "'";
    }

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

    public static void validateBackslashEscapingSafeCharset(@Nullable String characterSet) {
        if (!isBackslashEscapingSafeCharset(characterSet)) {
            throw new IllegalArgumentException(
                    "MySQL character_set_connection is not safe for backslash string escaping: " + characterSet);
        }
    }
}
