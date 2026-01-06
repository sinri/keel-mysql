package io.github.sinri.keel.integration.mysql.statement.impl.ddl.view;

import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.SelectStatement;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/create-view.html">CREATE VIEW Statement</a>
 * @since 5.0.0
 */
@NullMarked
public class CreateViewStatement extends AbstractStatement {
    private final List<String> columns = new ArrayList<>();
    private boolean createOrReplace = false;
    /**
     * {@code UNDEFINED | MERGE | TEMPTABLE }
     */
    private @Nullable String algorithm = null;
    private @Nullable String definer = null;
    private @Nullable String sqlSecurity = null;
    private String viewName = "";
    private @Nullable SelectStatement selectStatement;
    /**
     * {@code [WITH [CASCADED | LOCAL] CHECK OPTION]}
     */
    private @Nullable String others = null;

    public CreateViewStatement setAlgorithm(@Nullable String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public CreateViewStatement setDefiner(@Nullable String definer) {
        this.definer = definer;
        return this;
    }

    public CreateViewStatement setOthers(@Nullable String others) {
        this.others = others;
        return this;
    }

    public CreateViewStatement setSelectStatement(SelectStatement selectStatement) {
        this.selectStatement = selectStatement;
        return this;
    }

    public CreateViewStatement setCreateOrReplace(boolean createOrReplace) {
        this.createOrReplace = createOrReplace;
        return this;
    }

    public CreateViewStatement setSqlSecurity(@Nullable String sqlSecurity) {
        this.sqlSecurity = sqlSecurity;
        return this;
    }

    public CreateViewStatement setViewName(String viewName) {
        this.viewName = viewName;
        return this;
    }

    public CreateViewStatement addColumns(String columnName) {
        this.columns.add(columnName);
        return this;
    }

    @Override
    public String toString() {
        /*
        CREATE
    [OR REPLACE]
    [ALGORITHM = {UNDEFINED | MERGE | TEMPTABLE}]
    [DEFINER = user]
    [SQL SECURITY { DEFINER | INVOKER }]
    VIEW view_name [(column_list)]
    AS select_statement
    [WITH [CASCADED | LOCAL] CHECK OPTION]
         */

        var columnsString = "";
        if (!columns.isEmpty()) {
            columnsString = "(" + columns.stream().map(x -> "`" + x + "`").collect(Collectors.joining(", ")) + ")";
        }
        return "CREATE "
                + (createOrReplace ? "OR REPLACE" : "") + " "
                + (algorithm != null ? ("ALGORITHM=" + algorithm) : "") + " "
                + (definer != null ? ("DEFINER=" + definer) : "") + " "
                + (sqlSecurity != null ? ("SQL SECURITY " + sqlSecurity) : "") + " "
                + "VIEW `" + viewName + "` "
                + columnsString
                + " AS " + selectStatement + " "
                + (others == null ? "" : others)
                ;
    }
}
