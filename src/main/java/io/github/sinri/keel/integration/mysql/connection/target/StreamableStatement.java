package io.github.sinri.keel.integration.mysql.connection.target;

import io.github.sinri.keel.base.async.KeelAsyncMixin;
import io.github.sinri.keel.integration.mysql.result.stream.ResultStreamReader;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.github.sinri.keel.logger.api.LateObject;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.Cursor;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class StreamableStatement extends AnyStatementWithSqlConnection implements KeelAsyncMixin {

    private final LateObject<Vertx> lateVertx = new LateObject<>();

    public StreamableStatement(AnyStatement<?> statement) {
        super(statement);
    }

    public final Future<Void> streamRead(ResultStreamReader resultStreamReader) {
        return streamRead(resultStreamReader, 1);
    }

    public final Future<Void> streamRead(ResultStreamReader resultStreamReader, int batch) {
        return getSqlConnection()
                .prepare(toString())
                .compose(preparedStatement -> {
                    Cursor cursor = preparedStatement.cursor();

                    return asyncCallRepeatedly(routineResult -> {
                        if (!cursor.hasMore()) {
                            routineResult.stop();
                            return Future.succeededFuture();
                        }

                        return cursor.read(batch)
                                     .compose(rows -> {
                                         return asyncCallIteratively(rows, resultStreamReader::read);
                                     });
                    })
                            .eventually(cursor::close)
                            .eventually(preparedStatement::close);
                });
    }

    @Override
    public Vertx getVertx() {
        return lateVertx.get();
    }

    public void setVertx(Vertx vertx) {
        lateVertx.set(vertx);
    }
}
