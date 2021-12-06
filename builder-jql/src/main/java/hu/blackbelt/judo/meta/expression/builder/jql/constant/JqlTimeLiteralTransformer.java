package hu.blackbelt.judo.meta.expression.builder.jql.constant;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlExpressionTransformerFunction;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.TimeLiteral;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newTimeConstantBuilder;

public class JqlTimeLiteralTransformer implements JqlExpressionTransformerFunction {

    @Override
    public Expression apply(JqlExpression expression, ExpressionBuildingVariableResolver context) {
        TimeLiteral timestampLiteral = (TimeLiteral) expression;
        String stringValue = timestampLiteral.getValue();
        LocalTime localTime = LocalTime.parse(stringValue, DateTimeFormatter.ISO_LOCAL_TIME);
        return newTimeConstantBuilder().withValue(localTime).build();
    }

}
