package io.github.sinri.keel.integration.mysql.dev;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.integration.mysql.KeelMySQLDataSourceProvider;
import io.github.sinri.keel.integration.mysql.NamedMySQLConnection;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;
import org.jetbrains.annotations.NotNull;

public class GenDevTest extends KeelInstantRunner implements KeelMySQLDataSourceProvider {
    @Override
    protected @NotNull Future<Void> run() throws Exception {
        return this.loadNamedMySQLDataSource(
                           this.defaultMySQLDataSourceName(),
                           sqlConnection -> new SampleMySQLConnection(this, sqlConnection)
                   )
                   .compose(namedMySQLDataSource -> {
                       return namedMySQLDataSource.withConnection(sampleMySQLConnection -> {
                           TableRowClassSourceCodeGenerator tableRowClassSourceCodeGenerator = new TableRowClassSourceCodeGenerator(sampleMySQLConnection);
                           return tableRowClassSourceCodeGenerator
                                   .generate(
                                           "io.github.sinri.keel.integration.mysql.dev.runtime",
                                           this.config("test-dev-package-path")
                                   );
                       });

                   });

    }

    @Override
    public @NotNull Keel getKeel() {
        return this;
    }

    static class SampleMySQLConnection extends NamedMySQLConnection {

        /**
         * 构造命名MySQL连接
         *
         * @param sqlConnection SQL连接对象
         */
        public SampleMySQLConnection(@NotNull Keel keel, @NotNull SqlConnection sqlConnection) {
            super(keel, sqlConnection);
        }

        @Override
        public @NotNull String getDataSourceName() {
            return "union";
        }
    }
}
