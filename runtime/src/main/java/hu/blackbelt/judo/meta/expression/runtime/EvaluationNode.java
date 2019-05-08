package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.Expression;

import java.util.Collection;
import java.util.Map;

@lombok.Getter
@lombok.Builder
@lombok.ToString
public class EvaluationNode {

    private Expression expression;

    private Collection<Expression> terminals;

    private Map<String, EvaluationNode> navigations;
}
