package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table;

import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;
import io.github.sinri.keel.integration.mysql.statement.mixin.SpecialStatementMixin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/truncate-table.html">TRUNCATE TABLE Statement</a>
 * @since 5.0.0
 */
@NullMarked
public class TruncateTableStatement extends AbstractStatement<TruncateTableStatement>
implements SpecialStatementMixin<TruncateTableStatement> {
    private @Nullable String schemaName = null;
    private String tableName = "";

    @Nullable
    protected String getSchemaName() {
        return schemaName;
    }

    public TruncateTableStatement setSchemaName(@Nullable String schemaName) {
        this.schemaName = schemaName;
        return this;
    }


    protected String getTableName() {
        return tableName;
    }

    public TruncateTableStatement setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    protected final String getTableExpression() {
        return (schemaName == null ? "" : ("`" + schemaName + "`.")) + ("`" + tableName + "`");
    }

    @Override
    public String buildSql() {
        return "TRUNCATE TABLE " + getTableExpression();
    }
}
