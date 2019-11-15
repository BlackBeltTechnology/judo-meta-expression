package hu.blackbelt.judo.meta.expression.builder.jql.function.collection;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.IntegerExpression;
import hu.blackbelt.judo.meta.expression.OrderedCollectionExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuildingContext;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.operator.ObjectSelector;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import java.math.BigInteger;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newSubCollectionExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newIntegerConstantBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectSelectorExpressionBuilder;

public class JqlHeadFunctionTransformer extends AbstractJqlFunctionTransformer<OrderedCollectionExpression> {

    public JqlHeadFunctionTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(OrderedCollectionExpression argument, FunctionCall functionCall, JqlExpressionBuildingContext context) {
        if (functionCall.getParameters() == null || functionCall.getParameters().isEmpty()) {
            return newObjectSelectorExpressionBuilder().withCollectionExpression(argument).withOperator(ObjectSelector.HEAD).build();
        } else {
            Expression parameter = jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context);
            if (!(parameter instanceof IntegerExpression)) {
                throw new IllegalArgumentException("Function parameter must be integer, got: " + parameter);
            }
            return newSubCollectionExpressionBuilder()
                    .withCollectionExpression(argument)
                    .withLength((IntegerExpression) parameter)
                    .withPosition(newIntegerConstantBuilder().withValue(BigInteger.ZERO).build()).build();
        }
    }
}
