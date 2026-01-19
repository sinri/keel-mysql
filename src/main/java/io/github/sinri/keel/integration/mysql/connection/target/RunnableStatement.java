package io.github.sinri.keel.integration.mysql.connection;

import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.github.sinri.keel.integration.mysql.statement.MySQLAuditSpecificLog;
import io.github.sinri.keel.integration.mysql.statement.StatementAuditorHolder;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

/**
 * 可执行的 SQL 语句
 * <p>
 * Consisted by a certain {@link AnyStatement} and a late {@link SqlConnection}.
 *
 * @since 5.0.0
 */
@NullMarked
public final class RunnableStatement {
    private final AnyStatement<?> statement;
    private final LateObject<SqlConnection> lateConnection;
    private final String runnableStatementUUID;

    public RunnableStatement(AnyStatement<?> statement) {
        this.statement = statement;
        this.runnableStatementUUID = UUID.randomUUID().toString();
        this.lateConnection = new LateObject<>();
    }

    private SqlConnection getSqlConnection() {
        return lateConnection.get();
    }

    public RunnableStatement setSQLConnection(SqlConnection connection) {
        lateConnection.set(connection);
        return this;
    }

    public Future<ResultMatrix> execute() {
        String sql = statement.buildSql();
        boolean toPrepareStatement = statement.isToPrepareStatement();
        return Future.succeededFuture()
                     .compose(v -> {
                         if (!toPrepareStatement) {
                             getSqlAuditLogger().info(r -> r.setQuery(runnableStatementUUID, sql));
                             return getSqlConnection().query(sql).execute();
                         } else {
                             getSqlAuditLogger().info(r -> r.setPreparation(runnableStatementUUID, sql));
                             return getSqlConnection().preparedQuery(sql).execute();
                         }
                     })
                     .compose(rows -> {
                         ResultMatrix resultMatrix = ResultMatrix.create(rows);
                         return Future.succeededFuture(resultMatrix);
                     })
                     .compose(resultMatrix -> {
                         getSqlAuditLogger().info(r -> r.setForDone(runnableStatementUUID, sql, resultMatrix.getTotalAffectedRows(), resultMatrix.getTotalFetchedRows()));
                         return Future.succeededFuture(resultMatrix);
                     }, throwable -> {
                         getSqlAuditLogger().error(r -> r.setForFailed(runnableStatementUUID, sql)
                                                         .exception(throwable));
                         return Future.failedFuture(throwable);
                     });
    }

    private SpecificLogger<MySQLAuditSpecificLog> getSqlAuditLogger() {
        return StatementAuditorHolder.getInstance().getSqlAuditLogger();
    }
}
