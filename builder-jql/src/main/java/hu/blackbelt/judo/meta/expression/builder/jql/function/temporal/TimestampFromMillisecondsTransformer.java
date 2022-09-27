package hu.blackbelt.judo.meta.expression.builder.jql.function.temporal;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionParameter;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import java.util.List;

import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampFromMillisecondsExpressionBuilder;

public class TimestampFromMillisecondsTransformer extends AbstractJqlFunctionTransformer<Expression> {

    public TimestampFromMillisecondsTransformer(ExpressionTransformer expressionTransformer) {
        super(expressionTransformer);
    }

    @Override
    public Expression apply(Expression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        if (argument instanceof TypeNameExpression){
            List<FunctionParameter> parameters = functionCall.getParameters();
            if (parameters.size() == 1) {
                Expression transformResult = expressionTransformer.transform(parameters.get(0).getExpression(), context);
                if (transformResult instanceof IntegerExpression) {
                    return newTimestampFromMillisecondsExpressionBuilder()
                            .withMilliseconds((IntegerExpression) transformResult)
                            .build();
                } else {
                    throw new IllegalArgumentException(String.format("Invalid parameter type: Expected %s. Got: %s",
                                                                     IntegerExpression.class.getSimpleName(),
                                                                     transformResult.getClass().getSimpleName()));
                }
            } else {
                throw new IllegalArgumentException("Invalid number of arguments. Expected: 1. Got: " + functionCall.getParameters().size());
            }
        } else {
            throw new IllegalArgumentException(String.format("Function %s not supported on %s", functionCall.getName(), argument.getClass().getSimpleName()));
        }
    }

}
