package io.github.sinri.keel.integration.mysql.connection;

import io.vertx.sqlclient.SqlConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractNamedMySQLConnection implements NamedMySQLConnection {
    @NotNull
    private final SqlConnection sqlConnection;
    @Nullable
    private String mysqlVersion;

    /**
     * 构造命名MySQL连接
     *
     * @param sqlConnection SQL连接对象
     */
    public AbstractNamedMySQLConnection(@NotNull SqlConnection sqlConnection) {
        this.sqlConnection = sqlConnection;
    }

    /**
     * 获取SQL连接对象
     *
     * @return SQL连接对象
     */
    public @NotNull SqlConnection getSqlConnection() {
        return sqlConnection;
    }

    /**
     * 获取MySQL版本信息
     *
     * @return MySQL版本，可能为null
     */
    @Nullable
    public final String getMysqlVersion() {
        return mysqlVersion;
    }

    /**
     * 设置MySQL版本信息
     *
     * @param mysqlVersion MySQL版本
     */
    public final void setMysqlVersion(@Nullable String mysqlVersion) {
        this.mysqlVersion = mysqlVersion;
    }

}
