package io.github.sinri.keel.integration.mysql.dev;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.integration.mysql.provider.KeelMySQLDataSourceProvider;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public class GenDevTest extends KeelInstantRunner {
    private final LateObject<KeelMySQLDataSourceProvider> lateProvider = new LateObject<>();

    @Override
    protected Future<Void> beforeRun() {
        this.lateProvider.set(new KeelMySQLDataSourceProvider());
        return Future.succeededFuture();
    }

    @Override
    protected Future<Void> run() throws Exception {
        return lateProvider.get().loadDefault(getVertx())
                           .compose(dataSource -> {
                               return dataSource.withConnection(sampleMySQLConnection -> {
                                   TableRowClassSourceCodeGenerator tableRowClassSourceCodeGenerator = new TableRowClassSourceCodeGenerator(getVertx(), sampleMySQLConnection);
                                   String packagePath = ConfigElement.root().readString("table.package.path");
                                   Objects.requireNonNull(packagePath);
                                   return tableRowClassSourceCodeGenerator
                                           .generate(
                                                   "io.github.sinri.keel.integration.mysql.dev.runtime",
                                                   packagePath
                                           );
                               });

                           });

    }

}
