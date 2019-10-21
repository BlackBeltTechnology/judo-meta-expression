package hu.blackbelt.judo.meta.expression.builder.jql.function.collection;

import hu.blackbelt.judo.meta.expression.CollectionExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.IntegerExpression;
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.JqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

public class JqlIntegerParamCollectionFunctionTransformer implements JqlFunctionTransformer {

    private JqlTransformers jqlTransformers;
    private BiFunction<CollectionExpression, IntegerExpression, Expression> builderFunction;

    public JqlIntegerParamCollectionFunctionTransformer(JqlTransformers jqlTransformers, BiFunction<CollectionExpression, IntegerExpression, Expression> builderFunction) {
        this.jqlTransformers = jqlTransformers;
        this.builderFunction = builderFunction;
    }

    @Override
    public Expression apply(Expression argument, FunctionCall functionCall, List<ObjectVariable> variables) {
        CollectionExpression collectionExpression = (CollectionExpression) argument;
        Expression parameter = jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), variables);
        if (!(parameter instanceof IntegerExpression)) {
            throw new IllegalArgumentException("Function parameter must be integer, got: " + parameter);
        }
        return builderFunction.apply(collectionExpression, (IntegerExpression) parameter);
    }

}
