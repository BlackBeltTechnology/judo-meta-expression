package hu.blackbelt.judo.meta.expression.builder.jql.function.collection;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.collection.OrderByItem;
import hu.blackbelt.judo.meta.expression.collection.impl.SortExpressionImpl;
import hu.blackbelt.judo.meta.expression.collection.util.builder.SortExpressionBuilder;
import hu.blackbelt.judo.meta.expression.constant.BooleanConstant;
import hu.blackbelt.judo.meta.expression.variable.CollectionVariable;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionParameter;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;
import org.eclipse.emf.common.util.ECollections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newOrderByItemBuilder;
import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newSortExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newBooleanConstantBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newConcatenateCollectionBuilder;

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
            builder = builder.withOrderBy(orderByItems);
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
