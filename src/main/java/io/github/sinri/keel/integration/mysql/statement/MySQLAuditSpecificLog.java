package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.logger.api.log.SpecificLog;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;


/**
 * A specialized implementation of {@link SpecificLog} for recording MySQL audit issues.
 * This class provides methods to set the state of a MySQL query, including preparation, execution, and failure,
 * along with relevant attributes such as the SQL statement, statement UUID, and affected/fetched rows.
 *
 * @since 4.1.0
 */
public final class MySQLAuditSpecificLog extends SpecificLog<MySQLAuditSpecificLog> {
    public static final String TopicMysqlAudit = "MysqlAudit";
    public static final String AttributeMysqlAudit = "MysqlAudit";
    public static final String KeyStatementUuid = "statement_uuid";
    public static final String KeySql = "sql";
    public static final String KeyTotalAffectedRows = "TotalAffectedRows";
    public static final String KeyTotalFetchedRows = "TotalFetchedRows";

    public MySQLAuditSpecificLog() {
        super();
    }


    /**
     * Sets the preparation state for a MySQL query, including the statement UUID and the SQL query.
     *
     * @param statement_uuid The unique identifier for the prepared statement.
     * @param sql            The SQL query that was prepared.
     * @return The current instance of {@link MySQLAuditSpecificLog} for method chaining.
     */
    public MySQLAuditSpecificLog setPreparation(@NotNull String statement_uuid, @NotNull String sql) {
        this.message("MySQL query prepared.")
            .extra(AttributeMysqlAudit, new JsonObject()
                    .put(KeyStatementUuid, statement_uuid)
                    .put(KeySql, sql)
            );
        return this;
    }

    /**
     * Sets the query details for a MySQL audit issue, including the statement UUID and the SQL query.
     *
     * @param statement_uuid The unique identifier for the statement.
     * @param sql            The SQL query that was executed.
     * @return The current instance of {@link MySQLAuditSpecificLog} for method chaining.
     */
    public MySQLAuditSpecificLog setQuery(@NotNull String statement_uuid, @NotNull String sql) {
        this.message("MySQL query without preparation.")
            .extra(AttributeMysqlAudit, new JsonObject()
                    .put(KeyStatementUuid, statement_uuid)
                    .put(KeySql, sql)
            );
        return this;
    }


    /**
     * Sets the completion state for a MySQL query, including the statement UUID, SQL query, total affected rows,
     * and total fetched rows.
     *
     * @param statement_uuid    The unique identifier for the executed statement.
     * @param sql               The SQL query that was executed.
     * @param totalAffectedRows The number of rows affected by the query.
     * @param totalFetchedRows  The number of rows fetched by the query.
     * @return The current instance of {@link MySQLAuditSpecificLog} for method chaining.
     */
    public MySQLAuditSpecificLog setForDone(
            @NotNull String statement_uuid,
            @NotNull String sql,
            int totalAffectedRows,
            int totalFetchedRows
    ) {
        this.message("MySQL query executed.")
            .extra(AttributeMysqlAudit, new JsonObject()
                    .put(KeyStatementUuid, statement_uuid)
                    .put(KeySql, sql)
                    .put(KeyTotalFetchedRows, totalFetchedRows)
                    .put(KeyTotalAffectedRows, totalAffectedRows)
            );
        return this;
    }

    /**
     * Sets the failed state for a MySQL query, including the statement UUID and the SQL query.
     *
     * @param statement_uuid The unique identifier for the statement that failed.
     * @param sql            The SQL query that was executed and failed.
     * @return The current instance of {@link MySQLAuditSpecificLog} for method chaining.
     */
    public MySQLAuditSpecificLog setForFailed(@NotNull String statement_uuid, @NotNull String sql) {
        this.message("MySQL query failed.")
            .extra(AttributeMysqlAudit, new JsonObject()
                    .put(KeyStatementUuid, statement_uuid)
                    .put(KeySql, sql)
            );
        return this;
    }
}
