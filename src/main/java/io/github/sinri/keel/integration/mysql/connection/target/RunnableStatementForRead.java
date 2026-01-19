package io.github.sinri.keel.integration.mysql.connection.target;

import io.github.sinri.keel.integration.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.github.sinri.keel.integration.mysql.result.row.ResultRow;
import io.github.sinri.keel.integration.mysql.result.row.SimpleResultRow;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@NullMarked
public class RunnableStatementForRead extends RunnableStatement {
    public RunnableStatementForRead(AnyStatement<?> statement) {
        super(statement);
    }

    private <T extends ResultRow> Function<JsonObject, T> mapper(Class<T> classT) {
        Constructor<T> constructor;
        try {
            constructor = classT.getConstructor(JsonObject.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return jsonObject -> {
            try {
                return constructor.newInstance(jsonObject);
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * @param classT class of type of result object
     * @param <T>    type of result object
     * @return 查询到数据，异步返回第一行数据封装的指定类实例；查询不到时异步返回null。
     */
    public <T extends ResultRow> Future<@Nullable T> executeForOneRow(Class<T> classT) {
        return execute()
                .compose(statementExecuteResult -> {
                    try {
                        ResultMatrix<T> resultMatrix = statementExecuteResult.toMatrix(mapper(classT));
                        T t = resultMatrix.getFirstRow();
                        return Future.succeededFuture(t);
                    } catch (KeelSQLResultRowIndexError e) {
                        return Future.succeededFuture(null);
                    }
                });
    }

    /**
     * @param classT class of type of result object
     * @param <T>    type of result object
     * @return 查询到数据，异步返回所有行数据封装的指定类实例；查询不到时异步返回null。
     */
    public <T extends ResultRow> Future<List<T>> executeForRowList(Class<T> classT) {
        return execute()
                .compose(statementExecuteResult -> {
                    return Future.succeededFuture(statementExecuteResult.toMatrix(mapper(classT))
                                                                        .getRowList());
                });
    }

    public Future<ResultMatrix<SimpleResultRow>> executeForResultMatrix() {
        return execute()
                .compose(statementExecuteResult -> Future.succeededFuture(statementExecuteResult.toMatrix()));
    }

    public <R extends ResultRow> Future<ResultMatrix<R>> executeForResultMatrix(Class<R> clazz) {
        return execute()
                .compose(statementExecuteResult -> Future.succeededFuture(statementExecuteResult.toMatrix(clazz)));
    }

    public <R extends ResultRow> Future<ResultMatrix<R>> executeForResultMatrix(Function<JsonObject, R> mapper) {
        return execute()
                .compose(statementExecuteResult -> Future.succeededFuture(statementExecuteResult.toMatrix(mapper)));
    }

    public <K> Future<Map<K, List<SimpleResultRow>>> queryForCategorizedMap(Function<SimpleResultRow, K> categoryGenerator) {
        return executeForResultMatrix()
                .compose(resultMatrix -> resultMatrix.buildCategorizedRowsMap(categoryGenerator));
    }

    public <K, T extends ResultRow> Future<Map<K, List<T>>> queryForCategorizedMap(
            Class<T> classT,
            Function<T, K> categoryGenerator
    ) {
        return executeForResultMatrix(classT)
                .compose(resultMatrix -> resultMatrix.buildCategorizedRowsMap(categoryGenerator));
    }

    public <K, T extends ResultRow> Future<Map<K, T>> queryForUniqueKeyBoundMap(
            Class<T> classT,
            Function<T, K> uniqueKeyGenerator
    ) {
        return executeForResultMatrix(classT)
                .compose(resultMatrix -> resultMatrix.buildUniqueKeyBoundRowMap(uniqueKeyGenerator));
    }


}
