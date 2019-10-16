package hu.blackbelt.judo.meta.expression.builder.jql.function;

import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;

public abstract class AbstractJqlFunctionTransformer implements JqlFunctionTransformer {

    protected JqlTransformers jqlTransformers;

    protected AbstractJqlFunctionTransformer(JqlTransformers jqlTransformers) {
        this.jqlTransformers = jqlTransformers;
    }

}
