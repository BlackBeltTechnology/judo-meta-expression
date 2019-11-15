package hu.blackbelt.judo.meta.expression.builder.jql.function;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuildingContext;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

@FunctionalInterface
public interface JqlFunctionTransformer<B extends Expression> {

    Expression apply(B argument, FunctionCall functionCall, JqlExpressionBuildingContext context);

}
