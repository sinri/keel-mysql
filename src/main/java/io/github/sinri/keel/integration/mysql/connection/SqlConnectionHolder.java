package io.github.sinri.keel.integration.mysql.connection;

import io.vertx.sqlclient.SqlConnection;
import org.jspecify.annotations.NullMarked;

@NullMarked
interface SqlConnectionHolder {
    SqlConnection getSqlConnection();
}
