package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.create;


import org.jetbrains.annotations.NotNull;

/**
 * Follow Pattern:
 * {@code CREATE [TEMPORARY] TABLE [IF NOT EXISTS] tbl_name { LIKE old_tbl_name | (LIKE old_tbl_name) } }.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/create-table.html">CREATE TABLE Statement</a> MySQL 8.0
 *         Docs
 * @since 4.0.4
 */
public final class CreateTableLikeTableStatement extends CreateTableStatementBase<CreateTableLikeTableStatement> {
    private @NotNull String anotherTableExpression = "";

    public CreateTableLikeTableStatement setAnotherTableExpression(@NotNull String anotherTableExpression) {
        this.anotherTableExpression = anotherTableExpression;
        return this;
    }

    @Override
    public String toString() {
        return "CREATE " + (useTemporary() ? "TEMPORARY " : " ") + "TABLE "
                + (useIfNotExists() ? "IF NOT EXISTS " : " ")
                + getTableExpression()
                + " LIKE " + anotherTableExpression;
    }

    @NotNull
    @Override
    public CreateTableLikeTableStatement getImplementation() {
        return this;
    }
}
