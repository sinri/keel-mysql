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
public class StreamableStatement extends AnyStatementWithSqlConnection {
    private final LateObject<KeelAsyncMixin> lateKeelAsyncMixin = new LateObject<>();

    public StreamableStatement(AnyStatement<?> statement) {
        super(statement);
    }

    public StreamableStatement attach(Vertx vertx) {
        lateKeelAsyncMixin.set(KeelAsyncMixin.wrap(vertx));
        return this;
    }

    public StreamableStatement attach(KeelAsyncMixin keelAsyncMixin) {
        lateKeelAsyncMixin.set(keelAsyncMixin);
        return this;
    }

    protected final KeelAsyncMixin getKeelAsyncMixin() {
        return lateKeelAsyncMixin.get();
    }

    public final Future<Void> stream(ResultStreamReader resultStreamReader) {
        return getSqlConnection()
                .prepare(toString())
                .compose(preparedStatement -> {
                    Cursor cursor = preparedStatement.cursor();


                    return getKeelAsyncMixin()
                            .asyncCallRepeatedly(routineResult -> {
                                if (!cursor.hasMore()) {
                                    routineResult.stop();
                                    return Future.succeededFuture();
                                }

                                return cursor.read(1)
                                             .compose(rows -> getKeelAsyncMixin().asyncCallIteratively(rows, resultStreamReader::read));
                            })
                            .eventually(cursor::close)
                            .eventually(preparedStatement::close);
                });
    }
}
