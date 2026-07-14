package io.github.sinri.keel.integration.mysql.result;

import io.vertx.sqlclient.PropertyKind;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.desc.ColumnDescriptor;
import io.vertx.sqlclient.desc.RowDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatementExecuteResultTest {

    @Test
    void aggregatesEveryResultInBatchChain() {
        TestRowSet third = new TestRowSet(2, 4, null);
        TestRowSet second = new TestRowSet(0, 3, third);
        TestRowSet first = new TestRowSet(1, 2, second);

        StatementExecuteResult result = new StatementExecuteResult(first);

        assertEquals(3, result.getTotalFetchedRows());
        assertEquals(9, result.getTotalAffectedRows());
        assertEquals(3, result.getRowSets().size());
        assertSame(first, result.getRowSet());
        assertSame(first, result.getRowSets().get(0));
        assertSame(second, result.getRowSets().get(1));
        assertSame(third, result.getRowSets().get(2));
        assertThrows(UnsupportedOperationException.class, () -> result.getRowSets().add(first));
    }

    @Test
    void preservesSingleResultStatistics() {
        TestRowSet only = new TestRowSet(5, 7, null);

        StatementExecuteResult result = new StatementExecuteResult(only);

        assertEquals(5, result.getTotalFetchedRows());
        assertEquals(7, result.getTotalAffectedRows());
        assertEquals(List.of(only), result.getRowSets());
    }

    private static final class TestRowSet implements RowSet<Row> {
        private final int size;
        private final int rowCount;
        private final RowSet<Row> next;

        private TestRowSet(int size, int rowCount, RowSet<Row> next) {
            this.size = size;
            this.rowCount = rowCount;
            this.next = next;
        }

        @Override
        public RowIterator<Row> iterator() {
            return new RowIterator<>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public Row next() {
                    throw new NoSuchElementException();
                }
            };
        }

        @Override
        public RowSet<Row> next() {
            return next;
        }

        @Override
        public int rowCount() {
            return rowCount;
        }

        @Override
        public RowDescriptor rowDescriptor() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> columnsNames() {
            return List.of();
        }

        @Override
        public List<ColumnDescriptor> columnDescriptors() {
            return List.of();
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public <V> V property(PropertyKind<V> propertyKind) {
            return null;
        }

        @Override
        public RowSet<Row> value() {
            return this;
        }
    }
}
