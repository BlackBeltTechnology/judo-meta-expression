package hu.blackbelt.judo.meta.expression.builder.jql.function.temporal;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.TimeExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.*;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.numeric.util.builder.TimeAsSecondsExpressionBuilder;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

public class TimeAsSecondsTransformer extends AbstractJqlFunctionTransformer<Expression> {

    public TimeAsSecondsTransformer(ExpressionTransformer jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(Expression expression, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        TimeExpression timeExpression = JqlTransformerUtils.castExpression(TimeExpression.class, () -> expression, functionCall.getName() + " is not supported on {1}");
        JqlTransformerUtils.validateParameterCount(functionCall.getName(), functionCall.getParameters(), 0);
        return TimeAsSecondsExpressionBuilder.create().withTime(timeExpression).build();
    }

}
