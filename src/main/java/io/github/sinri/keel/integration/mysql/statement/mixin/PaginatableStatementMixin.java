package io.github.sinri.keel.integration.mysql.statement.mixin;

import org.jspecify.annotations.NullMarked;

/**
 * SELECT语句混合类，为读取操作提供分页查询功能
 *
 * @since 5.0.0
 */
@NullMarked
public interface SelectStatementMixin<S> extends ReadStatementMixin<S> {
    default S limit(long limit) {
        return limit(limit, 0);
    }

    S limit(long limit, long offset);
}
