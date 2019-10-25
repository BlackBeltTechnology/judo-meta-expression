package hu.blackbelt.judo.meta.expression.builder.jql.function;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;

public abstract class AbstractJqlFunctionTransformer<B extends Expression> implements JqlFunctionTransformer<B> {

    protected final JqlTransformers jqlTransformers;

    protected AbstractJqlFunctionTransformer(JqlTransformers jqlTransformers) {
        this.jqlTransformers = jqlTransformers;
    }

}
