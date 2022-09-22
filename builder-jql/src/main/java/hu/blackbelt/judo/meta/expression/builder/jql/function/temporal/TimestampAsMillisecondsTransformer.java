package hu.blackbelt.judo.meta.expression.builder.jql.function.temporal;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.TimestampExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.numeric.util.builder.TimestampAsMillisecondsExpressionBuilder;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

public class TimestampAsMillisecondsTransformer extends AbstractJqlFunctionTransformer<TimestampExpression> {

    public TimestampAsMillisecondsTransformer(ExpressionTransformer jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(TimestampExpression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        return TimestampAsMillisecondsExpressionBuilder.create()
                                                       .withTimestamp(argument)
                                                       .build();
    }

}
