package io.github.sinri.keel.integration.mysql.result.row;

import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class SimpleResultRowTest extends KeelJUnit5Test {

    public SimpleResultRowTest() {
        super();
    }

    @Test
    public void test1() {
        SimpleResultRow row = new SimpleResultRow(new JsonObject()
                .put("a", "b"));
        List<SimpleResultRow> rows = List.of(row);
        getUnitTestLogger().info(new JsonObject().put("rows", rows).encode());
    }

    @Test
    public void readJsonReturnsJsonObjectAsIs() {
        JsonObject payload = new JsonObject().put("k", 1);
        SimpleResultRow row = new SimpleResultRow(new JsonObject().put("doc", payload));

        Object result = row.readJson("doc");

        JsonObject jo = assertInstanceOf(JsonObject.class, result);
        assertSame(payload, jo);
        assertEquals(1, jo.getInteger("k"));
    }

    @Test
    public void readJsonReturnsJsonArrayAsIs() {
        JsonArray payload = new JsonArray().add(1).add(2);
        SimpleResultRow row = new SimpleResultRow(new JsonObject().put("doc", payload));

        Object result = row.readJson("doc");

        JsonArray ja = assertInstanceOf(JsonArray.class, result);
        assertSame(payload, ja);
        assertEquals(2, ja.size());
    }

    @Test
    public void readJsonParsesObjectStringLiteral() {
        SimpleResultRow row = new SimpleResultRow(new JsonObject()
                .put("doc", "  {\"k\":\"v\"}  "));

        Object result = row.readJson("doc");

        JsonObject jo = assertInstanceOf(JsonObject.class, result);
        assertEquals("v", jo.getString("k"));
    }

    @Test
    public void readJsonParsesArrayStringLiteral() {
        SimpleResultRow row = new SimpleResultRow(new JsonObject()
                .put("doc", "[1,2,3]"));

        Object result = row.readJson("doc");

        JsonArray ja = assertInstanceOf(JsonArray.class, result);
        assertEquals(3, ja.size());
        assertEquals(1, ja.getInteger(0));
    }

    @Test
    public void readJsonReturnsNullWhenAbsentOrNull() {
        SimpleResultRow row = new SimpleResultRow(new JsonObject().putNull("doc"));
        assertNull(row.readJson("doc"));
        assertNull(row.readJson("missing"));
    }

    @Test
    public void readJsonPreservesScalarValues() {
        SimpleResultRow row = new SimpleResultRow(new JsonObject()
                .put("num", 42)
                .put("bool", true)
                .put("plain", "hello"));

        assertEquals(42, row.readJson("num"));
        assertEquals(true, row.readJson("bool"));
        assertEquals("hello", row.readJson("plain"));
    }
}