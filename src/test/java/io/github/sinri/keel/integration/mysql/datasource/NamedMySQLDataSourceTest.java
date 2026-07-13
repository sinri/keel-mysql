package io.github.sinri.keel.integration.mysql.datasource;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.integration.mysql.KeelMySQLConfiguration;
import io.github.sinri.keel.integration.mysql.connection.DynamicNamedMySQLConnection;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NamedMySQLDataSourceTest {

    @Test
    void fetchConnectionInVirtualThreadShouldRejectPlatformThreadBeforeConnecting() throws Exception {
        Vertx vertx = Vertx.vertx();
        NamedMySQLDataSource<DynamicNamedMySQLConnection> dataSource = new NamedMySQLDataSource<>(
                vertx,
                createConfiguration(),
                sqlConnection -> new DynamicNamedMySQLConnection(sqlConnection, "test")
        );

        try {
            UnsupportedOperationException exception = assertThrows(
                    UnsupportedOperationException.class,
                    dataSource::fetchConnectionInVirtualThread
            );
            assertEquals("This method must be called from a virtual thread", exception.getMessage());
            assertEquals(0, dataSource.getCurrentPoolSize());
            assertEquals(0, dataSource.getCurrentActiveConnectionCount());
        } finally {
            dataSource.close().toCompletionStage().toCompletableFuture().join();
            vertx.close().toCompletionStage().toCompletableFuture().join();
        }
    }

    private KeelMySQLConfiguration createConfiguration() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("mysql.test.host", "127.0.0.1");
        properties.setProperty("mysql.test.username", "test_user");
        properties.setProperty("mysql.test.password", "test_password");

        ConfigElement root = new ConfigElement("");
        root.loadData(properties);
        return new KeelMySQLConfiguration(root.extract("mysql", "test"));
    }
}
