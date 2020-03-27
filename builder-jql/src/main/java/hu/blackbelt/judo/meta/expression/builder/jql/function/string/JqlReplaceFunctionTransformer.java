package hu.blackbelt.judo.meta.expression.builder.jql.function.string;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newReplaceBuilder;

public class JqlReplaceFunctionTransformer extends AbstractJqlFunctionTransformer<StringExpression> {

    public JqlReplaceFunctionTransformer(ExpressionTransformer jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(StringExpression argument, FunctionCall functionCall, ExpressionBuildingVariableResolver context) {
        StringExpression pattern = (StringExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context);
        StringExpression replacement = (StringExpression) jqlTransformers.transform(functionCall.getParameters().get(1).getExpression(), context);
        return newReplaceBuilder().withExpression(argument).withPattern(pattern).withReplacement(replacement).build();
    }
}
