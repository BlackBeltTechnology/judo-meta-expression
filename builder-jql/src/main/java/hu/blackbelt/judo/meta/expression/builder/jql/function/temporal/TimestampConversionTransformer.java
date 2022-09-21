package hu.blackbelt.judo.meta.expression.builder.jql.function.temporal;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.TimestampExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.numeric.TimestampConversion;
import hu.blackbelt.judo.meta.expression.numeric.util.builder.TimestampConversionExpressionBuilder;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import java.time.temporal.ChronoUnit;

public class TimestampConversionTransformer extends AbstractJqlFunctionTransformer<TimestampExpression> {

    private final ChronoUnit conversionUnit;

    public TimestampConversionTransformer(ExpressionTransformer jqlTransformers, ChronoUnit timestampConversion) {
        super(jqlTransformers);
        this.conversionUnit = timestampConversion;
    }

    @Override
    public Expression apply(TimestampExpression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        if (conversionUnit == ChronoUnit.MILLIS) {
            return TimestampConversionExpressionBuilder.create()
                                                       .withTimestamp(argument)
                                                       .withTimestampConversion(TimestampConversion.MILLISEC)
                                                       .build();
        } else {
            throw new IllegalArgumentException("Timestamp conversion not supported with unit " + conversionUnit);
        }
    }

}
