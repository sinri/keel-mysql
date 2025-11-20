package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.column;

import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.TableAlterOption;
import org.jetbrains.annotations.NotNull;

/**
 * {@code DROP [COLUMN] col_name}
 *
 * @since 4.0.4
 */
public final class TableAlterOptionToDropColumn extends TableAlterOption {
    public @NotNull String columnName = "";

    public TableAlterOptionToDropColumn setColumnName(@NotNull String columnName) {
        this.columnName = columnName;
        return this;
    }

    @Override
    public String toString() {
        return "DROP COLUMN `" + columnName + "`";
    }
}
