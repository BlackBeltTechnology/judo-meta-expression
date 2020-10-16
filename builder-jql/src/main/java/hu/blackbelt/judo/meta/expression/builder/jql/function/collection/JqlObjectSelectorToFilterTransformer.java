package hu.blackbelt.judo.meta.expression.builder.jql.function.collection;

import hu.blackbelt.judo.meta.expression.*;
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
import org.eclipse.emf.ecore.EObject;
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
        LogicalExpression condition;
        if (!(argument instanceof CollectionExpression)) {
            throw new IllegalArgumentException("Expected iterable collection");
        }
        CollectionExpression filteringBase = EcoreUtil.copy(argument);
        AugmentedCopier copier = new AugmentedCopier();
        sortingExpression.eContents().stream().filter(e -> e instanceof VariableReference)
                .forEach(variableReference -> {
                    copier.copy(variableReference);
                    copier.copyReferences();
                });
        copier.setUseOriginalReferences(false);
        DataExpression sortingExpressionCopy = (DataExpression) copier.copy(sortingExpression);
        copier.copyReferences();
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

    /* This class is used to refine copying logic. We can prepare some copies (containing references to original objects), but then make sure we use the existing copy if needed.
     */
    private static class AugmentedCopier extends EcoreUtil.Copier {

        public void setUseOriginalReferences(boolean v) {
            this.useOriginalReferences = v;
        }

        @Override
        protected EObject createCopy(EObject eObject) {
            if (containsKey(eObject)) {
                return get(eObject);
            } else {
                return super.createCopy(eObject);
            }
        }
    }

}
