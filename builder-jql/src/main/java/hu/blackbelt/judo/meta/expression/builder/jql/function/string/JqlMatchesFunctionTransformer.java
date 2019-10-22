package hu.blackbelt.judo.meta.expression.builder.jql.function.string;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import java.util.List;

import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newMatchesBuilder;


public class JqlMatchesFunctionTransformer extends AbstractJqlFunctionTransformer<StringExpression> {

    public JqlMatchesFunctionTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(StringExpression argument, FunctionCall functionCall, List<ObjectVariable> variables) {
        StringExpression parameter = (StringExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), variables);
        return newMatchesBuilder().withExpression(argument).withPattern(parameter).build();
    }
}
