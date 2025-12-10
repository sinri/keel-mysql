package io.github.sinri.keel.integration.mysql.dev;

import io.github.sinri.keel.integration.mysql.provider.KeelMySQLDataSourceProvider;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

public class GenDevTest extends KeelInstantRunner {
    private KeelMySQLDataSourceProvider provider;

    @Override
    protected @NotNull Future<Void> beforeRun() {
        this.provider = new KeelMySQLDataSourceProvider(getKeel());
        return Future.succeededFuture();
    }

    @Override
    protected @NotNull Future<Void> run() throws Exception {
        return provider.loadDefault()
                       .compose(dataSource -> {
                           return dataSource.withConnection(sampleMySQLConnection -> {
                               TableRowClassSourceCodeGenerator tableRowClassSourceCodeGenerator = new TableRowClassSourceCodeGenerator(getKeel(), sampleMySQLConnection);
                               return tableRowClassSourceCodeGenerator
                                       .generate(
                                               "io.github.sinri.keel.integration.mysql.dev.runtime",
                                               this.config("table.package.path")
                                       );
                           });

                       });

    }

}
