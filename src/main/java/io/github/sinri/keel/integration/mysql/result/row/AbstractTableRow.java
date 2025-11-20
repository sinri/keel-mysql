package io.github.sinri.keel.integration.mysql.result.row;

import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * As of 2.8 rename and remove.
 *
 * @since 2.0
 */
public abstract class AbstractTableRow extends SimpleResultRow {
    public AbstractTableRow(@NotNull JsonObject tableRow) {
        super(tableRow);
    }

    /**
     * @return default null
     */
    @Nullable
    public String sourceSchemaName() {
        return null;
    }

    /**
     * @return table name
     */
    @NotNull
    abstract public String sourceTableName();
}
