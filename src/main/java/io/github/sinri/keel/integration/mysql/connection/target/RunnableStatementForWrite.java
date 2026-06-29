package io.github.sinri.keel.integration.mysql.connection.target;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.integration.mysql.result.StatementExecuteResult;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.core.Future;
import io.vertx.sqlclient.Tuple;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public class RunnableStatementForWrite extends RunnableStatementForModify {
    public RunnableStatementForWrite(AnyStatement<?> statement) {
        super(statement);
    }

    /**
     * @return future with last inserted id; if any error occurs, failed future returned instead.
     */
    public Future<Long> executeForLastInsertedID() {
        return execute()
                .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getLastInsertedID()));
    }

    /**
     * Execute the current prepared SQL with a batch of tuple arguments.
     * <p>
     * This method is for parameterized write statements. For literal VALUES built
     * through {@code WriteIntoStatement}, callers may explicitly split the statement
     * with {@code WriteIntoStatement#divide(int)}.
     *
     * @param keel   async scheduler
     * @param tuples tuple arguments to execute as a prepared batch
     * @return statement execution result for the prepared batch
     */
    @TechnicalPreview(since = "5.0.4")
    public Future<StatementExecuteResult> execute(Keel keel, List<Tuple> tuples) {
        String sql = getStatement().buildSql();
        getSqlAuditLogger().info(r -> r.setPreparation(getUuid(), sql));
        return executeWithPreparedStatement(keel, sql, preparedStatement -> preparedStatement.query().executeBatch(tuples))
                .compose(rows -> {
                    StatementExecuteResult statementExecuteResult = new StatementExecuteResult(rows);
                    getSqlAuditLogger().info(r -> r.setForDone(
                            getUuid(),
                            sql,
                            statementExecuteResult.getTotalAffectedRows(),
                            statementExecuteResult.getTotalFetchedRows()
                    ));
                    return Future.succeededFuture(statementExecuteResult);
                }, throwable -> {
                    getSqlAuditLogger().error(r -> r.setForFailed(getUuid(), sql)
                            .exception(throwable));
                    return Future.failedFuture(throwable);
                });
    }
}
