package hu.blackbelt.judo.meta.expression.builder.jql.function.collection;

import hu.blackbelt.judo.meta.expression.CollectionExpression;
import hu.blackbelt.judo.meta.expression.DataExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.LogicalExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.operation.JqlBinaryOperationTransformer;
import hu.blackbelt.judo.meta.expression.collection.CollectionFilterExpression;
import hu.blackbelt.judo.meta.expression.collection.CollectionVariableReference;
import hu.blackbelt.judo.meta.expression.collection.SortExpression;
import hu.blackbelt.judo.meta.expression.object.ObjectVariableReference;
import hu.blackbelt.judo.meta.expression.variable.CollectionVariable;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionParameter;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;
import org.eclipse.emf.ecore.util.EcoreUtil;

import static hu.blackbelt.judo.meta.expression.builder.jql.function.collection.JqlObjectSelectorToFilterTransformer.ObjectSelector.*;
import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newCollectionFilterExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectSelectorExpressionBuilder;

public class JqlObjectSelectorToFilterTransformer extends AbstractJqlFunctionTransformer<CollectionExpression> {

    public enum ObjectSelector {
        HEAD, TAIL, HEADS, TAILS;
    }

    private final ObjectSelector selector;

    public JqlObjectSelectorToFilterTransformer(JqlTransformers jqlTransformers, ObjectSelector selector) {
        super(jqlTransformers);
        this.selector = selector;
    }

    @Override
    public Expression apply(CollectionExpression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        Expression result;
        if ((functionCall.getParameters() == null || functionCall.getParameters().isEmpty()) && argument instanceof SortExpression) {
            return newObjectSelectorExpressionBuilder()
                    .withCollectionExpression(argument)
                    .withOperator(hu.blackbelt.judo.meta.expression.operator.ObjectSelector.ANY)
                    .build();
        }
        FunctionParameter functionParameter = functionCall.getParameters().get(0);
        DataExpression sortingExpression = (DataExpression) jqlTransformers.transform(functionParameter.getExpression(), context);
        boolean descending = JqlSortFunctionTransformer.isDescending(functionParameter.getParameterExtension());
        LogicalExpression condition = null;
        if (!(argument instanceof CollectionVariable || argument instanceof CollectionVariableReference)) {
            throw new IllegalArgumentException("Expected iterable collection");
        }
        CollectionVariable collectionWithVariable = argument instanceof CollectionVariable ? (CollectionVariable) argument : ((CollectionVariableReference) argument).getVariable();
        ObjectVariable iteratorVariable = collectionWithVariable.getIteratorVariable();
        CollectionExpression filteringBase = EcoreUtil.copy(argument);
        CollectionVariable filteringBaseWithVariable = filteringBase instanceof CollectionVariable ? (CollectionVariable) filteringBase : ((CollectionVariableReference) filteringBase).getVariable();
        ObjectVariable filteringBaseIterator = filteringBaseWithVariable.getIteratorVariable();
        DataExpression sortingExpressionCopy = EcoreUtil.copy(sortingExpression);
        sortingExpressionCopy.eContents().stream().filter(e -> e instanceof ObjectVariableReference && ((ObjectVariableReference) e).getVariable().equals(iteratorVariable))
                .forEach(objectVariableReference -> {
                    ((ObjectVariableReference) objectVariableReference).setVariable(filteringBaseIterator);
                });
        Expression aggregationExpression;
        JqlAggregatedExpressionTransformer aggregatedExpressionTransformer =
                ((selector == HEAD || selector == HEADS) && descending || (selector == TAIL || selector == TAILS) && !descending) ?
                        JqlAggregatedExpressionTransformer.createMaxInstance(jqlTransformers)
                        : JqlAggregatedExpressionTransformer.createMinInstance(jqlTransformers);
        aggregationExpression = aggregatedExpressionTransformer.createAggregatedExpression(filteringBase, sortingExpressionCopy);
        condition = (LogicalExpression) new JqlBinaryOperationTransformer((JqlTransformers) jqlTransformers).createBinaryOperationExpression(sortingExpression, aggregationExpression, "==");

        CollectionFilterExpression collectionFilterExpression = newCollectionFilterExpressionBuilder()
                .withCollectionExpression(argument)
                .withCondition(condition)
                .build();
        if (selector == HEADS || selector == TAILS) {
            result = collectionFilterExpression;
        } else {
            result = new JqlAnyFunctionTransformer(jqlTransformers).apply(collectionFilterExpression, null, context);
        }
        return result;
    }

}
