package io.github.sinri.keel.integration.mysql.statement.impl;

import io.github.sinri.keel.integration.mysql.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.condition.CompareCondition;
import io.github.sinri.keel.integration.mysql.condition.GroupCondition;
import io.github.sinri.keel.integration.mysql.condition.MySQLCondition;
import io.github.sinri.keel.integration.mysql.condition.RawCondition;
import io.github.sinri.keel.integration.mysql.exception.KeelSQLGenerateError;
import io.github.sinri.keel.integration.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;
import io.github.sinri.keel.integration.mysql.statement.component.ConditionsComponent;
import io.github.sinri.keel.integration.mysql.statement.mixin.ReadStatementMixin;
import io.github.sinri.keel.integration.mysql.statement.mixin.SelectStatementMixin;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;


/**
 * SELECT语句实现类，用于构建和执行SELECT查询
 *
 * @since 5.0.0
 */
public class SelectStatement extends AbstractStatement implements SelectStatementMixin {
    final ConditionsComponent whereConditionsComponent;
    final ConditionsComponent havingConditionsComponent;
    private final List<String> tables;
    private final List<String> columns;
    private final List<String> categories;
    private final List<String> sortRules;
    private long offset;
    private long limit;
    private @NotNull String lockMode;
    /**
     * For MySQL 5.7 ,8.0 or higher, in Select, to limit the max execution time in millisecond.
     *
     * @since 3.1.0
     */
    private @Nullable Long maxExecutionTime;

    /**
     * @param another To swift clone one instance based on without direct reference.
     * @since 3.2.3
     */
    public SelectStatement(@NotNull SelectStatement another) {
        this.whereConditionsComponent = new ConditionsComponent(another.whereConditionsComponent);
        this.havingConditionsComponent = new ConditionsComponent(another.havingConditionsComponent);
        this.tables = new ArrayList<>(another.tables);
        this.columns = new ArrayList<>(another.columns);
        this.categories = new ArrayList<>(another.categories);
        this.sortRules = new ArrayList<>(another.sortRules);
        this.offset = another.offset;
        this.limit = another.limit;
        this.lockMode = another.lockMode;
        this.maxExecutionTime = another.maxExecutionTime;
    }

    public SelectStatement() {
        this.whereConditionsComponent = new ConditionsComponent();
        this.havingConditionsComponent = new ConditionsComponent();
        this.tables = new ArrayList<>();
        this.columns = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.sortRules = new ArrayList<>();
        this.offset = 0;
        this.limit = 0;
        this.lockMode = "";
        this.maxExecutionTime = null;
    }

    public SelectStatement from(@NotNull String tableExpression) {
        return from(tableExpression, null);
    }

    public SelectStatement from(@NotNull String tableExpression, @Nullable String alias) {
        if (tableExpression.isBlank()) {
            throw new KeelSQLGenerateError("Select from blank");
        }
        String x = tableExpression;
        if (alias != null) {
            x += " AS " + alias;
        }
        if (tables.isEmpty()) {
            tables.add(x);
        } else {
            tables.set(0, x);
        }
        return this;
    }

    /**
     * @since 2.8
     */
    public SelectStatement from(@NotNull ReadStatementMixin subQuery, @NotNull String alias) {
        if (alias.isBlank()) {
            throw new KeelSQLGenerateError("Sub Query without alias");
        }
        return this.from("(" + subQuery + ")", alias);
    }

    public SelectStatement leftJoin(@NotNull Function<JoinComponent, JoinComponent> joinFunction) {
        JoinComponent join = new JoinComponent("LEFT JOIN");
        tables.add(joinFunction.apply(join).toString());
        return this;
    }

    public SelectStatement rightJoin(@NotNull Function<JoinComponent, JoinComponent> joinFunction) {
        JoinComponent join = new JoinComponent("RIGHT JOIN");
        tables.add(joinFunction.apply(join).toString());
        return this;
    }

    public SelectStatement innerJoin(@NotNull Function<JoinComponent, JoinComponent> joinFunction) {
        JoinComponent join = new JoinComponent("INNER JOIN");
        tables.add(joinFunction.apply(join).toString());
        return this;
    }

    public SelectStatement straightJoin(@NotNull Function<JoinComponent, JoinComponent> joinFunction) {
        JoinComponent join = new JoinComponent("STRAIGHT_JOIN");
        tables.add(joinFunction.apply(join).toString());
        return this;
    }

    public SelectStatement resetColumns() {
        this.columns.clear();
        return this;
    }

    public SelectStatement column(@NotNull Function<ColumnComponent, ColumnComponent> func) {
        columns.add(func.apply(new ColumnComponent()).toString());
        return this;
    }

    public SelectStatement columnWithAlias(@NotNull String columnExpression, @NotNull String alias) {
        if (columnExpression.isBlank() || alias.isBlank()) {
            throw new IllegalArgumentException("Column or its alias is empty.");
        }
        columns.add(columnExpression + " as `" + alias + "`");
        return this;
    }

    public SelectStatement columnAsExpression(@NotNull String fieldName) {
        columns.add(fieldName);
        return this;
    }

    /**
     * @param function ConditionsComponent → ConditionsComponent it self
     * @return this
     * @since 1.4
     */
    public SelectStatement where(@NotNull Function<ConditionsComponent, ConditionsComponent> function) {
        function.apply(whereConditionsComponent);
        return this;
    }

    public SelectStatement groupBy(@NotNull String x) {
        categories.add(x);
        return this;
    }

    public SelectStatement groupBy(@NotNull List<String> x) {
        categories.addAll(x);
        return this;
    }

    public SelectStatement having(@NotNull Function<ConditionsComponent, ConditionsComponent> function) {
        function.apply(havingConditionsComponent);
        return this;
    }

    public SelectStatement orderByAsc(@NotNull String x) {
        sortRules.add(x);
        return this;
    }

    public SelectStatement orderByDesc(@NotNull String x) {
        sortRules.add(x + " DESC");
        return this;
    }

    public SelectStatement limit(long limit) {
        this.offset = 0;
        this.limit = limit;
        return this;
    }

    public SelectStatement limit(long limit, long offset) {
        this.offset = offset;
        this.limit = limit;
        return this;
    }

    public SelectStatement setLockMode(@NotNull String lockMode) {
        Objects.requireNonNull(lockMode);
        this.lockMode = lockMode;
        return this;
    }

    /**
     * Available in MySQL 5.7, 8.0 or higher.
     *
     * @since 3.1.0
     */
    public SelectStatement setMaxExecutionTime(long maxExecutionTime) {
        this.maxExecutionTime = maxExecutionTime;
        return this;
    }

    public String toString() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");

        // since 3.1.0: The Max Statement Execution Time
        //  MYSQL 5.7, 8.0+ /*+ MAX_EXECUTION_TIME(1000) */
        //  MYSQL 5.6 /*+ MAX_STATEMENT_TIME(1000) */ THIS IS NOT FOR ONE STATEMENT.
        if (this.maxExecutionTime != null) {
            sql.append("/*+ MAX_EXECUTION_TIME(").append(maxExecutionTime).append(") */ ")
               .append(AbstractStatement.SQL_COMPONENT_SEPARATOR);
        }

        if (columns.isEmpty()) {
            sql.append("*");
        } else {
            sql.append(String.join(",", columns));
        }
        if (!tables.isEmpty()) {
            sql.append(AbstractStatement.SQL_COMPONENT_SEPARATOR).append("FROM ")
               .append(String.join(AbstractStatement.SQL_COMPONENT_SEPARATOR, tables));
        }
        if (!whereConditionsComponent.isEmpty()) {
            sql.append(AbstractStatement.SQL_COMPONENT_SEPARATOR).append("WHERE ").append(whereConditionsComponent);
        }
        if (!categories.isEmpty()) {
            sql.append(AbstractStatement.SQL_COMPONENT_SEPARATOR).append("GROUP BY ")
               .append(String.join(",", categories));
        }
        if (!havingConditionsComponent.isEmpty()) {
            sql.append(AbstractStatement.SQL_COMPONENT_SEPARATOR).append("HAVING ").append(havingConditionsComponent);
        }
        if (!sortRules.isEmpty()) {
            sql.append(AbstractStatement.SQL_COMPONENT_SEPARATOR).append("ORDER BY ")
               .append(String.join(",", sortRules));
        }
        if (limit > 0) {
            sql.append(AbstractStatement.SQL_COMPONENT_SEPARATOR).append("LIMIT ").append(limit).append(" OFFSET ")
               .append(offset);
        }
        if (!lockMode.isEmpty()) {
            sql.append(AbstractStatement.SQL_COMPONENT_SEPARATOR).append(lockMode);
        }
        if (!getRemarkAsComment().isEmpty()) {
            sql.append("\n-- ").append(getRemarkAsComment()).append("\n");
        }
        return String.valueOf(sql);
    }


    /**
     * Call from this instance, as the original query as Select Statement for all rows in certain order.
     *
     * @param pageNo   since 1.
     * @param pageSize a number
     * @since 3.2.3
     * @since 3.2.20 Public
     */
    @Override
    public Future<PaginationResult> queryForPagination(
            NamedMySQLConnection sqlConnection,
            long pageNo,
            long pageSize
    ) {
        if (pageSize <= 0) throw new IllegalArgumentException("page size <= 0");
        if (pageNo < 1) throw new IllegalArgumentException("page no < 1");
        var countStatement = new SelectStatement(this)
                .resetColumns()
                .columnWithAlias("count(*)", "total")
                .limit(0, 0);
        this.limit(pageSize, (pageNo - 1) * pageSize);

        return Future.all(
                             countStatement.execute(sqlConnection)
                                           .compose(resultMatrix -> {
                                               try {
                                                   long total = resultMatrix.getOneColumnOfFirstRowAsLong("total");
                                                   return Future.succeededFuture(total);
                                               } catch (KeelSQLResultRowIndexError e) {
                                                   throw new RuntimeException(e);
                                               }
                                           }),
                             this.execute(sqlConnection)
                     )
                     .compose(compositeFuture -> {
                         Long total = compositeFuture.resultAt(0);
                         ResultMatrix resultMatrix = compositeFuture.resultAt(1);
                         return Future.succeededFuture(new PaginationResult(total, resultMatrix));
                     });
    }

    public static class JoinComponent {
        @NotNull
        final String joinType;
        final List<MySQLCondition> onConditions = new ArrayList<>();
        @NotNull
        String tableExpression = "NOT-SET";
        @Nullable
        String alias;

        public JoinComponent(@NotNull String joinType) {
            this.joinType = joinType;
        }

        public JoinComponent table(@NotNull String tableExpression) {
            this.tableExpression = tableExpression;
            return this;
        }

        public JoinComponent alias(@NotNull String alias) {
            this.alias = alias;
            return this;
        }

        public JoinComponent onForRaw(@NotNull Function<RawCondition, RawCondition> func) {
            this.onConditions.add(func.apply(new RawCondition()));
            return this;
        }

        public JoinComponent onForAndGroup(@NotNull Function<GroupCondition, GroupCondition> func) {
            this.onConditions.add(func.apply(new GroupCondition(GroupCondition.JUNCTION_FOR_AND)));
            return this;
        }

        public JoinComponent onForOrGroup(@NotNull Function<GroupCondition, GroupCondition> func) {
            this.onConditions.add(func.apply(new GroupCondition(GroupCondition.JUNCTION_FOR_OR)));
            return this;
        }

        public JoinComponent onForCompare(@NotNull Function<CompareCondition, CompareCondition> func) {
            this.onConditions.add(func.apply(new CompareCondition()));
            return this;
        }

        public String toString() {
            String s = joinType + " " + tableExpression;
            if (alias != null) {
                s += " AS " + alias;
            }
            if (!onConditions.isEmpty()) {
                s += " ON ";
                s += String.join(" AND ", onConditions.stream().map(MySQLCondition::toString).toList());
            }
            return s;
        }
    }

    public static class ColumnComponent {
        @Nullable
        String schema;
        @Nullable
        String field = "NOT-SET";
        @Nullable
        String expression;
        @Nullable
        String alias;

        public ColumnComponent field(@NotNull String field) {
            this.field = field;
            return this;
        }

        public ColumnComponent field(@Nullable String schema, @NotNull String field) {
            this.schema = schema;
            this.field = field;
            return this;
        }

        public ColumnComponent expression(@NotNull String expression) {
            this.expression = expression;
            return this;
        }

        public ColumnComponent alias(@Nullable String alias) {
            this.alias = alias;
            return this;
        }

        public String toString() {
            StringBuilder column = new StringBuilder();
            if (expression == null) {
                if (schema == null) {
                    column.append("`").append(field).append("`");
                } else {
                    column.append("`").append(schema).append("`.`").append(field).append("`");
                }
            } else {
                column.append(expression);
            }

            if (alias != null) {
                column.append(" AS `").append(alias).append("`");
            }
            return String.valueOf(column);
        }
    }
}
