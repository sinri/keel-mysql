package io.github.sinri.keel.integration.mysql.connection.target;

import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.integration.mysql.result.stream.ResultStreamReader;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.core.Future;
import io.vertx.sqlclient.Cursor;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class StreamableStatement extends AnyStatementWithSqlConnection {


    private final Keel keel;

    public StreamableStatement(Keel keel, AnyStatement<?> statement) {
        super(statement);
        this.keel = keel;
    }

    public final Future<Void> streamRead(ResultStreamReader resultStreamReader) {
        return streamRead(resultStreamReader, 1);
    }

    public final Future<Void> streamRead(ResultStreamReader resultStreamReader, int batch) {
        return getSqlConnection()
                .prepare(toString())
                .compose(preparedStatement -> {
                    Cursor cursor = preparedStatement.cursor();

                    return getKeel().asyncCallRepeatedly(routineResult -> {
                                        if (!cursor.hasMore()) {
                                            routineResult.stop();
                                            return Future.succeededFuture();
                                        }

                                        return cursor.read(batch)
                                                     .compose(rows -> {
                                                         return getKeel().asyncCallIteratively(rows, resultStreamReader::read);
                                                     });
                                    })
                                    .eventually(cursor::close)
                                    .eventually(preparedStatement::close);
                });
    }

    public Keel getKeel() {
        return keel;
    }
}
