package io.github.sinri.keel.integration.mysql.statement.quoter;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import org.jspecify.annotations.Nullable;

@TechnicalPreview(since = "5.0.4")
public record MySQLEscapeContext(@Nullable String characterSet, @Nullable String sqlMode) {
    public static final String SQL_MODE_NO_BACKSLASH_ESCAPES = "NO_BACKSLASH_ESCAPES";
    public static final MySQLEscapeContext DEFAULT = new MySQLEscapeContext("utf8mb4", null);

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
