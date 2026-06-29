package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.integration.mysql.statement.mixin.SpecialStatementMixin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RawStatement extends AbstractStatement<RawStatement> implements SpecialStatementMixin<RawStatement> {
    private final String sql;

    /**
     * 创建原始 SQL 语句。
     * <p>
     * 执行时默认由 {@link io.github.sinri.keel.integration.mysql.connection.target.RunnableStatement#execute()}
     * 走预编译路径；若 SQL 包含 {@code ?} 占位符，应调用
     * {@link io.github.sinri.keel.integration.mysql.connection.target.RunnableStatement#executeThroughPrepare(io.vertx.sqlclient.Tuple)}
     * 提供参数绑定。
     *
     * @param sql 原始 SQL 语句
     */
    public RawStatement(String sql) {
        super();
        this.sql = sql;
    }

    /**
     * 已废弃的构造器。
     * <p>
     * 自 5.0.4 起，是否走预编译协议不再由语句对象决定。请使用
     * {@link #RawStatement(String)} 创建语句，并在执行层选择
     * {@code executeThroughPrepare(...)} 或 {@code executeThroughQuery()}。
     *
     * @param sql              原始 SQL 语句
     * @param prepareStatement 已废弃参数
     * @deprecated since 5.0.4, use {@link #RawStatement(String)} and explicit
     * execution methods on {@code RunnableStatement}.
     */
    @Deprecated(since = "5.0.4", forRemoval = true)
    public RawStatement(String sql, boolean prepareStatement) {
        super();
        this.sql = sql;
        throw new UnsupportedOperationException(
                "RawStatement(String, boolean) was removed in 5.0.4; choose the execution method on RunnableStatement."
        );
    }

    @Override
    public String buildSql() {
        return sql;
    }
}
