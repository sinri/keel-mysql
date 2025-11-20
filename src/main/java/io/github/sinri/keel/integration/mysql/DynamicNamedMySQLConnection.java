package io.github.sinri.keel.integration.mysql;

import io.vertx.sqlclient.SqlConnection;
import org.jetbrains.annotations.NotNull;



public class DynamicNamedMySQLConnection extends NamedMySQLConnection {
    private final @NotNull String dataSourceName;

    public DynamicNamedMySQLConnection(@NotNull SqlConnection sqlConnection, @NotNull String dataSourceName) {
        super(sqlConnection);
        this.dataSourceName = dataSourceName;
    }

    @NotNull
    @Override
    public String getDataSourceName() {
        return dataSourceName;
    }
}
