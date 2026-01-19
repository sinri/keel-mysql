package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.result.pagination.PaginationResult;
import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.github.sinri.keel.integration.mysql.statement.impl.SelectStatement;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;

public class Usage {
    public NamedMySQLConnection namedMySQLConnection;

    public Future<ResultMatrix> querySth1() {
        return namedMySQLConnection.select(s -> s
                                           .from("table_1")
                                           .where(conditionsComponent -> conditionsComponent
                                                   .expressionEqualsNumericValue("id", 1))
                                   )
                                   .execute();
    }

    public Future<ResultMatrix> querySth2() {
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
                .execute()
                .compose(resultMatrix -> {
                    return resultMatrix.buildCategorizedRowsMap(
                            jsonObject -> {
                                return jsonObject.getInteger("attribute_key");
                            }
                    );
                });
    }
    public Future<PaginationResult> querySth4() {
        return new SelectStatement()
                .from("table_1")
                .where(conditionsComponent -> conditionsComponent
                        .expressionEqualsNumericValue("id", 1))
                .attachToConnection(namedMySQLConnection.getSqlConnection())
                .executeForPagination(3,100);
    }
}
