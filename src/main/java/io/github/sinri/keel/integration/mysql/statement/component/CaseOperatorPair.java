package io.github.sinri.keel.integration.mysql.statement.component;

import io.github.sinri.keel.integration.mysql.Quoter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;


/**
 * CASE操作符配对类，用于定义CASE语句中的WHEN-THEN对
 *
 * @since 5.0.0
 */
@NullMarked
public class CaseOperatorPair {
    private @Nullable String whenExpression;
    private @Nullable String thenExpression;

    public CaseOperatorPair() {

    }

    public CaseOperatorPair setThenAsNumber(Number thenAsNumber) {
        this.thenExpression = String.valueOf(thenAsNumber);
        return this;
    }

    public CaseOperatorPair setThenAsString(@Nullable String thenAsString) {
        this.thenExpression = new Quoter(thenAsString).toString();
        return this;
    }

    public CaseOperatorPair setWhenAsNumber(Number whenAsNumber) {
        this.whenExpression = String.valueOf(whenAsNumber);
        return this;
    }

    public CaseOperatorPair setWhenAsString(@Nullable String whenAsString) {
        this.whenExpression = new Quoter(whenAsString).toString();
        return this;
    }


    public @Nullable String getWhenExpression() {
        return whenExpression;
    }

    public CaseOperatorPair setWhenExpression(String whenExpression) {
        this.whenExpression = whenExpression;
        return this;
    }


    public @Nullable String getThenExpression() {
        return thenExpression;
    }

    public CaseOperatorPair setThenExpression(String thenExpression) {
        this.thenExpression = thenExpression;
        return this;
    }
}
