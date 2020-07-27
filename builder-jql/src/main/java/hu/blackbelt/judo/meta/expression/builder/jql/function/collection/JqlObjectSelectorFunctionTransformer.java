package hu.blackbelt.judo.meta.expression.builder.jql.function.collection;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.collection.CollectionVariableReference;
import hu.blackbelt.judo.meta.expression.collection.OrderByItem;
import hu.blackbelt.judo.meta.expression.collection.SortExpression;
import hu.blackbelt.judo.meta.expression.constant.BooleanConstant;
import hu.blackbelt.judo.meta.expression.operator.ObjectSelector;
import hu.blackbelt.judo.meta.expression.variable.CollectionVariable;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.util.ArrayList;
import java.util.Collection;

import static hu.blackbelt.judo.meta.expression.builder.jql.function.collection.JqlSortFunctionTransformer.isDescending;
import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.*;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newBooleanConstantBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectSelectorExpressionBuilder;

public class JqlObjectSelectorFunctionTransformer extends AbstractJqlFunctionTransformer<CollectionExpression> {

    private ObjectSelector selector;

    public JqlObjectSelectorFunctionTransformer(ExpressionTransformer jqlTransformers, ObjectSelector selector) {
        super(jqlTransformers);
        this.selector = selector;
    }

    @Override
    public Expression apply(CollectionExpression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        Expression result;
        if ((functionCall.getParameters() == null || functionCall.getParameters().isEmpty()) && argument instanceof SortExpression) {
            result = createDefaultResult((SortExpression) argument);
        } else {
            Collection<OrderByItem> orderByItems = new ArrayList<>();
            functionCall.getParameters().forEach((param) -> {
                        DataExpression orderByItemExpression = (DataExpression) jqlTransformers.transform(param.getExpression(), context);
                        BooleanConstant orderByItemDesc = newBooleanConstantBuilder().withValue(isDescending(param.getParameterExtension())).build();
                        orderByItems.add(newOrderByItemBuilder().withExpression(orderByItemExpression).withDescending(orderByItemDesc.isValue()).build());
                    }
            );
            result = newObjectSelectorExpressionBuilder()
                    .withCollectionExpression(argument)
                    .withOrderBy(orderByItems)
                    .withOperator(selector)
                    .build();
        }
        return result;
    }

    private Expression createDefaultResult(SortExpression argument) {
        SortExpression sortExpression = argument;
        return newObjectSelectorExpressionBuilder()
                    .withCollectionExpression(sortExpression)
                    .withOperator(selector)
                    .build();
    }
}
