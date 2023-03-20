package hu.blackbelt.judo.meta.expression.builder.jql.function.collection;

/*-
 * #%L
 * JUDO :: Expression :: Model
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.operation.JqlBinaryOperationTransformer;
import hu.blackbelt.judo.meta.expression.collection.*;
import hu.blackbelt.judo.meta.expression.logical.ContainsExpression;
import hu.blackbelt.judo.meta.expression.logical.ObjectComparison;
import hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders;
import hu.blackbelt.judo.meta.expression.object.ObjectSelectorExpression;
import hu.blackbelt.judo.meta.expression.object.util.builder.ObjectSelectorExpressionBuilder;
import hu.blackbelt.judo.meta.expression.object.util.builder.ObjectVariableReferenceBuilder;
import hu.blackbelt.judo.meta.expression.operator.ObjectComparator;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionParameter;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import static hu.blackbelt.judo.meta.expression.builder.jql.function.collection.JqlObjectSelectorToFilterTransformer.ObjectSelector.*;
import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.*;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newExistsBuilder;

public class JqlObjectSelectorToFilterTransformer extends AbstractJqlFunctionTransformer<CollectionExpression> {

    public enum ObjectSelector {
        HEAD, TAIL, HEADS, TAILS, ANY;
    }

    private final ObjectSelector selector;
    private JqlTransformers jqlTransformers;

    public JqlObjectSelectorToFilterTransformer(JqlTransformers jqlTransformers, ObjectSelector selector) {
        super(jqlTransformers);
        this.jqlTransformers = jqlTransformers;
        this.selector = selector;
    }

    @Override
    public Expression apply(CollectionExpression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        Expression result;
        if (!(argument instanceof CollectionExpression)) {
            throw new IllegalArgumentException("Expected iterable collection");
        }
        LogicalExpression condition;
        AugmentedCopier copier = new AugmentedCopier();

        if (selector == ANY || (functionCall.getParameters() == null || functionCall.getParameters().isEmpty()) && argument instanceof SortExpression) {
            result = new JqlAnyFunctionTransformer(expressionTransformer).apply(argument, null, context);
            if (context.peekBaseExpression() instanceof CollectionExpression) {
                result = retransformResult(result, null);
            }
        } else {
            FunctionParameter functionParameter = functionCall.getParameters().get(0);
            DataExpression sortingExpression = (DataExpression) expressionTransformer.transform(functionParameter.getExpression(), context);
            boolean descending = JqlSortFunctionTransformer.isDescending(functionParameter.getParameterExtension());
            CollectionExpression filteringBase = EcoreUtil.copy(argument);
            sortingExpression.eContents().stream().filter(e -> e instanceof VariableReference)
                    .forEach(variableReference -> {
                        copier.copy(variableReference);
                        copier.copyReferences();
                    });
            copier.setUseOriginalReferences(false);
            DataExpression sortingExpressionCopy = (DataExpression) copier.copy(sortingExpression);
            copier.copyReferences();
            AggregatedExpression aggregationExpression;
            JqlAggregatedExpressionTransformer aggregatedExpressionTransformer = ((selector == HEAD || selector == HEADS) && descending
                    || (selector == TAIL || selector == TAILS) && !descending) ? JqlAggregatedExpressionTransformer.createMaxInstance(expressionTransformer)
                            : JqlAggregatedExpressionTransformer.createMinInstance(expressionTransformer);
            aggregationExpression = aggregatedExpressionTransformer.createAggregatedExpression(filteringBase, sortingExpressionCopy);
            condition = (LogicalExpression) new JqlBinaryOperationTransformer((JqlTransformers) expressionTransformer).createBinaryOperationExpression(sortingExpression,
                    aggregationExpression, "==");
            CollectionFilterExpression collectionFilterExpression = newCollectionFilterExpressionBuilder()
                    .withCollectionExpression(argument)
                    .withCondition(condition)
                    .build();
            if (selector == HEADS || selector == TAILS) {
                result = collectionFilterExpression;
                if (context.peekBaseExpression() instanceof CollectionExpression) {
                    result = retransformResult(result, aggregationExpression);
                }
            } else {
                result = new JqlAnyFunctionTransformer(expressionTransformer).apply(collectionFilterExpression, null, context);
                if (context.peekBaseExpression() instanceof CollectionExpression) {
                    result = retransformResult(result, aggregationExpression);
                }
            }

        }
        return result;
    }

    private Expression retransformResult(Expression exp, AggregatedExpression aggregationExpression) {
        // If in a derived expression, we need to transform. See transformation rules in JNG-1700.
        // The object selector expression will be used in an other filter
        // self.classes.students!filter(s | (s.height == self=>classes=>students!max(s | s.height)))!any() =>
        // self.classes.students!filter(_s | self.classes!exists( _c | _c.students!filter(s | (s.height == self=>classes=>students!max(s | s.height)))!any() == _s))
        // in general
        // from: SELECTOR.RELATION!FILTER!any()
        // to: SELECTOR.RELATION!filter(_s | SELECTOR!exists(_c | _c.RELATION!FILTER!any() == _s))
        // This is supported only for cases there is a SELECTOR.RELATION structure. If not, then we keep it as is.
        Expression resultBaseExpression = exp;
        boolean single;
        CollectionExpression filter;
        if (resultBaseExpression instanceof ObjectSelectorExpression) {
            single = true;
            filter = ((ObjectSelectorExpression) resultBaseExpression).getCollectionExpression();
        } else {
            single = false;
            filter = (CollectionExpression) exp;
        }
        // the filter should be a collection filter expression
        if (filter instanceof CollectionFilterExpression) {
            CollectionFilterExpression collectionFilterExpression = (CollectionFilterExpression) filter;
            CollectionExpression filteredCollection = collectionFilterExpression.getCollectionExpression();
            // the filtered collection should be in selector.relation structure, that is some kind of CollectionNavigation
            CollectionExpression newNavigation = null;
            CollectionExpression selector = null;
            String relation = null;
            if (filteredCollection instanceof CollectionNavigationFromCollectionExpression) {
                selector = ((CollectionNavigationFromCollectionExpression) filteredCollection).getCollectionExpression();
                relation = ((CollectionNavigationFromCollectionExpression) filteredCollection).getReferenceName();
                newNavigation = newCollectionNavigationFromCollectionExpressionBuilder()
                        .withCollectionExpression(EcoreUtil.copy(selector))
                        .withReferenceName(relation)
                        .build();
                newNavigation.createIterator(jqlTransformers.newGeneratedIteratorName(), jqlTransformers.getExpressionBuilder().getModelAdapter(),
                        jqlTransformers.getExpressionBuilder().getExpressionResource());
            } else if (filteredCollection instanceof ObjectNavigationFromCollectionExpression) {
                selector = ((ObjectNavigationFromCollectionExpression) filteredCollection).getCollectionExpression();
                relation = ((ObjectNavigationFromCollectionExpression) filteredCollection).getReferenceName();
                newNavigation = newCollectionNavigationFromCollectionExpressionBuilder()
                        .withCollectionExpression(EcoreUtil.copy(selector))
                        .withReferenceName(relation)
                        .build();
                newNavigation.createIterator(jqlTransformers.newGeneratedIteratorName(), jqlTransformers.getExpressionBuilder().getModelAdapter(),
                        jqlTransformers.getExpressionBuilder().getExpressionResource());
            }
            if (newNavigation != null) {
                ObjectVariable _s = newNavigation.getIteratorVariable();
                CollectionExpression internalSelector = EcoreUtil.copy(selector);
                ObjectVariable _c = internalSelector.createIterator(jqlTransformers.newGeneratedIteratorName(), jqlTransformers.getExpressionBuilder().getModelAdapter(),
                        jqlTransformers.getExpressionBuilder().getExpressionResource());
                CollectionNavigationFromObjectExpression internalFiltered = newCollectionNavigationFromObjectExpressionBuilder()
                        .withObjectExpression(ObjectVariableReferenceBuilder.create().withVariable(_c).build())
                        .withReferenceName(relation).build();
                internalFiltered.setIteratorVariable(filteredCollection.getIteratorVariable());
                if (aggregationExpression != null) {
                    aggregationExpression.setCollectionExpression(EcoreUtil.copy(internalFiltered));
                }
                resultBaseExpression = newCollectionFilterExpressionBuilder()
                        .withCollectionExpression(newNavigation)
                        .withCondition(
                                newExistsBuilder()
                                        .withCollectionExpression(internalSelector)
                                        .withCondition(
                                                single ? existsConditionSingle(collectionFilterExpression, internalFiltered, _s)
                                                        : existsConditionMulti(collectionFilterExpression, internalFiltered, _s))
                                        .build())
                        .build();
            }
        }
        return resultBaseExpression;
    }

    public ObjectComparison existsConditionSingle(CollectionFilterExpression collectionFilterExpression, CollectionNavigationFromObjectExpression internalFiltered, ObjectVariable newNavigationIterator) {
        return LogicalBuilders.newObjectComparisonBuilder()
                .withLeft(
                        ObjectSelectorExpressionBuilder.create().withCollectionExpression(
                                newCollectionFilterExpressionBuilder()
                                        .withCollectionExpression(internalFiltered)
                                        .withCondition(collectionFilterExpression.getCondition()).build())
                                .withOperator(hu.blackbelt.judo.meta.expression.operator.ObjectSelector.ANY)
                                .build())
                .withRight(ObjectVariableReferenceBuilder.create().withVariable(newNavigationIterator).build())
                .withOperator(ObjectComparator.EQUAL).build();
    }

    public ContainsExpression existsConditionMulti(CollectionFilterExpression collectionFilterExpression, CollectionNavigationFromObjectExpression internalFiltered, ObjectVariable newNavigationIterator) {
        return LogicalBuilders.newContainsExpressionBuilder()
                .withCollectionExpression(newCollectionFilterExpressionBuilder()
                        .withCollectionExpression(internalFiltered)
                        .withCondition(collectionFilterExpression.getCondition()).build())
                .withObjectExpression(ObjectVariableReferenceBuilder.create().withVariable(newNavigationIterator).build())
                .build();
    }


    /*
     * This class is used to refine copying logic. We can prepare some copies (containing references to original objects), but then make sure we use the
     * existing copy if needed.
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
