package hu.blackbelt.judo.meta.expression.builder.jql.function.temporal;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionParameter;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampFromMillisecondsExpressionBuilder;

public class TimestampConstructionTransformer extends AbstractJqlFunctionTransformer<TypeNameExpression> {

    private final ChronoUnit unit;
    private final ExpressionTransformer jqlTransformers;

    public TimestampConstructionTransformer(ExpressionTransformer jqlTransformers, ChronoUnit unit) {
        super(jqlTransformers);
        this.jqlTransformers = jqlTransformers;
        this.unit = unit;
    }

    @Override
    public Expression apply(TypeNameExpression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        List<FunctionParameter> parameters = functionCall.getParameters();
        if (parameters.size() == 1) {
            Expression transformResult = jqlTransformers.transform(parameters.get(0).getExpression(), context);
            if (transformResult instanceof IntegerExpression) {
                if (ChronoUnit.MILLIS.equals(unit)) {
                    return newTimestampFromMillisecondsExpressionBuilder()
                            .withMilliseconds((IntegerExpression) transformResult)
                            .build();
                } else {
                    throw new IllegalArgumentException("Timestamp creation is not supported for this type: " + unit);
                }
            } else {
                throw new IllegalArgumentException(String.format("Invalid parameter type: Expected %s. Got: %s",
                                                                 IntegerExpression.class.getSimpleName(),
                                                                 transformResult.getClass().getSimpleName()));
            }
        } else {
            throw new IllegalArgumentException("Invalid number of arguments. Expected: 1. Got: " + functionCall.getParameters().size());
        }
    }

}
