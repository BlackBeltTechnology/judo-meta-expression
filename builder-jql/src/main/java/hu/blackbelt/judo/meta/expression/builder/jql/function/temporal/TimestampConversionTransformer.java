package hu.blackbelt.judo.meta.expression.builder.jql.function.temporal;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.TimestampExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.numeric.TimestampConversion;
import hu.blackbelt.judo.meta.expression.numeric.util.builder.TimestampConversionExpressionBuilder;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

public class TimestampConversionTransformer extends AbstractJqlFunctionTransformer<TimestampExpression> {

    private final TimestampConversion timestampConversion;

    public TimestampConversionTransformer(ExpressionTransformer jqlTransformers, TimestampConversion timestampConversion) {
        super(jqlTransformers);
        this.timestampConversion = timestampConversion;
    }

    @Override
    public Expression apply(TimestampExpression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        if (timestampConversion == TimestampConversion.MILLISEC) {
            return TimestampConversionExpressionBuilder.create()
                                                       .withTimestamp(argument)
                                                       .withTimestampConversion(timestampConversion)
                                                       .build();
        } else {
            throw new IllegalArgumentException("Timestamp conversion not supported with unit " + timestampConversion);
        }
    }

}
