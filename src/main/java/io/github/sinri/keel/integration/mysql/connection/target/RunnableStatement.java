package io.github.sinri.keel.integration.mysql.connection.target;

import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;
import org.jspecify.annotations.NullMarked;

/**
 * 可执行的 SQL 语句
 * <p>
 * Consisted by a certain {@link AnyStatement} and a late {@link SqlConnection}.
 *
 * @since 5.0.0
 */
@NullMarked
public class RunnableStatement extends AnyStatementWithSqlConnection {

    public RunnableStatement(AnyStatement<?> statement) {
        super(statement);
    }

    public Future<ResultMatrix> execute() {
        String sql = getStatement().buildSql();
        boolean toPrepareStatement = getStatement().isToPrepareStatement();
        return Future.succeededFuture()
                     .compose(v -> {
                         if (!toPrepareStatement) {
                             getSqlAuditLogger().info(r -> r.setQuery(getUuid(), sql));
                             return getSqlConnection().query(sql).execute();
                         } else {
                             getSqlAuditLogger().info(r -> r.setPreparation(getUuid(), sql));
                             return getSqlConnection().preparedQuery(sql).execute();
                         }
                     })
                     .compose(rows -> {
                         ResultMatrix resultMatrix = ResultMatrix.create(rows);
                         return Future.succeededFuture(resultMatrix);
                     })
                     .compose(resultMatrix -> {
                         getSqlAuditLogger().info(r -> r.setForDone(getUuid(), sql, resultMatrix.getTotalAffectedRows(), resultMatrix.getTotalFetchedRows()));
                         return Future.succeededFuture(resultMatrix);
                     }, throwable -> {
                         getSqlAuditLogger().error(r -> r.setForFailed(getUuid(), sql)
                                                         .exception(throwable));
                         return Future.failedFuture(throwable);
                     });
    }
}
