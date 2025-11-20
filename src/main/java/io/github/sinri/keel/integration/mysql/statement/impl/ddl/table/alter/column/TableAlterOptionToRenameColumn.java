package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.column;

import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.TableAlterOption;
import org.jetbrains.annotations.NotNull;

/**
 * {@code RENAME COLUMN old_col_name TO new_col_name}
 *
 * @since 4.0.4
 */
public final class TableAlterOptionToRenameColumn extends TableAlterOption {
    public @NotNull String columnName = "";
    public @NotNull String newColumnName = "";


    public TableAlterOptionToRenameColumn setColumnName(@NotNull String columnName) {
        this.columnName = columnName;
        return this;
    }

    public TableAlterOptionToRenameColumn setNewColumnName(@NotNull String newColumnName) {
        this.newColumnName = newColumnName;
        return this;
    }

    @Override
    public String toString() {
        return "RENAME COLUMN `" + columnName + "` TO `" + newColumnName + "`";
    }
}
