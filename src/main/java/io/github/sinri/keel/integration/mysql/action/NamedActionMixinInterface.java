package io.github.sinri.keel.integration.mysql.action;

import io.github.sinri.keel.base.annotations.SelfInterface;
import io.github.sinri.keel.integration.mysql.NamedMySQLConnection;
import org.jetbrains.annotations.NotNull;

/**
 * Mixin风格命名MySQL动作接口，定义了mixin风格的命名MySQL动作接口
 *
 * 该接口扩展了{@link SelfInterface}，提供了获取与动作关联的命名MySQL连接的方法，
 * 使用此连接可以执行SQL语句。
 * 所有对MySQL连接的操作都应该包装在由高层管理的事务中；
 * 即不要在任何动作内管理事务。
 *
 * @param <C> 扩展NamedMySQLConnection的特定连接类
 * @param <W> 表示mixin或附加上下文的泛型类型
 * @since 5.0.0
 */
public interface NamedActionMixinInterface<C extends NamedMySQLConnection, W>
        extends SelfInterface<W> {
    /**
     * 获取关联的命名MySQL连接
     * @return 与此动作关联的命名MySQL连接实例，永不为null
     */
    @NotNull
    C getNamedSqlConnection();
}
