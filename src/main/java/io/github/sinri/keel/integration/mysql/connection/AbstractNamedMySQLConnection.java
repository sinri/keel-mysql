package io.github.sinri.keel.integration.mysql.connection;

import io.vertx.sqlclient.SqlConnection;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract class AbstractNamedMySQLConnection implements NamedMySQLConnection {
    private final SqlConnection sqlConnection;
    private @Nullable String mysqlVersion;

    /**
     * 构造命名MySQL连接
     *
     * @param sqlConnection SQL连接对象
     */
    public AbstractNamedMySQLConnection(SqlConnection sqlConnection) {
        this.sqlConnection = sqlConnection;
    }

    /**
     * 获取SQL连接对象
     *
     * @return SQL连接对象
     */
    public SqlConnection getSqlConnection() {
        return sqlConnection;
    }

    /**
     * 获取MySQL版本信息
     *
     * @return MySQL版本，可能为null
     */
    public final @Nullable String getMysqlVersion() {
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
