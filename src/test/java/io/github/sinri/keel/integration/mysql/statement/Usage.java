package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.result.StatementExecuteResult;
import io.github.sinri.keel.integration.mysql.result.pagination.PaginationResult;
import io.github.sinri.keel.integration.mysql.statement.impl.SelectStatement;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Usage {
    public NamedMySQLConnection namedMySQLConnection;

    public Future<StatementExecuteResult> querySth1() {
        return namedMySQLConnection.select(s -> s
                                           .from("table_1")
                                           .where(conditionsComponent -> conditionsComponent
                                                   .expressionEqualsNumericValue("id", 1))
                                   )
                                   .execute();
    }

    public Future<StatementExecuteResult> querySth2() {
        return new SelectStatement()
                .from("table_1")
                .where(conditionsComponent -> conditionsComponent
                        .expressionEqualsNumericValue("id", 1))
                .attachToConnection(namedMySQLConnection.getSqlConnection())
                .execute();
    }

    public Future<Map<Integer, List<JsonObject>>> querySth3() {
        return new SelectStatement()
                .from("table_1")
                .where(conditionsComponent -> conditionsComponent
                        .expressionEqualsNumericValue("id", 1))
                .attachToConnection(namedMySQLConnection.getSqlConnection())
                .executeForCategorizedMap(x -> {
                    return x.readIntegerRequired("attribute_key");
                })
                .compose(map -> {
                    Map<Integer, List<JsonObject>> result = new ConcurrentHashMap<>();
                    map.forEach((key, value) -> {
                        List<JsonObject> list = value.stream().map(JsonifiableDataUnitImpl::toJsonObject).toList();
                        result.put(key, list);
                    });
                    return Future.succeededFuture(result);
                });
    }

    public Future<PaginationResult> querySth4() {
        return new SelectStatement()
                .from("table_1")
                .where(conditionsComponent -> conditionsComponent
                        .expressionEqualsNumericValue("id", 1))
                .attachToConnection(namedMySQLConnection.getSqlConnection())
                .executeForPagination(3, 100);
    }
}
