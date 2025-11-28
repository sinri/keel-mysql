package io.github.sinri.keel.integration.mysql;

import io.vertx.sqlclient.SqlConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * 命名MySQL连接抽象基类，提供MySQL连接的通用功能
 *
 * @since 5.0.0
 */
abstract public class NamedMySQLConnection {
    @NotNull
    private final SqlConnection sqlConnection;

    /**
     * 构造命名MySQL连接
     *
     * @param sqlConnection SQL连接对象
     */
    public NamedMySQLConnection(@NotNull SqlConnection sqlConnection) {
        this.sqlConnection = sqlConnection;
    }

    /**
     * 获取SQL连接对象
     * @return SQL连接对象
     */
    public @NotNull SqlConnection getSqlConnection() {
        return sqlConnection;
    }

    /**
     * 获取提供SQL连接的数据源名称
     * @return 数据源名称
     */
    @NotNull
    abstract public String getDataSourceName();

    private @Nullable String mysqlVersion;

    /**
     * 获取MySQL版本信息
     * @return MySQL版本，可能为null
     */
    @Nullable
    public final String getMysqlVersion() {
        return mysqlVersion;
    }

    /**
     * 设置MySQL版本信息
     * @param mysqlVersion MySQL版本
     * @return 自身实例
     */
    @NotNull
    public final NamedMySQLConnection setMysqlVersion(@Nullable String mysqlVersion) {
        this.mysqlVersion = mysqlVersion;
        return this;
    }

    /**
     * 判断是否为MySQL 5.6.x版本
     * @return 是否为MySQL 5.6.x
     */
    public final boolean isMySQLVersion5dot6() {
        return mysqlVersion != null
                && mysqlVersion.startsWith("5.6.");
    }

    /**
     * 判断是否为MySQL 5.7.x版本
     * @return 是否为MySQL 5.7.x
     */
    public final boolean isMySQLVersion5dot7() {
        return mysqlVersion != null
                && mysqlVersion.startsWith("5.7.");
    }

    /**
     * 判断是否为MySQL 8.0.x版本
     * @return 是否为MySQL 8.0.x
     */
    public final boolean isMySQLVersion8dot0() {
        return mysqlVersion != null
                && mysqlVersion.startsWith("8.0.");
    }

    /**
     * 判断是否为MySQL 8.2.x版本
     * @return 是否为MySQL 8.2.x
     */
    public final boolean isMySQLVersion8dot2() {
        return mysqlVersion != null
                && mysqlVersion.startsWith("8.2.");
    }

    /**
     * 判断是否用于事务
     * @return 是否用于事务
     */
    public boolean isForTransaction() {
        return null != sqlConnection.transaction();
    }
}
