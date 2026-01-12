package io.github.sinri.keel.integration.mysql.result.row;

import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.List;

class SimpleResultRowTest extends KeelJUnit5Test {

    public SimpleResultRowTest(Vertx vertx) {
        super(vertx);
    }

    @Test
    public void test1() {
        SimpleResultRow row = new SimpleResultRow(new JsonObject()
                .put("a", "b"));
        List<SimpleResultRow> rows = List.of(row);
        getUnitTestLogger().info(new JsonObject().put("rows", rows).encode());
    }
}