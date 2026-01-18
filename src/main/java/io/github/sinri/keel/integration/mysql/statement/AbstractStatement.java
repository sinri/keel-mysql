package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.core.utils.value.ValueBox;
import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * 抽象SQL语句基类，实现了通用的SQL执行和审计功能
 *
 * @since 5.0.0
 */
@NullMarked
abstract public class AbstractStatement<S> implements AnyStatement<S> {
    protected static String SQL_COMPONENT_SEPARATOR = " ";//"\n";
    protected final String statement_uuid;
    private final ValueBox<NamedMySQLConnection> connectionBox = new ValueBox<>();
    private String remarkAsComment = "";
    private boolean prepareStatement = true;

    /**
     * 构造抽象语句，生成唯一标识符
     */
    public AbstractStatement() {
        this.statement_uuid = UUID.randomUUID().toString();
    }

    @Override
    public S setNamedMySQLConnection(@Nullable NamedMySQLConnection connection) {
        this.connectionBox.setValue(connection);
        return getImplementation();
    }

    @Override
    public NamedMySQLConnection getNamedMySQLConnection() {
        if (connectionBox.isValueSetAndNotNull()) return connectionBox.getNonNullValue();
        else throw new IllegalStateException();
    }

    /**
     * 获取备注注释
     *
     * @return 备注注释
     */
    protected String getRemarkAsComment() {
        return remarkAsComment;
    }

    /**
     * 设置备注注释
     *
     * @param remarkAsComment 备注注释
     * @return 自身实例
     */
    @Override
    public S setRemarkAsComment(String remarkAsComment) {
        remarkAsComment = remarkAsComment.replaceAll("[\\r\\n]+", "¦");
        this.remarkAsComment = remarkAsComment;
        return getImplementation();
    }

    @Override
    public Future<ResultMatrix> execute() {
        if (connectionBox.isValueSetAndNotNull()) {
            return execute(connectionBox.getNonNullValue());
        } else {
            throw new IllegalStateException("Connection is not set for this statement.");
        }
    }

    /**
     * 在给定的SqlConnection上执行SQL，异步返回ResultMatrix，或异步报错。
     * （如果SQL审计日志记录器可用）将为审计记录执行的SQL和执行结果，以及任何异常。
     *
     * @param namedSqlConnection Fetched from Pool
     * @return the result matrix wrapped in a future, any error would cause a failed future
     * @since 2.8 将整个运作体加入了try-catch，统一加入审计日志，出现异常时一律异步报错。
     * @since 3.0.0 removed try-catch
     */
    @Override
    public Future<ResultMatrix> execute(NamedMySQLConnection namedSqlConnection) {
        LateObject<String> theSql = new LateObject<>();
        return Future.succeededFuture(this.toString())
                     .compose(sql -> {
                         theSql.set(sql);

                         if (isWithoutPrepare()) {
                             getSqlAuditLogger().info(r -> r.setQuery(statement_uuid, sql));
                             return namedSqlConnection.getSqlConnection().query(sql).execute();
                         } else {
                             getSqlAuditLogger().info(r -> r.setPreparation(statement_uuid, sql));
                             return namedSqlConnection.getSqlConnection().preparedQuery(sql).execute();
                         }
                     })
                     .compose(rows -> {
                         ResultMatrix resultMatrix = ResultMatrix.create(rows);
                         return Future.succeededFuture(resultMatrix);
                     })
                     .compose(resultMatrix -> {
                         getSqlAuditLogger().info(r -> r.setForDone(statement_uuid, theSql.get(), resultMatrix.getTotalAffectedRows(), resultMatrix.getTotalFetchedRows()));
                         return Future.succeededFuture(resultMatrix);
                     }, throwable -> {
                         getSqlAuditLogger().error(r -> r.setForFailed(statement_uuid, theSql.get())
                                                         .exception(throwable));
                         return Future.failedFuture(throwable);
                     });
    }

    @Override
    public S setPrepareStatement(boolean prepareStatement) {
        this.prepareStatement = prepareStatement;
        return getImplementation();
    }

    public boolean isPrepareStatement() {
        return prepareStatement;
    }

    /**
     * 判断是否不使用预处理语句
     *
     * @return 是否不使用预处理语句
     */
    @Deprecated(forRemoval = true)
    public boolean isWithoutPrepare() {
        return !prepareStatement;
    }

    /**
     * 设置是否不使用预处理语句
     *
     * @param withoutPrepare 是否不使用预处理语句
     * @return 自身实例
     */
    @Deprecated(forRemoval = true)
    public S setWithoutPrepare(boolean withoutPrepare) {
        this.prepareStatement = !withoutPrepare;
        return getImplementation();
    }

    abstract public String buildSql();

    @Override
    final public String toString() {
        return buildSql();
    }

    protected SpecificLogger<MySQLAuditSpecificLog> getSqlAuditLogger() {
        return StatementAuditorHolder.getInstance().getSqlAuditLogger();
    }
}
