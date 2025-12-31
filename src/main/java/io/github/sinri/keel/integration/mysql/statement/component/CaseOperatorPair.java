package io.github.sinri.keel.integration.mysql.statement.component;

import io.github.sinri.keel.integration.mysql.Quoter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * CASE操作符配对类，用于定义CASE语句中的WHEN-THEN对
 *
 * @since 5.0.0
 */
public class CaseOperatorPair {
    private String whenExpression;
    private String thenExpression;

    public CaseOperatorPair() {

    }

    public CaseOperatorPair setThenAsNumber(@NotNull Number thenAsNumber) {
        this.thenExpression = String.valueOf(thenAsNumber);
        return this;
    }

    public CaseOperatorPair setThenAsString(@Nullable String thenAsString) {
        this.thenExpression = new Quoter(thenAsString).toString();
        return this;
    }

    public CaseOperatorPair setWhenAsNumber(@NotNull Number whenAsNumber) {
        this.whenExpression = String.valueOf(whenAsNumber);
        return this;
    }

    public CaseOperatorPair setWhenAsString(@Nullable String whenAsString) {
        this.whenExpression = new Quoter(whenAsString).toString();
        return this;
    }

    @NotNull
    public String getWhenExpression() {
        return whenExpression;
    }

    public CaseOperatorPair setWhenExpression(@NotNull String whenExpression) {
        this.whenExpression = whenExpression;
        return this;
    }

    @NotNull
    public String getThenExpression() {
        return thenExpression;
    }

    public CaseOperatorPair setThenExpression(@NotNull String thenExpression) {
        this.thenExpression = thenExpression;
        return this;
    }
}
