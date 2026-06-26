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

    protected MySQLEscapeContext getEscapeContext() {
        return escapeContext;
    }

    public Quoter() {
        this(MySQLEscapeContext.DEFAULT);
    }

    public String quoteLiteral(String value) {
        return quoteEscapedString(escapeString(value));
    }

    public String quoteLikePattern(String value) {
        String escapedPattern = value.replace("\\", "\\\\")
                                     .replace("%", "\\%")
                                     .replace("_", "\\_");
        return quoteEscapedString(escapeString(escapedPattern));
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

    protected String quoteValue(@Nullable Object value) {
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

    protected String escapeString(String value) {
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

    protected String quoteEscapedString(String escaped) {
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
