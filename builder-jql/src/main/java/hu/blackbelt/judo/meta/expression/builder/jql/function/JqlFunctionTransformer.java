package hu.blackbelt.judo.meta.expression.builder.jql.function;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import java.util.List;

@FunctionalInterface
public interface JqlFunctionTransformer {

    Expression apply(Expression argument, FunctionCall functionCall, List<ObjectVariable> variables);

}
