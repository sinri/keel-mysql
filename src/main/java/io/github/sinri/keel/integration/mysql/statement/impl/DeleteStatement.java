package io.github.sinri.keel.integration.mysql.statement.impl;


import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;
import io.github.sinri.keel.integration.mysql.statement.component.ConditionsComponent;
import io.github.sinri.keel.integration.mysql.statement.mixin.ModifyStatementMixin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


/**
 * DELETE语句实现类，用于构建和执行DELETE删除操作
 *
 * @since 5.0.0
 */
@NullMarked
public class DeleteStatement extends AbstractStatement implements ModifyStatementMixin {
    private final ConditionsComponent whereConditionsComponent = new ConditionsComponent();
    private final List<String> sortRules = new ArrayList<>();
    /**
     * DELETE [LOW_PRIORITY] [QUICK] [IGNORE] FROM tbl_name [[AS] tbl_alias]
     * [PARTITION (partition_name [, partition_name] ...)]
     * [WHERE where_condition]
     * [ORDER BY ...]
     * [LIMIT row_count]
     */

    private @Nullable String schema;
    private String table = "NOT-SET";
    private long limit = 0;

    public DeleteStatement() {

    }

    public DeleteStatement from(String table) {
        this.schema = null;
        this.table = table;
        return this;
    }

    public DeleteStatement from(@Nullable String schema, String table) {
        this.schema = schema;
        this.table = table;
        return this;
    }

    /**
     * @param function ConditionsComponent → this
     * @return this
     * @since 1.4
     */
    public DeleteStatement where(Function<ConditionsComponent, ConditionsComponent> function) {
        function.apply(whereConditionsComponent);
        return this;
    }

    public DeleteStatement orderByAsc(String x) {
        sortRules.add(x);
        return this;
    }

    public DeleteStatement orderByDesc(String x) {
        sortRules.add(x + " DESC");
        return this;
    }

    public DeleteStatement limit(long limit) {
        this.limit = limit;
        return this;
    }

    public String toString() {
        String sql = "DELETE FROM ";
        if (schema != null) {
            sql += schema + ".";
        }
        sql += table;
        if (!whereConditionsComponent.isEmpty()) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "WHERE " + whereConditionsComponent;
        }
        if (!sortRules.isEmpty()) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "ORDER BY " + String.join(",", sortRules);
        }
        if (limit > 0) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "limit " + limit;
        }
        if (!getRemarkAsComment().isEmpty()) {
            sql += "\n-- " + getRemarkAsComment() + "\n";
        }
        return sql;
    }
}
