package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.Expression;

import java.util.Collection;

@lombok.Getter
@lombok.Builder
public class EvaluationNode {

    private Collection<Expression> expressions;

    private Collection<EvaluationNode> navigations;
}
