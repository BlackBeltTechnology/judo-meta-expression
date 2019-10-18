package hu.blackbelt.judo.meta.expression.builder.jql.function.string;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.IntegerExpression;
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import java.util.List;

import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newSubStringBuilder;

public class JqlSubstringFunctionTransformer extends AbstractJqlFunctionTransformer {

    public JqlSubstringFunctionTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(Expression argument, FunctionCall functionCall, List<ObjectVariable> variables) {
        IntegerExpression position = (IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), variables);
        IntegerExpression length = (IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(1).getExpression(), variables);
        return newSubStringBuilder().withExpression((StringExpression)argument).withPosition(position).withLength(length).build();
    }
}
