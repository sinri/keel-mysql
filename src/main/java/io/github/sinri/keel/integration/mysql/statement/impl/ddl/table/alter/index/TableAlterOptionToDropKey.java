package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.index;

import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.TableAlterOption;
import org.jetbrains.annotations.NotNull;


/**
 * {@code DROP {INDEX | KEY} index_name}
 *
 * @since 4.0.4
 */
public final class TableAlterOptionToDropKey extends TableAlterOption {
    private @NotNull String indexName = "";

    public TableAlterOptionToDropKey setIndexName(@NotNull String indexName) {
        this.indexName = indexName;
        return this;
    }

    @Override
    public String toString() {
        return "DROP KEY `" + indexName + "`";
    }
}
