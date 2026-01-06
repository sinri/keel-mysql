package io.github.sinri.keel.integration.mysql.statement.component;

import io.github.sinri.keel.integration.mysql.Quoter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * CASE操作符类，用于构建MySQL CASE表达式
 * <p>
 * {@code CASE value WHEN compare_value THEN result [WHEN compare_value THEN result ...] [ELSE result] END }
 * </p>
 * <p>
 * {@code CASE WHEN condition THEN result [WHEN condition THEN result ...] [ELSE result] END }
 * </p>
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/flow-control-functions.html#operator_case">
 *         Case operator</a>
 * @since 5.0.0
 */
@NullMarked
public class CaseOperator {
    private final Collection<CaseOperatorPair> whenThenPairs;
    private @Nullable String caseValueExpression = null;
    private @Nullable String elseResultExpression = null;

    public CaseOperator() {
        this.whenThenPairs = new ArrayList<>();
    }

    public CaseOperator setCaseValueAsNumber(Number caseValueAsNumber) {
        this.caseValueExpression = String.valueOf(caseValueAsNumber);
        return this;
    }

    public CaseOperator setCaseValueAsString(String caseValueAsString) {
        this.caseValueExpression = new Quoter(caseValueAsString).toString();
        return this;
    }

    public CaseOperator setElseResultAsNumber(String elseResultAsNumber) {
        this.elseResultExpression = elseResultAsNumber;
        return this;
    }

    public CaseOperator setElseResultAsString(String elseResultAsString) {
        this.elseResultExpression = new Quoter(elseResultAsString).toString();
        return this;
    }

    public CaseOperator addWhenThenPair(CaseOperatorPair caseOperatorPair) {
        this.whenThenPairs.add(caseOperatorPair);
        return this;
    }

    public @Nullable String getCaseValueExpression() {
        return caseValueExpression;
    }

    public CaseOperator setCaseValueExpression(String caseValueExpression) {
        this.caseValueExpression = caseValueExpression;
        return this;
    }


    public Collection<CaseOperatorPair> getWhenThenPairs() {
        return whenThenPairs;
    }

    public @Nullable String getElseResultExpression() {
        return elseResultExpression;
    }

    public CaseOperator setElseResultExpression(String elseResultExpression) {
        this.elseResultExpression = elseResultExpression;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CASE");
        if (!(Objects.requireNonNullElse(getCaseValueExpression(), "")).isBlank()) {
            sb.append(" ").append(getCaseValueExpression());
        }
        getWhenThenPairs().forEach(pair -> sb.append(" WHEN ").append(pair.getWhenExpression())
                                             .append(" THEN ").append(pair.getThenExpression()));
        if (!(Objects.requireNonNullElse(getElseResultExpression(), "")).isBlank()) {
            sb.append(" ELSE ").append(getElseResultExpression());
        }
        sb.append(" END");
        return sb.toString();
    }
}
