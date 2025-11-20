package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table;

import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/truncate-table.html">TRUNCATE TABLE Statement</a>
 * @since 5.0.0
 */
public class TruncateTableStatement extends AbstractStatement {
    private @Nullable String schemaName = null;
    private @NotNull String tableName = "";

    @Nullable
    protected String getSchemaName() {
        return schemaName;
    }

    public TruncateTableStatement setSchemaName(@Nullable String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    @NotNull
    protected String getTableName() {
        return tableName;
    }

    public TruncateTableStatement setTableName(@NotNull String tableName) {
        this.tableName = tableName;
        return this;
    }

    protected final String getTableExpression() {
        return (schemaName == null ? "" : ("`" + schemaName + "`.")) + ("`" + tableName + "`");
    }

    @Override
    public String toString() {
        return "TRUNCATE TABLE " + getTableExpression();
    }
}
