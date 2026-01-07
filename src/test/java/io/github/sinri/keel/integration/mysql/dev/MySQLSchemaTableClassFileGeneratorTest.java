package io.github.sinri.keel.integration.mysql.dev;

import io.github.sinri.keel.integration.mysql.provider.KeelMySQLDataSourceProvider;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class MySQLSchemaTableClassFileGeneratorTest extends KeelInstantRunner implements MySQLSchemaTableClassFileGenerator {
    @Override
    public String getTablePackage() {
        return "io.github.sinri.keel.integration.mysql.dev.runtime";
    }

    @Override
    public @Nullable String getStrictEnumPackage() {
        return null;
    }

    @Override
    public @Nullable String getEnvelopePackage() {
        return null;
    }

    @Override
    protected Future<Void> run() throws Exception {
        return rebuildTablesInSchema(
                KeelMySQLDataSourceProvider.defaultMySQLDataSourceName(),
                SampleMySQLConnection::new,
                "drydock_lesson"
        );
    }
}
