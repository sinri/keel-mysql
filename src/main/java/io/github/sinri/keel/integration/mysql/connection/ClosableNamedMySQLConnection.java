package io.github.sinri.keel.integration.mysql.connection;

import io.vertx.sqlclient.SqlConnection;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.Closeable;

@Deprecated(since = "5.0.0", forRemoval = true)
@NullMarked
public final class ClosableNamedMySQLConnection<C extends NamedMySQLConnection> implements NamedMySQLConnection, Closeable {
    private final C namedMySQLConnection;

    public ClosableNamedMySQLConnection(C namedMySQLConnection) {
        this.namedMySQLConnection = namedMySQLConnection;
    }

    public C getNamedMySQLConnection() {
        return namedMySQLConnection;
    }

    @Override
    public SqlConnection getSqlConnection() {
        return getNamedMySQLConnection().getSqlConnection();
    }

    @Override
    public String getDataSourceName() {
        return getNamedMySQLConnection().getDataSourceName();
    }

    @Override
    public @Nullable String getMysqlVersion() {
        return getNamedMySQLConnection().getMysqlVersion();
    }

    @Override
    public void setMysqlVersion(@Nullable String mysqlVersion) {
        this.getNamedMySQLConnection().setMysqlVersion(mysqlVersion);
    }

    @Override
    public void close() {
        namedMySQLConnection.asyncClose().await();
    }
}
