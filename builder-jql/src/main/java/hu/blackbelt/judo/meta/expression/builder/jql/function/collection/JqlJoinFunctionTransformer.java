package hu.blackbelt.judo.meta.expression.builder.jql.function.collection;

import hu.blackbelt.judo.meta.expression.CollectionExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import java.util.List;

import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newConcatenateCollectionBuilder;

public class JqlJoinFunctionTransformer extends AbstractJqlFunctionTransformer<CollectionExpression> {

    public JqlJoinFunctionTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(CollectionExpression collection, FunctionCall functionCall, List<ObjectVariable> variables) {
        StringExpression text = (StringExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), variables);
        StringExpression separator = (StringExpression) jqlTransformers.transform(functionCall.getParameters().get(1).getExpression(), variables);
        return newConcatenateCollectionBuilder().withCollectionExpression(collection).withExpression(text).withSeparator(separator).build();
    }
}
