package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.FilteringExpression;
import hu.blackbelt.judo.meta.expression.WindowingExpression;
import hu.blackbelt.judo.meta.expression.collection.SortExpression;

import java.util.Collection;

@lombok.Getter
@lombok.Builder
public class SetTransformation {

    private String alias;

    private Collection<EvaluationNode<FilteringExpression>> filtering;

    private Collection<EvaluationNode<SortExpression>> ordering;

    private EvaluationNode<WindowingExpression> windowingExpression;

    @Override
    public String toString() {
        return "[" + alias + "|" +
                (filtering != null && !filtering.isEmpty() ? "F:" + filtering : "") +
                (ordering != null && !ordering.isEmpty() ? "O:" + ordering : "") +
                (windowingExpression != null ? "W:" + windowingExpression : "") +
                "]";
    }
}
