package io.github.sinri.keel.integration.mysql.result.matrix;

import io.github.sinri.keel.integration.mysql.result.row.SimpleResultRow;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.jspecify.annotations.NullMarked;

@NullMarked
class SimpleResultMatrix extends AbstractResultMatrix<SimpleResultRow> {

    public SimpleResultMatrix(RowSet<Row> rowSet) {
        super(
                rowSet.stream()
                      .map(row -> new SimpleResultRow(row.toJson()))
                      .toList()
        );
    }
}
