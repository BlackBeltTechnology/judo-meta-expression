package hu.blackbelt.judo.meta.expression.builder.jql.function;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;

public abstract class AbstractJqlFunctionTransformer<B extends Expression> implements JqlFunctionTransformer<B> {

    protected final ExpressionTransformer expressionTransformer;

    protected AbstractJqlFunctionTransformer(ExpressionTransformer jqlTransformers) {
        this.expressionTransformer = jqlTransformers;
    }

}
