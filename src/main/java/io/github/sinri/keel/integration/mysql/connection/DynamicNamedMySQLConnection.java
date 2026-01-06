package io.github.sinri.keel.integration.mysql.connection;

import io.vertx.sqlclient.SqlConnection;
import org.jspecify.annotations.NullMarked;


/**
 * 动态命名MySQL连接类，用于动态创建具有指定数据源名称的MySQL连接
 *
 * @since 5.0.0
 */
@NullMarked
public class DynamicNamedMySQLConnection extends AbstractNamedMySQLConnection {
    private final String dataSourceName;

    /**
     * 构造动态命名MySQL连接
     *
     * @param sqlConnection  SQL连接对象
     * @param dataSourceName 数据源名称
     */
    public DynamicNamedMySQLConnection(SqlConnection sqlConnection, String dataSourceName) {
        super(sqlConnection);
        this.dataSourceName = dataSourceName;
    }

    /**
     * 获取数据源名称
     *
     * @return 数据源名称
     */

    @Override
    public String getDataSourceName() {
        return dataSourceName;
    }
}
