package io.github.sinri.keel.integration.mysql.dev;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public @NotNull Keel getKeel() {
        return this;
    }

    @Override
    protected @NotNull Future<Void> run() throws Exception {
        return rebuildTablesInSchema(
                this.defaultMySQLDataSourceName(),
                sqlConnection -> new SampleMySQLConnection(sqlConnection),
                "drydock_lesson"
        );
    }
}
