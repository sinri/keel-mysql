package io.github.sinri.keel.integration.mysql.statement.templated;

import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;
import io.github.sinri.keel.integration.mysql.statement.mixin.ReadStatementMixin;
import io.vertx.core.Handler;
import org.jspecify.annotations.NullMarked;


/**
 * 模板读取语句类，用于基于模板构建读取型SQL语句
 *
 * @since 5.0.0
 */
@NullMarked
public class TemplatedReadStatement extends AbstractStatement implements ReadStatementMixin, TemplatedStatement {

    private final String templateSql;
    private final TemplateArgumentMapping argumentMapping;

    /**
     * 构造模板读取语句
     *
     * @param templateSql SQL模板字符串
     */
    public TemplatedReadStatement(String templateSql) {
        this.templateSql = templateSql;
        this.argumentMapping = new TemplateArgumentMapping();
    }

    /**
     * 绑定参数
     *
     * @param binder 参数绑定器
     * @return 自身实例
     */
    public TemplatedReadStatement bindArguments(Handler<TemplateArgumentMapping> binder) {
        binder.handle(this.argumentMapping);
        return this;
    }


    @Override
    public String toString() {
        return this.build();
    }

    @Override
    public String getSqlTemplate() {
        return templateSql;
    }

    @Override
    public TemplateArgumentMapping getArguments() {
        return argumentMapping;
    }

}
