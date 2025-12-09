package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.factory.SilentLoggerFactory;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 抽象SQL语句基类，实现了通用的SQL执行和审计功能
 *
 * @since 5.0.0
 */
abstract public class AbstractStatement implements AnyStatement {
    protected static @NotNull String SQL_COMPONENT_SEPARATOR = " ";//"\n";
    /**
     * SQL审计日志记录器
     */
    protected static @NotNull SpecificLogger<MySQLAuditSpecificLog> sqlAuditLogger;

    static {
        sqlAuditLogger = buildSqlAuditLogger(SilentLoggerFactory.getInstance());
    }

    protected final @NotNull String statement_uuid;
    private @NotNull String remarkAsComment = "";
    /**
     * @since 4.0.7
     */
    private boolean withoutPrepare = false;

    /**
     * 构造抽象语句，生成唯一标识符
     */
    public AbstractStatement() {
        this.statement_uuid = UUID.randomUUID().toString();
    }

    /**
     * 获取SQL审计日志记录器
     *
     * @return SQL审计日志记录器
     */
    @NotNull
    public static SpecificLogger<MySQLAuditSpecificLog> getSqlAuditLogger() {
        return sqlAuditLogger;
    }


    /**
     * 构建SQL审计日志记录器
     * @param loggerFactory 日志工厂
     * @return SQL审计日志记录器
     */
    private static SpecificLogger<MySQLAuditSpecificLog> buildSqlAuditLogger(@NotNull LoggerFactory loggerFactory) {
        return loggerFactory.createLogger(MySQLAuditSpecificLog.AttributeMysqlAudit, MySQLAuditSpecificLog::new);
    }

    /**
     * 重新加载SQL审计问题记录器
     * @param issueRecordCenter SQL审计发送到的记录中心
     */
    public static synchronized void reloadSqlAuditIssueRecording(@NotNull LoggerFactory issueRecordCenter) {
        sqlAuditLogger = buildSqlAuditLogger(issueRecordCenter);
    }

    /**
     * 设置SQL组件分隔符
     * @param sqlComponentSeparator SQL组件分隔符
     */
    public static void setSqlComponentSeparator(@NotNull String sqlComponentSeparator) {
        SQL_COMPONENT_SEPARATOR = sqlComponentSeparator;
    }

    /**
     * 获取备注注释
     * @return 备注注释
     */
    @NotNull
    protected String getRemarkAsComment() {
        return remarkAsComment;
    }

    /**
     * 设置备注注释
     * @param remarkAsComment 备注注释
     * @return 自身实例
     */
    public AbstractStatement setRemarkAsComment(@NotNull String remarkAsComment) {
        remarkAsComment = remarkAsComment.replaceAll("[\\r\\n]+", "¦");
        this.remarkAsComment = remarkAsComment;
        return this;
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
    public Future<ResultMatrix> execute(@NotNull NamedMySQLConnection namedSqlConnection) {
        AtomicReference<String> theSql = new AtomicReference<>();
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
                         getSqlAuditLogger().exception(throwable, r -> r.setForFailed(statement_uuid, theSql.get()));
                         return Future.failedFuture(throwable);
                     });
    }

    /**
     * 判断是否不使用预处理语句
     * @return 是否不使用预处理语句
     */
    @Override
    public boolean isWithoutPrepare() {
        return withoutPrepare;
    }

    /**
     * 设置是否不使用预处理语句
     * @param withoutPrepare 是否不使用预处理语句
     * @return 自身实例
     */
    public AbstractStatement setWithoutPrepare(boolean withoutPrepare) {
        this.withoutPrepare = withoutPrepare;
        return this;
    }
}
