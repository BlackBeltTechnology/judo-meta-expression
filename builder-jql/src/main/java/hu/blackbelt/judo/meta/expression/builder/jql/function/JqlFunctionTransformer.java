package hu.blackbelt.judo.meta.expression.builder.jql.function;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

@FunctionalInterface
public interface JqlFunctionTransformer<B extends Expression> {

    Expression apply(B argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context);

}
