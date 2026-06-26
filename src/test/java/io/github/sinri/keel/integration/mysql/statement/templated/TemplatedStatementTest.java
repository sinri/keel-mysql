package io.github.sinri.keel.integration.mysql.statement.templated;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplatedStatementTest {
    @Test
    void forStringShouldEscapeStringLiteral() {
        TemplateArgument argument = TemplateArgument.forString("don't {name}");

        assertEquals("'don\\'t {name}'", argument.toString());
    }

    @Test
    void bindStringShouldRenderEscapedLiteralAtExpressionPlaceholder() {
        TemplatedReadStatement statement = new TemplatedReadStatement(
                "SELECT * FROM user WHERE name = {name} AND remark = {remark}"
        ).bindArguments(arguments -> arguments
                .bindString("name", "O'Reilly")
                .bindString("remark", "don't {inject}")
        );

        assertEquals(
                "SELECT * FROM user WHERE name = 'O\\'Reilly' AND remark = 'don\\'t {inject}'",
                statement.buildSql()
        );
    }
}
