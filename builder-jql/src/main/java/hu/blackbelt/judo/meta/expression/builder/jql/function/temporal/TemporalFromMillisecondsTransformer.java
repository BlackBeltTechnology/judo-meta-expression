package hu.blackbelt.judo.meta.expression.builder.jql.function.temporal;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.*;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.temporal.util.builder.TimeFromMillisecondsExpressionBuilder;
import hu.blackbelt.judo.meta.expression.temporal.util.builder.TimestampFromMillisecondsExpressionBuilder;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

public class TemporalFromMillisecondsTransformer<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> extends AbstractJqlFunctionTransformer<Expression> {

    private final JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers;

    public TemporalFromMillisecondsTransformer(JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers) {
        super(jqlTransformers);
        this.jqlTransformers = jqlTransformers;
    }

    @Override
    public Expression apply(Expression expression, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        TypeNameExpression typeNameExpression = JqlTransformerUtils.castExpression(TypeNameExpression.class, () -> expression, functionCall.getName() + " is not supported on {1}");

        NE ne = jqlTransformers.getModelAdapter().get(typeNameExpression).orElseThrow();
        if (jqlTransformers.getModelAdapter().isPrimitiveType(ne) && jqlTransformers.getModelAdapter().isTime((P) ne)) {
            JqlTransformerUtils.validateParameterCount(functionCall.getName(), functionCall.getParameters(), 1);
            IntegerExpression milliseconds = JqlTransformerUtils.castExpression(IntegerExpression.class, () -> expressionTransformer.transform(functionCall.getParameters().get(0).getExpression(), context));
            return TimeFromMillisecondsExpressionBuilder.create().withMilliseconds(milliseconds).build();
        } else if (jqlTransformers.getModelAdapter().isPrimitiveType(ne) && jqlTransformers.getModelAdapter().isTimestamp((P) ne)) {
            JqlTransformerUtils.validateParameterCount(functionCall.getName(), functionCall.getParameters(), 1);
            IntegerExpression milliseconds = JqlTransformerUtils.castExpression(IntegerExpression.class, () -> expressionTransformer.transform(functionCall.getParameters().get(0).getExpression(), context));
            return TimestampFromMillisecondsExpressionBuilder.create().withMilliseconds(milliseconds).build();
        } else {
            throw new IllegalArgumentException("Function '" + functionCall.getName() + "' is not supported on " + jqlTransformers.getModelAdapter().getName(ne).orElse("<unknown primitive>"));
        }

    }

}
