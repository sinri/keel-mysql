package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.column;

import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.TableAlterOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * {@code MODIFY [COLUMN] col_name column_definition [FIRST | AFTER col_name]}
 *
 * @since 4.0.4
 */
public final class TableAlterOptionToModifyColumn extends TableAlterOption {
    public @NotNull String columnName = "";
    public @NotNull String columnDefinition = "";
    public @Nullable String position;

    public TableAlterOptionToModifyColumn setColumnName(@NotNull String columnName) {
        this.columnName = columnName;
        return this;
    }

    public TableAlterOptionToModifyColumn setColumnDefinition(@NotNull String columnDefinition) {
        this.columnDefinition = columnDefinition;
        return this;
    }

    public TableAlterOptionToModifyColumn placeToFirst() {
        this.position = "FIRST";
        return this;
    }

    public TableAlterOptionToModifyColumn placeAfter(@NotNull String previousColumnName) {
        this.position = "AFTER `" + previousColumnName + "`";
        return this;
    }

    @Override
    public String toString() {
        return "MODIFY COLUMN `" + columnName + "` " + columnDefinition + " "
                + (position == null ? "" : position);
    }
}
