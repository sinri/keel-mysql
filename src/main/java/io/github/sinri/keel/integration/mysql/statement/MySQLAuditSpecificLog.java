package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.logger.api.log.SpecificLog;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;


/**
 * MySQL审计专用日志类，用于记录MySQL审计问题
 * 该类提供了设置MySQL查询状态的方法，包括准备、执行和失败，
 * 以及相关属性如SQL语句、语句UUID和受影响/获取的行数
 *
 * @since 5.0.0
 */
@NullMarked
public final class MySQLAuditSpecificLog extends SpecificLog<MySQLAuditSpecificLog> {
    public static final String TopicMysqlAudit = "MysqlAudit";
    public static final String AttributeMysqlAudit = "MysqlAudit";
    public static final String KeyStatementUuid = "statement_uuid";
    public static final String KeySql = "sql";
    public static final String KeyTotalAffectedRows = "TotalAffectedRows";
    public static final String KeyTotalFetchedRows = "TotalFetchedRows";

    /**
     * 构造MySQL审计日志对象
     */
    public MySQLAuditSpecificLog() {
        super();
    }


    /**
     * 设置MySQL查询的准备状态，包括语句UUID和SQL查询
     *
     * @param statement_uuid 预处理语句的唯一标识符
     * @param sql            被准备的SQL查询
     * @return 当前MySQLAuditSpecificLog实例，用于方法链式调用
     */
    public MySQLAuditSpecificLog setPreparation(String statement_uuid, String sql) {
        this.message("MySQL query prepared.")
            .extra(AttributeMysqlAudit, new JsonObject()
                    .put(KeyStatementUuid, statement_uuid)
                    .put(KeySql, sql)
            );
        return this;
    }

    /**
     * 设置MySQL审计问题的查询详情，包括语句UUID和SQL查询
     *
     * @param statement_uuid 语句的唯一标识符
     * @param sql            被执行的SQL查询
     * @return 当前MySQLAuditSpecificLog实例，用于方法链式调用
     */
    public MySQLAuditSpecificLog setQuery(String statement_uuid, String sql) {
        this.message("MySQL query without preparation.")
            .extra(AttributeMysqlAudit, new JsonObject()
                    .put(KeyStatementUuid, statement_uuid)
                    .put(KeySql, sql)
            );
        return this;
    }


    /**
     * 设置MySQL查询的完成状态，包括语句UUID、SQL查询、总受影响行数和总获取行数
     *
     * @param statement_uuid    被执行语句的唯一标识符
     * @param sql               被执行的SQL查询
     * @param totalAffectedRows 查询受影响的行数
     * @param totalFetchedRows  查询获取的行数
     * @return 当前MySQLAuditSpecificLog实例，用于方法链式调用
     */
    public MySQLAuditSpecificLog setForDone(
            String statement_uuid,
            String sql,
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
     * 设置MySQL查询的失败状态，包括语句UUID和SQL查询
     *
     * @param statement_uuid 失败语句的唯一标识符
     * @param sql            被执行且失败的SQL查询
     * @return 当前MySQLAuditSpecificLog实例，用于方法链式调用
     */
    public MySQLAuditSpecificLog setForFailed(String statement_uuid, String sql) {
        this.message("MySQL query failed.")
            .extra(AttributeMysqlAudit, new JsonObject()
                    .put(KeyStatementUuid, statement_uuid)
                    .put(KeySql, sql)
            );
        return this;
    }
}
