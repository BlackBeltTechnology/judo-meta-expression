package hu.blackbelt.judo.meta.expression.builder.jql.function.string;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.IntegerExpression;
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.JqlFunctionTransformer;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newSubStringBuilder;

public class JqlSubstringFunctionTransformer implements JqlFunctionTransformer<StringExpression> {

    private ExpressionTransformer jqlTransformers;

    public JqlSubstringFunctionTransformer(ExpressionTransformer jqlTransformers) {
        this.jqlTransformers = jqlTransformers;
    }

    @Override
    public Expression apply(StringExpression argument, FunctionCall functionCall, ExpressionBuildingVariableResolver context) {
        IntegerExpression position = (IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context);
        IntegerExpression length = (IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(1).getExpression(), context);
        return newSubStringBuilder().withExpression(argument).withPosition(position).withLength(length).build();
    }

}
