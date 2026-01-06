package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.component;

import org.jspecify.annotations.NullMarked;

/**
 * It is not standardized.
 *
 * @since 5.0.0
 */
@NullMarked
public class CreateTablePartitionOptions {
    private final String raw;

    public CreateTablePartitionOptions(String raw) {
        this.raw = raw;
    }

    @Override
    public String toString() {
        return raw;
    }
}
