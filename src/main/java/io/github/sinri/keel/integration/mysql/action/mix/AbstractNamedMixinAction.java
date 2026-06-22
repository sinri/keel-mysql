package io.github.sinri.keel.integration.mysql.action.mix;

import io.github.sinri.keel.integration.mysql.action.single.AbstractNamedAction;
import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import org.jspecify.annotations.NullMarked;


/**
 * 抽象命名Mixin动作基类，继承AbstractNamedAction并实现NamedActionMixinInterface接口
 * 用于处理mixin风格的命名MySQL连接，通过SelfInterface提供fluent链式调用能力
 *
 * @param <C> 扩展NamedMySQLConnection的特定连接类
 * @param <W> 表示mixin或附加上下文的泛型类型
 * @since 5.0.0
 */
@NullMarked
public abstract class AbstractNamedMixinAction<C extends NamedMySQLConnection, W>
        extends AbstractNamedAction<C>
        implements NamedActionMixinInterface<C, W> {

    /**
     * 构造具有指定命名MySQL连接的抽象命名Mixin动作
     *
     * @param namedSqlConnection 与此动作关联的命名MySQL连接实例，不能为null
     */
    public AbstractNamedMixinAction(C namedSqlConnection) {
        super(namedSqlConnection);
    }
}
