package io.github.sinri.keel.integration.mysql.connection.target;

import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.github.sinri.keel.integration.mysql.statement.MySQLAuditSpecificLog;
import io.github.sinri.keel.integration.mysql.statement.StatementAuditorHolder;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.sqlclient.SqlConnection;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class AnyStatementWithSqlConnection {
    private final AnyStatement<?> statement;
    private final LateObject<SqlConnection> lateConnection;
    private final String uuid;

    public AnyStatementWithSqlConnection(AnyStatement<?> statement) {
        this.statement = statement;
        this.uuid = UUID.randomUUID().toString();
        this.lateConnection = new LateObject<>();
    }

    protected final SqlConnection getSqlConnection() {
        return lateConnection.get();
    }

    public final void setSQLConnection(SqlConnection connection) {
        lateConnection.set(connection);
    }

    protected final AnyStatement<?> getStatement() {
        return statement;
    }

    public final String getUuid() {
        return uuid;
    }

    protected final SpecificLogger<MySQLAuditSpecificLog> getSqlAuditLogger() {
        return StatementAuditorHolder.getInstance().getSqlAuditLogger();
    }
}
