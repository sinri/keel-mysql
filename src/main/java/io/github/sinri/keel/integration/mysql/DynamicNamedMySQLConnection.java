package io.github.sinri.keel.integration.mysql;

import io.github.sinri.keel.base.Keel;
import io.vertx.sqlclient.SqlConnection;
import org.jetbrains.annotations.NotNull;


/**
 * 动态命名MySQL连接类，用于动态创建具有指定数据源名称的MySQL连接
 *
 * @since 5.0.0
 */
public class DynamicNamedMySQLConnection extends NamedMySQLConnection {
    private final @NotNull String dataSourceName;

    /**
     * 构造动态命名MySQL连接
     *
     * @param sqlConnection  SQL连接对象
     * @param dataSourceName 数据源名称
     */
    public DynamicNamedMySQLConnection(@NotNull Keel keel, @NotNull SqlConnection sqlConnection, @NotNull String dataSourceName) {
        super(keel, sqlConnection);
        this.dataSourceName = dataSourceName;
    }

    /**
     * 获取数据源名称
     *
     * @return 数据源名称
     */
    @NotNull
    @Override
    public String getDataSourceName() {
        return dataSourceName;
    }
}
