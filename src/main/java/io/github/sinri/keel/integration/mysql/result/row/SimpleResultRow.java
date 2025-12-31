package io.github.sinri.keel.integration.mysql.result.row;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;


/**
 * 简单结果行实现类，专为ResultMatrix中每行的包装器而设计
 *
 * @since 5.0.0
 */
public class SimpleResultRow extends JsonifiableDataUnitImpl implements ResultRow {
    private JsonObject row;

    /**
     * 构造简单结果行对象
     *
     * @param tableRow 表格行JSON对象
     */
    public SimpleResultRow(@NotNull JsonObject tableRow) {
        this.reloadData(tableRow);
    }

    /**
     * 转换为JSON对象
     *
     * @return JSON对象
     */
    @Override
    public final @NotNull JsonObject toJsonObject() {
        return row;
    }

    /**
     * 重新加载数据
     *
     * @param jsonObject JSON对象
     */
    @Override
    public final void reloadData(@NotNull JsonObject jsonObject) {
        this.row = jsonObject;
    }
}
