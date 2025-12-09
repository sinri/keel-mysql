package io.github.sinri.keel.integration.mysql.connection;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.vertx.sqlclient.SqlConnection;
import org.jetbrains.annotations.NotNull;

/**
 * For virtual thread mode, the connection is closable and could be used in a try-with-resource statement.
 *
 * @since 5.0.0
 */
@TechnicalPreview(since = "5.0.0")
public abstract class AbstractClosableNamedMySQLConnection extends AbstractNamedMySQLConnection implements ClosableNamedMySQLConnection {
    /**
     * 构造命名MySQL连接
     *
     * @param sqlConnection SQL连接对象
     */
    public AbstractClosableNamedMySQLConnection(@NotNull SqlConnection sqlConnection) {
        super(sqlConnection);
    }
}
