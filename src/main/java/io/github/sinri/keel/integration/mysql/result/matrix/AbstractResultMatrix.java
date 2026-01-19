package io.github.sinri.keel.integration.mysql.result.matrix;

import io.github.sinri.keel.integration.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.integration.mysql.result.row.ResultRow;
import org.jspecify.annotations.NullMarked;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@NullMarked
class AbstractResultMatrix<R extends ResultRow> implements ResultMatrix<R> {
    private final List<R> array;

    protected AbstractResultMatrix(List<R> array) {
        this.array = array;
    }

    @Override
    final public int size() {
        return array.size();
    }

    @Override
    final public R getFirstRow() throws KeelSQLResultRowIndexError {
        return getRowByIndex(0);
    }

    @Override
    final public R getRowByIndex(int index) throws KeelSQLResultRowIndexError {
        try {
            return array.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new KeelSQLResultRowIndexError(e);
        }
    }

    @Override
    public List<R> getRowList() {
        return Collections.unmodifiableList(array);
    }

    @Override
    final public Stream<R> stream() {
        return array.stream();
    }

    @Override
    final public Iterator<R> iterator() {
        return array.iterator();
    }
}
