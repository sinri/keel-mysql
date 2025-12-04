package io.github.sinri.keel.integration.mysql.dev;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.integration.mysql.KeelMySQLDataSourceProvider;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

public class GenDevTest extends KeelInstantRunner implements KeelMySQLDataSourceProvider {
    @Override
    protected @NotNull Future<Void> run() throws Exception {
        return this.loadNamedMySQLDataSource(
                           this.defaultMySQLDataSourceName(),
                           SampleMySQLConnection::new
                   )
                   .compose(namedMySQLDataSource -> {
                       return namedMySQLDataSource.withConnection(sampleMySQLConnection -> {
                           TableRowClassSourceCodeGenerator tableRowClassSourceCodeGenerator = new TableRowClassSourceCodeGenerator(getKeel(), sampleMySQLConnection);
                           return tableRowClassSourceCodeGenerator
                                   .generate(
                                           "io.github.sinri.keel.integration.mysql.dev.runtime",
                                           this.config("table.package.path")
                                   );
                       });

                   });

    }

    @Override
    public @NotNull Keel getKeel() {
        return this;
    }

}
