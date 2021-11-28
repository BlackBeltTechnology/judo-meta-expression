package hu.blackbelt.judo.meta.expression.builder.jql.function.temporal;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.IntegerExpression;
import hu.blackbelt.judo.meta.expression.TypeNameExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.*;

public class ConstructorTransformer<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> extends AbstractJqlFunctionTransformer<TypeNameExpression> {

    private JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers;

    public ConstructorTransformer(JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers) {
        super(jqlTransformers);
        this.jqlTransformers = jqlTransformers;
    }

    @Override
    public Expression apply(TypeNameExpression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        NE ne = jqlTransformers.getModelAdapter().get(argument).get();
        if (jqlTransformers.getModelAdapter().isPrimitiveType(ne)) {
            P primitiveType = (P) ne;
            if (jqlTransformers.getModelAdapter().isDate(primitiveType)) {
                if (functionCall.getParameters().size() != 3) {
                    throw new IllegalArgumentException("3 parameters expected");
                }
                return newDateConstructionExpressionBuilder()
                        .withYear((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context))
                        .withMonth((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(1).getExpression(), context))
                        .withDay((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(2).getExpression(), context))
                        .build();
            } else if (jqlTransformers.getModelAdapter().isTimestamp(primitiveType)) {
                if (functionCall.getParameters().size() != 6) {
                    throw new IllegalArgumentException("6 parameters expected");
                }
                return newTimestampConstructionExpressionBuilder()
                        .withYear((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context))
                        .withMonth((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(1).getExpression(), context))
                        .withDay((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(2).getExpression(), context))
                        .withHour((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(3).getExpression(), context))
                        .withMinute((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(4).getExpression(), context))
                        .withSecond((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(5).getExpression(), context))
                        .build();
            } else if (jqlTransformers.getModelAdapter().isTime(primitiveType)) {
                if (functionCall.getParameters().size() != 3) {
                    throw new IllegalArgumentException("3 parameters expected");
                }
                return newTimeConstructionExpressionBuilder()
                        .withHour((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context))
                        .withMinute((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(1).getExpression(), context))
                        .withSecond((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(2).getExpression(), context))
                        .build();
            } else {
                throw new IllegalArgumentException("Function is not valid for given type");
            }
        } else {
            throw new IllegalArgumentException("Type is not valid for environment variable");
        }
    }
}
