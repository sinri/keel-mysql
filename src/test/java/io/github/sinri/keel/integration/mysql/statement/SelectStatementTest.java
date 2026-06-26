package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.integration.mysql.statement.impl.SelectStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SelectStatementTest {

    @Test
    void whereShouldSupportInSubquery() {
        SelectStatement subQuery = new SelectStatement()
                .columnAsExpression("id")
                .from("archived_user")
                .where(conditions -> conditions.expressionEqualsLiteralValue("status", "ACTIVE"));

        SelectStatement statement = new SelectStatement()
                .from("user")
                .where(conditions -> conditions.expressionInSubquery("id", subQuery));

        assertEquals(
                "SELECT * FROM user WHERE id IN (SELECT id FROM archived_user WHERE status = 'ACTIVE')",
                statement.toString()
        );
    }

    @Test
    void whereShouldSupportNotInSubquery() {
        SelectStatement subQuery = new SelectStatement()
                .columnAsExpression("user_id")
                .from("blacklist");

        SelectStatement statement = new SelectStatement()
                .from("user")
                .where(conditions -> conditions.expressionNotInSubquery("id", subQuery));

        assertEquals(
                "SELECT * FROM user WHERE id NOT IN (SELECT user_id FROM blacklist)",
                statement.toString()
        );
    }
}
