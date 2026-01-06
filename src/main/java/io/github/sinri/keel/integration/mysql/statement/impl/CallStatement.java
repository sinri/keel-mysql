package io.github.sinri.keel.integration.mysql.statement.impl;

import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * CALL语句类，用于构建和执行存储过程调用
 * <p>不支持存储过程中CALL参数的OUT和INOUT模式。</p>
 *
 * @since 5.0.0
 */
@NullMarked
public class CallStatement extends AbstractStatement {
    private final List<String> parameters = new ArrayList<>();
    private @Nullable String storedProcedureName = null;

    public CallStatement setStoredProcedureName(String storedProcedureName) {
        this.storedProcedureName = storedProcedureName;
        return this;
    }

    protected CallStatement addParameterExpression(String parameter, @Nullable String parameterPrefix) {
        this.parameters.add((parameterPrefix == null ? "" : (parameterPrefix + " ")) + parameter);
        return this;
    }

    public CallStatement addParameterExpression(String parameter) {
        return addParameterExpression(parameter, null);
    }

    public CallStatement addOutParameterExpression(String parameter) {
        return addParameterExpression(parameter, "OUT");
    }

    public CallStatement addInOutParameterExpression(String parameter) {
        return addParameterExpression(parameter, "INOUT");
    }

    @Override
    public String toString() {
        String s = "CALL " + storedProcedureName;
        s += "(";
        if (!parameters.isEmpty()) {
            s += String.join(", ", parameters);
        }
        s += ")";
        return s;
    }
}
