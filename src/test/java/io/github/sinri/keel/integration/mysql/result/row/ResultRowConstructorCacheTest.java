package io.github.sinri.keel.integration.mysql.result.row;

import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultRowConstructorCacheTest extends KeelJUnit5Test {

    public ResultRowConstructorCacheTest() {
        super();
    }

    @Test
    public void testConstructorCacheReusesLookup() {
        ResultRow.CONSTRUCTOR_CACHE.clear();

        JsonObject json = new JsonObject().put("id", 1).put("name", "alice");

        SimpleResultRow row1 = ResultRow.of(json, SimpleResultRow.class);
        assertEquals(1, ResultRow.CONSTRUCTOR_CACHE.size());
        assertTrue(ResultRow.CONSTRUCTOR_CACHE.containsKey(SimpleResultRow.class));
        assertEquals("alice", row1.readString("name"));

        // 第二次调用同一类应命中缓存，不新增条目
        SimpleResultRow row2 = ResultRow.of(json, SimpleResultRow.class);
        assertEquals(1, ResultRow.CONSTRUCTOR_CACHE.size());
        assertEquals("alice", row2.readString("name"));

        // 不同子类触发一次新的反射查找
        TestRow row3 = ResultRow.of(json, TestRow.class);
        assertEquals(2, ResultRow.CONSTRUCTOR_CACHE.size());
        assertTrue(ResultRow.CONSTRUCTOR_CACHE.containsKey(TestRow.class));
        assertEquals("alice", row3.readString("name"));

        // 重复访问 TestRow 仍命中缓存
        ResultRow.of(json, TestRow.class);
        assertEquals(2, ResultRow.CONSTRUCTOR_CACHE.size());
    }

    /**
     * 用于验证反射构造器缓存针对不同子类各自独立命中。
     */
    public static class TestRow extends SimpleResultRow {
        public TestRow(JsonObject tableRow) {
            super(tableRow);
        }
    }
}
