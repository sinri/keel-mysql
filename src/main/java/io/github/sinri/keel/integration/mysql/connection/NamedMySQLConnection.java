package io.github.sinri.keel.integration.mysql.connection;

import io.vertx.core.Future;
import io.vertx.sqlclient.Transaction;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.Closeable;


/**
 * 命名MySQL连接抽象基类，提供MySQL连接的通用功能
 *
 * @since 5.0.0
 */
@NullMarked
public interface NamedMySQLConnection extends RunnableStatementFactory,MySQLServerVersionMixin, Closeable {

    /**
     * 获取提供SQL连接的数据源名称
     *
     * @return 数据源名称
     */
    String getDataSourceName();

    /**
     * 判断是否用于事务
     *
     * @return 是否用于事务
     */
    default boolean isForTransaction() {
        var sqlConnection = getSqlConnection();
        return null != sqlConnection.transaction();
    }

    default @Nullable Transaction getTransaction() {
        return getSqlConnection().transaction();
    }

    default Future<Transaction> beginTransaction() {
        return getSqlConnection().begin();
    }

    default Future<Void> commitTransaction() {
        return getSqlConnection().transaction().commit();
    }

    default Future<Void> rollbackTransaction() {
        return getSqlConnection().transaction().rollback();
    }

    default void close() {
        asyncClose();
    }

    default Future<Void> asyncClose() {
        return getSqlConnection().close();
    }

}
