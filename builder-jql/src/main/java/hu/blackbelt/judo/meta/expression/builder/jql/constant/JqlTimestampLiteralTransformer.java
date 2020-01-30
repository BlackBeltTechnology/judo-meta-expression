package hu.blackbelt.judo.meta.expression.builder.jql.constant;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlExpressionTransformerFunction;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.TimeStampLiteral;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newTimestampConstantBuilder;

public class JqlTimestampLiteralTransformer implements JqlExpressionTransformerFunction {

    @Override
    public Expression apply(JqlExpression expression, ExpressionBuildingVariableResolver context) {
        TimeStampLiteral timestampLiteral = (TimeStampLiteral) expression;
        return newTimestampConstantBuilder().withValue(timestampLiteral.getValue()).build();
    }

}
