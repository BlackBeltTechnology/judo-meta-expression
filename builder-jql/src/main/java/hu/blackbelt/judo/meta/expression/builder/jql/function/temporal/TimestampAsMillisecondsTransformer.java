package hu.blackbelt.judo.meta.expression.builder.jql.function.temporal;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.TimestampExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.numeric.util.builder.TimestampAsMillisecondsExpressionBuilder;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

public class TimestampAsMillisecondsTransformer extends AbstractJqlFunctionTransformer<Expression> {

    public TimestampAsMillisecondsTransformer(ExpressionTransformer jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(Expression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        if (argument instanceof TimestampExpression){
            return TimestampAsMillisecondsExpressionBuilder.create()
                                                           .withTimestamp((TimestampExpression) argument)
                                                           .build();
        } else {
            throw new IllegalArgumentException(String.format("Function %s not supported on %s", functionCall.getName(), argument.getClass().getSimpleName()));
        }
    }

}
