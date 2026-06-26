package io.github.sinri.keel.integration.mysql;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("deprecation")
class QuoterTest {
    @Test
    void escapeStringShouldKeepBaselineBackslashEscaping() {
        assertEquals("a\\\\b", Quoter.escapeString("a\\b"));
        assertEquals("a\\'b", Quoter.escapeString("a'b"));
        assertEquals("a\\nb", Quoter.escapeString("a\nb"));
        assertEquals("'a\\'b'", new Quoter("a'b").toString());
    }

    @Test
    void escapeStringWithWildcardsShouldKeepBaselineBackslashEscaping() {
        assertEquals("a\\%b\\_c", Quoter.escapeStringWithWildcards("a%b_c"));
    }

    @Test
    void constructorsShouldKeepLegacyRendering() {
        assertEquals("NULL", new Quoter((String) null).toString());
        assertEquals("123", new Quoter(123).toString());
        assertEquals("TRUE", new Quoter(true).toString());
        assertEquals("('a\\'b',123)", new Quoter(List.of("a'b", 123)).toString());
    }
}
