package io.github.sinri.keel.integration.mysql.statement.impl;

import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 3.2.19
 * The OUT and INOUT of Parameters for CALLed Stored Procedures are not supported.
 */
public class CallStatement extends AbstractStatement {
    private final List<String> parameters = new ArrayList<>();
    private String storedProcedureName = null;

    public CallStatement setStoredProcedureName(String storedProcedureName) {
        this.storedProcedureName = storedProcedureName;
        return this;
    }

    protected CallStatement addParameterExpression(@NotNull String parameter, @Nullable String parameterPrefix) {
        this.parameters.add((parameterPrefix == null ? "" : (parameterPrefix + " ")) + parameter);
        return this;
    }

    public CallStatement addParameterExpression(@NotNull String parameter) {
        return addParameterExpression(parameter, null);
    }

    public CallStatement addOutParameterExpression(@NotNull String parameter) {
        return addParameterExpression(parameter, "OUT");
    }

    public CallStatement addInOutParameterExpression(@NotNull String parameter) {
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
