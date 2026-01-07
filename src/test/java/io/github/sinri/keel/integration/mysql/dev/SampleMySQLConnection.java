package io.github.sinri.keel.integration.mysql.dev;

import io.github.sinri.keel.integration.mysql.connection.AbstractNamedMySQLConnection;
import io.vertx.sqlclient.SqlConnection;
import org.jspecify.annotations.NullMarked;

@NullMarked
class SampleMySQLConnection extends AbstractNamedMySQLConnection {

    /**
     * 构造命名MySQL连接
     *
     * @param sqlConnection SQL连接对象
     */
    public SampleMySQLConnection(SqlConnection sqlConnection) {
        super(sqlConnection);
    }

    @Override
    public String getDataSourceName() {
        return "union";
    }
}
