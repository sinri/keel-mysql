package io.github.sinri.keel.integration.mysql.result.row;

import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;


/**
 * Designed for a wrapper of each row in ResultMatrix.
 * <p>
 * As of 2.0 renamed from AbstractTableRow.
 * <p>
 * As of 2.7 renamed from AbstractRow.
 *
 * @since 1.10
 */
public class SimpleResultRow implements ResultRow {
    private JsonObject row;

    public SimpleResultRow(@NotNull JsonObject tableRow) {
        this.reloadData(tableRow);
    }

    @Override
    public final @NotNull JsonObject toJsonObject() {
        return row;
    }

    @Override
    public final void reloadData(@NotNull JsonObject jsonObject) {
        this.row = jsonObject;
    }
}
