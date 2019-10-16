package hu.blackbelt.judo.meta.expression.builder.jql.function.string;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.IntegerExpression;
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.JqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import java.util.List;

import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newReplaceBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newSubStringBuilder;

public class JqlReplaceFunctionTransformer extends AbstractJqlFunctionTransformer {

    public JqlReplaceFunctionTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(Expression argument, FunctionCall functionCall, List<ObjectVariable> variables) {
        StringExpression pattern = (StringExpression) jqlTransformers.transform(functionCall.getParameters().get(0), variables);
        StringExpression replacement = (StringExpression) jqlTransformers.transform(functionCall.getParameters().get(1), variables);
        return newReplaceBuilder().withExpression((StringExpression)argument).withPattern(pattern).withReplacement(replacement).build();
    }
}
