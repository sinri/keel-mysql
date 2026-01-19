package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.integration.mysql.statement.mixin.SpecialStatementMixin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RawStatement extends AbstractStatement<RawStatement> implements SpecialStatementMixin<RawStatement> {
    private final String sql;

    public RawStatement(String sql, boolean prepareStatment) {
        super();
        this.sql = sql;
        this.setToPrepareStatement(prepareStatment);
    }

    @Override
    public String buildSql() {
        return sql;
    }
}
