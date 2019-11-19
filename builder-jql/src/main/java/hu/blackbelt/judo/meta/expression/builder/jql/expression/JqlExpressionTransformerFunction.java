package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuildingContext;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;

import java.util.function.BiFunction;

public interface JqlExpressionTransformerFunction extends BiFunction<JqlExpression, JqlExpressionBuildingContext, Expression> {

}
