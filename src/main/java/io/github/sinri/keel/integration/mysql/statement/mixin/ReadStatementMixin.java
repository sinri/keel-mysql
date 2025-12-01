package io.github.sinri.keel.integration.mysql.statement.mixin;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.integration.mysql.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.integration.mysql.result.row.ResultRow;
import io.github.sinri.keel.integration.mysql.result.stream.ResultStreamReader;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.core.Future;
import io.vertx.sqlclient.Cursor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


/**
 * 读取语句混合类，为读取操作提供各种查询功能
 *
 * @since 5.0.0
 */
public interface ReadStatementMixin extends AnyStatement {
    /**
     * @param namedMySQLConnection NamedMySQLConnection
     * @param classT               class of type of result object
     * @param <T>                  type of result object
     * @return 查询到数据，异步返回第一行数据封装的指定类实例；查询不到时异步返回null。
     */
    default <T extends ResultRow> Future<T> queryForOneRow(@NotNull NamedMySQLConnection namedMySQLConnection, @NotNull Class<T> classT) {
        return execute(namedMySQLConnection)
                .compose(resultMatrix -> {
                    try {
                        T t = resultMatrix.buildTableRowByIndex(0, classT);
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
    default <T extends ResultRow> Future<List<T>> queryForRowList(@NotNull NamedMySQLConnection namedMySQLConnection, @NotNull Class<T> classT) {
        return execute(namedMySQLConnection)
                .compose(resultMatrix -> {
                    List<T> ts = resultMatrix.buildTableRowList(classT);
                    return Future.succeededFuture(ts);
                });
    }

    default <K, T extends ResultRow> Future<Map<K, List<T>>> queryForCategorizedMap(
            @NotNull NamedMySQLConnection namedMySQLConnection,
            @NotNull Class<T> classT,
            @NotNull Function<T, K> categoryGenerator
    ) {
        Map<K, List<T>> map = new HashMap<>();
        return queryForRowList(namedMySQLConnection, classT)
                .compose(list -> {
                    list.forEach(item -> {
                        K category = categoryGenerator.apply(item);
                        map.computeIfAbsent(category, k -> new ArrayList<>()).add(item);
                    });
                    return Future.succeededFuture(map);
                });
    }


    default <K, T extends ResultRow> Future<Map<K, T>> queryForUniqueKeyBoundMap(
            @NotNull NamedMySQLConnection namedMySQLConnection,
            @NotNull Class<T> classT,
            @NotNull Function<T, K> uniqueKeyGenerator
    ) {
        Map<K, T> map = new HashMap<>();

        return queryForRowList(namedMySQLConnection, classT)
                .compose(list -> {
                    list.forEach(item -> {
                        K uniqueKey = uniqueKeyGenerator.apply(item);
                        map.put(uniqueKey, item);
                    });
                    return Future.succeededFuture(map);
                });
    }


    default Future<Void> stream(
            @NotNull NamedMySQLConnection namedMySQLConnection,
            @NotNull ResultStreamReader resultStreamReader
    ) {
        return namedMySQLConnection.getSqlConnection()
                                   .prepare(toString())
                                   .compose(preparedStatement -> {
                                       Cursor cursor = preparedStatement.cursor();

                                       Keel keel = namedMySQLConnection.getKeel();
                                       return keel.asyncCallRepeatedly(routineResult -> {
                                                      if (!cursor.hasMore()) {
                                                          routineResult.stop();
                                                          return Future.succeededFuture();
                                                      }

                                                      return cursor.read(1)
                                                                   .compose(rows -> keel.asyncCallIteratively(rows, resultStreamReader::read));
                                                  })
                                                  .eventually(cursor::close)
                                                  .eventually(preparedStatement::close);
                                   });
    }


}
