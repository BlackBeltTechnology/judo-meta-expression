package hu.blackbelt.judo.meta.expression.builder.jql.function.temporal;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.*;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

public class TemporalAsMillisecondsTransformer extends AbstractJqlFunctionTransformer<Expression> {

    public TemporalAsMillisecondsTransformer(ExpressionTransformer jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(Expression expression, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        if (expression instanceof TimeExpression) {
            JqlTransformerUtils.validateParameterCount(functionCall.getName(), functionCall.getParameters(), 0);
            return NumericBuilders.newTimeAsMillisecondsExpressionBuilder().withTime((TimeExpression) expression).build();
        } else if (expression instanceof TimestampExpression) {
            JqlTransformerUtils.validateParameterCount(functionCall.getName(), functionCall.getParameters(), 0);
            return NumericBuilders.newTimestampAsMillisecondsExpressionBuilder().withTimestamp((TimestampExpression) expression).build();
        } else {
            throw new IllegalArgumentException("Function '" + functionCall.getName() + "' is not supported on " + expression.getClass().getSimpleName());
        }
    }

}
