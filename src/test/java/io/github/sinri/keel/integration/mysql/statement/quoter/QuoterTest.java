package io.github.sinri.keel.integration.mysql.statement.quoter;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuoterTest {
    @Test
    void quoteLiteralShouldUseBaselineBackslashEscaping() {
        Quoter quoter = new Quoter();

        assertEquals("'a\\\\b'", quoter.quoteLiteral("a\\b"));
        assertEquals("'a\\'b'", quoter.quoteLiteral("a'b"));
        assertEquals("'a\\nb'", quoter.quoteLiteral("a\nb"));
    }

    @Test
    void quoteLiteralShouldUseQuoteDoublingWhenNoBackslashEscapes() {
        Quoter quoter = new Quoter(new MySQLEscapeContext(
                "utf8mb4",
                "STRICT_TRANS_TABLES,NO_BACKSLASH_ESCAPES"
        ));

        assertEquals("'a''b\\c'", quoter.quoteLiteral("a'b\\c"));
    }

    @Test
    void quoteLiteralShouldRejectUnsafeCharsetForBackslashEscaping() {
        assertFalse(Quoter.isBackslashEscapingSafeCharset("gbk"));
        assertFalse(Quoter.isBackslashEscapingSafeCharset("Big5"));
        assertTrue(Quoter.isBackslashEscapingSafeCharset("utf8mb4"));

        Quoter quoter = new Quoter(new MySQLEscapeContext("gbk", ""));
        assertThrows(IllegalArgumentException.class, () -> quoter.quoteLiteral("a'b"));
    }

    @Test
    void quoteLikePatternShouldEscapeWildcardCharacters() {
        Quoter quoter = new Quoter();

        assertEquals("'a\\\\%b\\\\_c'", quoter.quoteLikePattern("a%b_c"));
    }

    @Test
    void quoteScalarsShouldRenderMysqlExpressions() {
        Quoter quoter = new Quoter();

        assertEquals("NULL", quoter.quoteNull());
        assertEquals("123", quoter.quoteNumeric(123));
        assertEquals("TRUE", quoter.quoteBoolean(true));
        assertEquals("FALSE", quoter.quoteBoolean(false));
    }

    @Test
    void quoteListShouldDispatchKnownValueTypes() {
        Quoter quoter = new Quoter();

        assertEquals("(NULL,123,TRUE,'a\\'b')", quoter.quoteList(Arrays.asList(null, 123, true, "a'b")));
    }
}
