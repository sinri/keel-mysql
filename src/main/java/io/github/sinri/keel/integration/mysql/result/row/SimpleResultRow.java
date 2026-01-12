package io.github.sinri.keel.integration.mysql.result.row;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;


/**
 * 简单结果行实现类，专为ResultMatrix中每行的包装器而设计
 *
 * @since 5.0.0
 */
@NullMarked
public class SimpleResultRow extends JsonifiableDataUnitImpl implements ResultRow {

    /**
     * 构造简单结果行对象
     *
     * @param tableRow 表格行JSON对象
     */
    public SimpleResultRow(JsonObject tableRow) {
        super(tableRow);
    }
}
