package io.github.sinri.keel.integration.mysql.connection;

import io.github.sinri.keel.base.annotations.TechnicalPreview;

import java.io.Closeable;

/**
 * For virtual thread mode, the connection is closeable.
 */
@TechnicalPreview(since = "5.0.0")
public interface ClosableNamedMySQLConnection extends NamedMySQLConnection, Closeable {
    @Override
    default void close() {
        closeSqlConnection().await();
    }
}
