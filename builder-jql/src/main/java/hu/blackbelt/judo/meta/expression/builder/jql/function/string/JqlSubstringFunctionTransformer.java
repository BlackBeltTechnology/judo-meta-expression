package hu.blackbelt.judo.meta.expression.builder.jql.function.string;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.IntegerExpression;
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newSubStringBuilder;

public class JqlSubstringFunctionTransformer extends AbstractJqlFunctionTransformer<StringExpression> {

    public JqlSubstringFunctionTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(StringExpression argument, FunctionCall functionCall, ExpressionBuildingVariableResolver context) {
        IntegerExpression position = (IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context);
        IntegerExpression length = (IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(1).getExpression(), context);
        return newSubStringBuilder().withExpression(argument).withPosition(position).withLength(length).build();
    }
}
