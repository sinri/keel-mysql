package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.integration.mysql.statement.mixin.SpecialStatementMixin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RawStatement extends AbstractStatement<RawStatement> implements SpecialStatementMixin<RawStatement> {
    private final String sql;

    /**
     * 创建原始 SQL 语句。
     * <p>
     * 当 {@code prepareStatement} 为 {@code true} 时，SQL 将通过 MySQL 的 COM_STMT_PREPARE 协议发送，
     * 可获得服务端语句缓存与执行计划复用的性能优化。注意：当前不支持 {@code ?} 占位符参数绑定，
     * SQL 中不应包含 {@code ?} 占位符，否则执行时会因参数数量不匹配而报错。
     *
     * @param sql              完整的 SQL 语句（不含 {@code ?} 占位符）
     * @param prepareStatement 是否使用 MySQL 预编译协议（COM_STMT_PREPARE）
     */
    public RawStatement(String sql, boolean prepareStatement) {
        super();
        this.sql = sql;
        this.setToPrepareStatement(prepareStatement);
    }

    @Override
    public String buildSql() {
        return sql;
    }
}
