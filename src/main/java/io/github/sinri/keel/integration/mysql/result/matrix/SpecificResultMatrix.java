package io.github.sinri.keel.integration.mysql.result.matrix;

import io.github.sinri.keel.integration.mysql.result.row.ResultRow;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.util.function.Function;

class SpecificResultMatrix<R extends ResultRow> extends AbstractResultMatrix<R> {

    public SpecificResultMatrix(RowSet<Row> rowSet, Function<JsonObject, R> mapper) {
        super(
                rowSet.stream()
                      .map(row -> mapper.apply(row.toJson()))
                      .toList()
        );
    }

}
