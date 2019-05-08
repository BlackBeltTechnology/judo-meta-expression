package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.DataExpression;
import hu.blackbelt.judo.meta.expression.Expression;

import java.util.Map;

@lombok.Getter
@lombok.Builder
@lombok.ToString
public class EvaluationNode<T extends Expression> {

    private T expression;

    private Map<String, Expression> terminals;

    private Map<SetTransformation, EvaluationNode> navigations;

    private Map<String, DataExpression> operations;
}
