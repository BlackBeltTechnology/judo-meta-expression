package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.FilteringExpression;
import hu.blackbelt.judo.meta.expression.WindowingExpression;
import hu.blackbelt.judo.meta.expression.collection.SortExpression;

import java.util.Collection;

@lombok.Getter
@lombok.Builder
@lombok.ToString
public class SetTransformation {

    private String alias;

    private Collection<EvaluationNode<FilteringExpression>> filtering;

    private Collection<EvaluationNode<SortExpression>> ordering;

    private EvaluationNode<WindowingExpression> windowingExpression;
}
