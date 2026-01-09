package io.github.sinri.keel.integration.mysql.connection;

import io.vertx.core.Closeable;
import io.vertx.core.Completable;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;


/**
 * 命名MySQL连接抽象基类，提供MySQL连接的通用功能
 *
 * @since 5.0.0
 */
@NullMarked
public interface NamedMySQLConnection extends Closeable {

    SqlConnection getSqlConnection();

    /**
     * 获取提供SQL连接的数据源名称
     *
     * @return 数据源名称
     */
    String getDataSourceName();

    /**
     * 获取MySQL版本信息
     *
     * @return MySQL版本，可能为null
     */
    @Nullable String getMysqlVersion();

    void setMysqlVersion(@Nullable String mysqlVersion);

    /**
     * 判断是否为MySQL 5.6.x版本
     *
     * @return 是否为MySQL 5.6.x
     */
    default boolean isMySQLVersion5dot6() {
        var mysqlVersion = getMysqlVersion();
        return mysqlVersion != null
                && mysqlVersion.startsWith("5.6.");
    }

    /**
     * 判断是否为MySQL 5.7.x版本
     *
     * @return 是否为MySQL 5.7.x
     */
    default boolean isMySQLVersion5dot7() {
        var mysqlVersion = getMysqlVersion();
        return mysqlVersion != null
                && mysqlVersion.startsWith("5.7.");
    }

    /**
     * 判断是否为MySQL 8.0.x版本
     *
     * @return 是否为MySQL 8.0.x
     */
    default boolean isMySQLVersion8dot0() {
        var mysqlVersion = getMysqlVersion();
        return mysqlVersion != null
                && mysqlVersion.startsWith("8.0.");
    }

    /**
     * 判断是否为MySQL 8.2.x版本
     *
     * @return 是否为MySQL 8.2.x
     */
    default boolean isMySQLVersion8dot2() {
        var mysqlVersion = getMysqlVersion();
        return mysqlVersion != null
                && mysqlVersion.startsWith("8.2.");
    }

    /**
     * 判断是否用于事务
     *
     * @return 是否用于事务
     */
    default boolean isForTransaction() {
        var sqlConnection = getSqlConnection();
        return null != sqlConnection.transaction();
    }

    default void close() {
        asyncClose();
    }

    default Future<Void> asyncClose() {
        return getSqlConnection().close();
    }

    @Override
    default void close(Completable<Void> completion) {
        asyncClose().andThen(completion);
    }
}
