package io.github.sinri.keel.integration.mysql.statement.templated;

import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;
import io.github.sinri.keel.integration.mysql.statement.mixin.ModifyStatementMixin;
import io.vertx.core.Handler;
import org.jetbrains.annotations.NotNull;


/**
 * 模板修改语句类，用于基于模板构建修改型SQL语句
 *
 * @since 5.0.0
 */
public class TemplatedModifyStatement extends AbstractStatement implements ModifyStatementMixin, TemplatedStatement {
    private final String templateSql;
    private final TemplateArgumentMapping argumentMapping;

    /**
     * 构造模板修改语句
     *
     * @param templateSql SQL模板字符串
     */
    public TemplatedModifyStatement(@NotNull String templateSql) {
        this.templateSql = templateSql;
        this.argumentMapping = new TemplateArgumentMapping();
    }

    @Override
    public String toString() {
        return this.build();
    }

    @Override
    public String getSqlTemplate() {
        return this.templateSql;
    }

    @Override
    public TemplateArgumentMapping getArguments() {
        return argumentMapping;
    }

    /**
     * 绑定参数
     * @param binder 参数绑定器
     * @return 自身实例
     */
    public TemplatedModifyStatement bindArguments(@NotNull Handler<TemplateArgumentMapping> binder) {
        binder.handle(this.argumentMapping);
        return this;
    }
}
