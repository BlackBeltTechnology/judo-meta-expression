package hu.blackbelt.judo.meta.expression.builder.jql.function.collection;

import hu.blackbelt.judo.meta.expression.CollectionExpression;
import hu.blackbelt.judo.meta.expression.DataExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.collection.OrderByItem;
import hu.blackbelt.judo.meta.expression.collection.util.builder.SortExpressionBuilder;
import hu.blackbelt.judo.meta.expression.constant.BooleanConstant;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import java.util.ArrayList;
import java.util.List;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newOrderByItemBuilder;
import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newSortExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newBooleanConstantBuilder;

public class JqlSortFunctionTransformer extends AbstractJqlFunctionTransformer<CollectionExpression> {

    public JqlSortFunctionTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(CollectionExpression collection, FunctionCall functionCall, List<ObjectVariable> variables) {
        List<OrderByItem> orderByItems = new ArrayList<>();
        functionCall.getParameters().forEach((param) -> {
                    DataExpression orderByItemExpression = (DataExpression) jqlTransformers.transform(param.getExpression(), variables);
                    BooleanConstant orderByItemDesc = newBooleanConstantBuilder().withValue(isDescending(param.getParameterExtension())).build();
                    orderByItems.add(newOrderByItemBuilder().withExpression(orderByItemExpression).withDescending(orderByItemDesc.isValue()).build());
                }
        );
        SortExpressionBuilder builder = newSortExpressionBuilder().withCollectionExpression(collection);
        if (!orderByItems.isEmpty()) {
            builder.withOrderBy(orderByItems);
        }
        return builder.build();
    }

    private boolean isDescending(String parameterExtension) {
        if (parameterExtension == null || parameterExtension.equalsIgnoreCase("asc")) {
            return false;
        } else if (parameterExtension.equalsIgnoreCase("desc")) {
            return true;
        } else {
            throw new IllegalArgumentException(("Invalid sorting direction " + parameterExtension));
        }
    }
}
