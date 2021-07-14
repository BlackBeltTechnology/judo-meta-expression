package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;

import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.*;
import static hu.blackbelt.judo.meta.expression.custom.util.builder.CustomBuilders.newCustomAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.enumeration.util.builder.EnumerationBuilders.newEnumerationAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newLogicalAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectNavigationExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newDateAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampAttributeBuilder;

public abstract class AbstractJqlExpressionTransformer<T extends JqlExpression, ME, NE extends ME, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S extends ME, M, U> implements JqlExpressionTransformerFunction {

    protected final JqlTransformers<ME, NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers;

    protected AbstractJqlExpressionTransformer(JqlTransformers<ME, NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers) {
        this.jqlTransformers = jqlTransformers;
    }

    @SuppressWarnings("unchecked")
    public final Expression apply(JqlExpression jqlExpression, ExpressionBuildingVariableResolver context) {
        return doTransform((T) jqlExpression, context);
    }

    protected abstract Expression doTransform(T jqlExpression, ExpressionBuildingVariableResolver context);

    protected ModelAdapter<ME, NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> getModelAdapter() {
        return jqlTransformers.getModelAdapter();
    }


}
