package io.github.sinri.keel.integration.mysql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuoterTest {
    @Test
    void escapeStringShouldKeepBaselineBackslashEscaping() {
        assertEquals("a\\\\b", Quoter.escapeString("a\\b"));
        assertEquals("a\\'b", Quoter.escapeString("a'b"));
        assertEquals("a\\nb", Quoter.escapeString("a\nb"));
        assertEquals("'a\\'b'", new Quoter("a'b").toString());
    }

    @Test
    void escapeStringShouldUseQuoteDoublingWhenNoBackslashEscapes() {
        Quoter.EscapeContext context = new Quoter.EscapeContext(
                "utf8mb4",
                "STRICT_TRANS_TABLES,NO_BACKSLASH_ESCAPES"
        );

        assertEquals("a''b\\c", Quoter.escapeString("a'b\\c", context));
        assertEquals("'a''b\\c'", new Quoter("a'b\\c", context).toString());
    }

    @Test
    void escapeStringShouldRejectUnsafeCharsetForBackslashEscaping() {
        assertFalse(Quoter.isBackslashEscapingSafeCharset("gbk"));
        assertFalse(Quoter.isBackslashEscapingSafeCharset("Big5"));
        assertTrue(Quoter.isBackslashEscapingSafeCharset("utf8mb4"));

        Quoter.EscapeContext context = new Quoter.EscapeContext("gbk", "");
        assertThrows(IllegalArgumentException.class, () -> Quoter.escapeString("a'b", context));
    }

    @Test
    void noBackslashEscapesShouldAllowUnsafeCharsetWithoutBackslashStringEscaping() {
        Quoter.EscapeContext context = new Quoter.EscapeContext("gbk", "NO_BACKSLASH_ESCAPES");

        assertEquals("a''b", Quoter.escapeString("a'b", context));
    }

    @Test
    void escapeStringWithWildcardsShouldAccountForSqlMode() {
        Quoter.EscapeContext noBackslashContext = new Quoter.EscapeContext("utf8mb4", "NO_BACKSLASH_ESCAPES");

        assertEquals("a\\\\%b\\\\_c", Quoter.escapeStringWithWildcards("a%b_c"));
        assertEquals("a\\%b\\_c", Quoter.escapeStringWithWildcards("a%b_c", noBackslashContext));
    }
}
