package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.factory.SilentLoggerFactory;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.atomic.AtomicReference;

/**
 * SQL审计日志记录器 Holder
 */
@NullMarked
public class StatementAuditorHolder {
    private static final StatementAuditorHolder INSTANCE = new StatementAuditorHolder();

    private final AtomicReference<SpecificLogger<MySQLAuditSpecificLog>> sqlAuditLoggerRef;

    private StatementAuditorHolder() {
        var sqlAuditLogger = buildSqlAuditLogger(SilentLoggerFactory.getInstance());
        sqlAuditLoggerRef = new AtomicReference<>(sqlAuditLogger);
    }

    public static StatementAuditorHolder getInstance() {
        return INSTANCE;
    }

    /**
     * 构建SQL审计日志记录器
     *
     * @param loggerFactory 日志工厂
     * @return SQL审计日志记录器
     */
    private SpecificLogger<MySQLAuditSpecificLog> buildSqlAuditLogger(LoggerFactory loggerFactory) {
        return loggerFactory.createLogger(MySQLAuditSpecificLog.AttributeMysqlAudit, MySQLAuditSpecificLog::new);
    }

    /**
     * 重新加载SQL审计日志记录器
     *
     * @param loggerFactory SQL审计发送到的日志工厂
     */
    public void reloadSqlAuditLogger(LoggerFactory loggerFactory) {
        var sqlAuditLogger = buildSqlAuditLogger(loggerFactory);
        sqlAuditLoggerRef.set(sqlAuditLogger);
    }

    public SpecificLogger<MySQLAuditSpecificLog> getSqlAuditLogger() {
        return sqlAuditLoggerRef.get();
    }
}
