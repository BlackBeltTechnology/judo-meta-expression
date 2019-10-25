package hu.blackbelt.judo.meta.expression.builder.jql.function.string;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import java.util.List;

import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newPositionBuilder;

public class JqlPositionFunctionTransformer extends AbstractJqlFunctionTransformer<StringExpression> {

    public JqlPositionFunctionTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(StringExpression argument, FunctionCall functionCall, List<ObjectVariable> variables) {
        StringExpression container = argument;
        StringExpression containment = (StringExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), variables);
        return newPositionBuilder().withContainer(container).withContainment(containment).build();
    }
}
