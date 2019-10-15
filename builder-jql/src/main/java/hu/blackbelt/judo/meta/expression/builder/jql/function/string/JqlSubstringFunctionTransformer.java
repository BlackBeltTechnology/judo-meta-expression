package hu.blackbelt.judo.meta.expression.builder.jql.function.string;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.IntegerExpression;
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.JqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import java.util.List;

import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newPositionBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newSubStringBuilder;

public class JqlSubstringFunctionTransformer implements JqlFunctionTransformer {

    private JqlTransformers jqlTransformers;

    public JqlSubstringFunctionTransformer(JqlTransformers jqlTransformers) {
        this.jqlTransformers = jqlTransformers;
    }

    @Override
    public Expression apply(Expression argument, FunctionCall functionCall, List<ObjectVariable> variables) {
        IntegerExpression position = (IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(0), variables);
        IntegerExpression length = (IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(1), variables);
        return newSubStringBuilder().withExpression((StringExpression)argument).withPosition(position).withLength(length).build();
    }
}
