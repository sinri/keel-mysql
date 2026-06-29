package io.github.sinri.keel.integration.mysql.connection.target;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.integration.mysql.result.StatementExecuteResult;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.core.Future;
import io.vertx.sqlclient.*;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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

    /**
     * 执行当前语句。
     * <p>
     * 默认使用预编译路径执行，但不自动绑定参数。若 SQL 中包含 {@code ?} 占位符，
     * 请调用 {@link #executeThroughPrepare(Tuple)} 并传入对应参数。
     *
     * @return 语句执行结果
     */
    public Future<StatementExecuteResult> execute() {
        return executeThroughPrepare();
    }

    /**
     * 通过普通查询协议执行当前语句。
     * <p>
     * 此方法不支持 {@code Tuple} 参数绑定，SQL 必须是可直接执行的完整字符串。
     *
     * @return 语句执行结果
     * @since 5.0.4
     */
    public Future<StatementExecuteResult> executeThroughQuery() {
        String sql = getStatement().buildSql();
        getSqlAuditLogger().info(r -> r.setQuery(getUuid(), sql));
        return getSqlConnection().query(sql).execute().compose(rows -> {
                    StatementExecuteResult result = new StatementExecuteResult(rows);
                    getSqlAuditLogger().info(r -> r.setForDone(
                            getUuid(),
                            sql,
                            result.getTotalAffectedRows(),
                            result.getTotalFetchedRows()
                    ));
                    return Future.succeededFuture(result);
                },
                throwable -> {
                    getSqlAuditLogger().error(r -> r.setForFailed(getUuid(), sql)
                                                    .exception(throwable));
                    return Future.failedFuture(throwable);
                });
    }

    /**
     * 通过预编译路径执行当前语句，并可绑定 {@code ?} 占位符参数。
     * <p>
     * 当 {@code tuple} 为 {@code null} 或空时，将按无参数预编译语句执行。
     *
     * @param tuple 绑定到 SQL 中 {@code ?} 占位符的参数
     * @return 语句执行结果
     * @since 5.0.4
     */
    public Future<StatementExecuteResult> executeThroughPrepare(@Nullable Tuple tuple) {
        String sql = getStatement().buildSql();
        getSqlAuditLogger().info(r -> r.setPreparation(getUuid(), sql));
        PreparedQuery<RowSet<Row>> rowSetPreparedQuery = getSqlConnection().preparedQuery(sql);
        return Future.succeededFuture().compose(v -> {
            if (tuple == null || tuple.size() == 0) {
                return rowSetPreparedQuery.execute();
            } else {
                return rowSetPreparedQuery.execute(tuple);
            }
        }).compose(rows -> {
            StatementExecuteResult result = new StatementExecuteResult(rows);
            getSqlAuditLogger().info(r -> r.setForDone(
                    getUuid(),
                    sql,
                    result.getTotalAffectedRows(),
                    result.getTotalFetchedRows()
            ));
            return Future.succeededFuture(result);
        }, throwable -> {
            getSqlAuditLogger().error(r -> r.setForFailed(getUuid(), sql)
                                            .exception(throwable));
            return Future.failedFuture(throwable);
        });
    }

    /**
     * 通过无参数预编译路径执行当前语句。
     * <p>
     * 若 SQL 中包含 {@code ?} 占位符，请改用 {@link #executeThroughPrepare(Tuple)}。
     *
     * @return 语句执行结果
     */
    public Future<StatementExecuteResult> executeThroughPrepare() {
        return executeThroughPrepare(null);
    }

    @TechnicalPreview(since = "5.0.4")
    protected <R> Future<R> executeWithPreparedStatement(Keel keel, String sql, Function<PreparedStatement, Future<R>> function) {
        return getSqlConnection().prepare(sql).compose(preparedStatement -> {
            return function.apply(preparedStatement).eventually(preparedStatement::close);
        });
    }

    /**
     * 复用同一个预编译语句，按顺序执行多组 {@code Tuple} 参数。
     * <p>
     * 此方法适用于同一 SQL 模板的批量参数绑定场景；每组参数会产生一个
     * {@link StatementExecuteResult}。
     *
     * @param keel      异步调度对象
     * @param tupleList 参数组列表
     * @return 每组参数对应的执行结果
     */
    @TechnicalPreview(since = "5.0.4")
    public Future<List<StatementExecuteResult>> executeThroughPrepare(Keel keel, List<Tuple> tupleList) {
        String sql = getStatement().buildSql();
        getSqlAuditLogger().info(r -> r.setPreparation(getUuid(), sql));
        List<StatementExecuteResult> results = new ArrayList<>();
        return executeWithPreparedStatement(keel, sql, preparedStatement -> {
            return keel.asyncCallIteratively(tupleList, tuple -> {
                return preparedStatement.query().execute(tuple)
                        .compose(rows -> {
                            StatementExecuteResult result = new StatementExecuteResult(rows);
                            getSqlAuditLogger().info(r -> r.setForDone(
                                    getUuid(),
                                    sql,
                                    result.getTotalAffectedRows(),
                                    result.getTotalFetchedRows()
                            ));
                            results.add(result);
                            return Future.succeededFuture();
                        });
            });
        }).compose(v -> Future.succeededFuture(results), throwable -> {
            getSqlAuditLogger().error(r -> r.setForFailed(getUuid(), sql)
                    .exception(throwable));
            return Future.failedFuture(throwable);
        });
    }
}
