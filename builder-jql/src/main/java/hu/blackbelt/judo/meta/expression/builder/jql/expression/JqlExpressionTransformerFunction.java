package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;

import java.util.List;
import java.util.function.BiFunction;

public interface JqlExpressionTransformerFunction extends BiFunction<hu.blackbelt.judo.meta.jql.jqldsl.Expression, List<ObjectVariable>, Expression> {

}
