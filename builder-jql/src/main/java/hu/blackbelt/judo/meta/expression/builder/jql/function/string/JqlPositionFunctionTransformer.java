package hu.blackbelt.judo.meta.expression.builder.jql.function.string;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.AbstractJqlExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.JqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import java.util.List;

import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newPositionBuilder;

public class JqlPositionFunctionTransformer extends AbstractJqlFunctionTransformer {

    public JqlPositionFunctionTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(Expression argument, FunctionCall functionCall, List<ObjectVariable> variables) {
        StringExpression container = (StringExpression) argument;
        StringExpression containment = (StringExpression) jqlTransformers.transform(functionCall.getParameters().get(0), variables);
        return newPositionBuilder().withContainer(container).withContainment(containment).build();
    }
}
