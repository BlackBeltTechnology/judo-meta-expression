package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;

public abstract class AbstractJqlExpressionTransformer<T extends JqlExpression, NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> implements JqlExpressionTransformerFunction {

    protected final JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers;

    protected AbstractJqlExpressionTransformer(JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers) {
        this.jqlTransformers = jqlTransformers;
    }

    @SuppressWarnings("unchecked")
    public final Expression apply(JqlExpression jqlExpression, ExpressionBuildingVariableResolver context) {
        return doTransform((T) jqlExpression, context);
    }

    protected abstract Expression doTransform(T jqlExpression, ExpressionBuildingVariableResolver context);

    protected ModelAdapter<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> getModelAdapter() {
        return jqlTransformers.getModelAdapter();
    }


}
