package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.column;

import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.TableAlterOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * {@code ADD [COLUMN] col_name column_definition [FIRST | AFTER col_name]}
 *
 * @since 4.0.4
 */
public final class TableAlterOptionToAddColumn extends TableAlterOption {
    public @NotNull String columnName = "";
    public @NotNull String columnDefinition = "";
    public @Nullable String position;

    public TableAlterOptionToAddColumn setColumnName(@NotNull String columnName) {
        this.columnName = columnName;
        return this;
    }

    public TableAlterOptionToAddColumn setColumnDefinition(@NotNull String columnDefinition) {
        this.columnDefinition = columnDefinition;
        return this;
    }

    public TableAlterOptionToAddColumn placeToFirst() {
        this.position = "FIRST";
        return this;
    }

    public TableAlterOptionToAddColumn placeAfter(@NotNull String previousColumnName) {
        this.position = "AFTER `" + previousColumnName + "`";
        return this;
    }

    @Override
    public String toString() {
        return "ADD COLUMN `" + columnName + "` " + columnDefinition + " "
                + (position == null ? "" : position);
    }
}
