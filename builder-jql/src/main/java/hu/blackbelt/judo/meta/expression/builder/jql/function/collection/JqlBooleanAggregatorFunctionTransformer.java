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
import hu.blackbelt.judo.meta.expression.builder.jql.*;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.operator.LogicalOperator;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;
import org.eclipse.emf.ecore.util.EcoreUtil;

import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.*;

public class JqlBooleanAggregatorFunctionTransformer extends AbstractJqlFunctionTransformer<Expression> {

    private final BooleanAggregatorType booleanAggregatorType;

    public enum BooleanAggregatorType {
        ANY_TRUE, ALL_TRUE, ANY_FALSE, ALL_FALSE
    }

    public JqlBooleanAggregatorFunctionTransformer(ExpressionTransformer jqlTransformers, BooleanAggregatorType booleanAggregatorType) {
        super(jqlTransformers);
        this.booleanAggregatorType = booleanAggregatorType;
    }

    @Override
    public Expression apply(Expression expression, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        CollectionExpression collection = JqlTransformerUtils.castExpression(CollectionExpression.class, () -> expression, functionCall.getName() + " not supported on {1}");
        JqlTransformerUtils.validateParameterCount(functionCall.getName(), functionCall.getParameters(), 1);
        LogicalExpression condition = JqlTransformerUtils.castExpression(LogicalExpression.class, () -> expressionTransformer.transform(functionCall.getParameters().get(0).getExpression(), context));

        // [COLLECTION]!anyTrue([VAR] | [CONDITION])  => same
        // [COLLECTION]!allTrue([VAR] | [CONDITION])  => [COLLECTION]!allTrue([VAR] | [CONDITION]!isDefined() AND [CONDITION])
        // [COLLECTION]!anyFalse([VAR] | [CONDITION]) => [COLLECTION]!anyFalse([VAR] | not [CONDITION]) 
        // [COLLECTION]!allFalse([VAR] | [CONDITION]) => [COLLECTION]!allFalse([VAR] | (not [CONDITION])!isDefined() AND (not [CONDITION]))

        // For allTrue and allFalse the extra undefined check must be added because in SQL instead of forall-like function
        //   EXISTS function is used with negated condition.

        switch (booleanAggregatorType) {
            case ANY_TRUE:
                return newExistsBuilder()
                        .withCollectionExpression(collection)
                        .withCondition(condition)
                        .build();
            case ALL_TRUE:
                return newForAllBuilder()
                        .withCollectionExpression(collection)
                        .withCondition(newKleeneExpressionBuilder()
                                               .withLeft(newNegationExpressionBuilder()
                                                                 .withExpression(newUndefinedComparisonBuilder()
                                                                                         .withExpression(condition)
                                                                                         .build())
                                                                 .build())
                                               .withOperator(LogicalOperator.AND)
                                               .withRight(EcoreUtil.copy(condition))
                                               .build())
                        .build();
            case ANY_FALSE:
                return newExistsBuilder()
                        .withCollectionExpression(collection)
                        .withCondition(newNegationExpressionBuilder()
                                               .withExpression(condition)
                                               .build())
                        .build();
            case ALL_FALSE:
                return newForAllBuilder()
                        .withCollectionExpression(collection)
                        .withCondition(newKleeneExpressionBuilder()
                                               .withLeft(newNegationExpressionBuilder()
                                                                 .withExpression(newUndefinedComparisonBuilder()
                                                                                         .withExpression(condition)
                                                                                         .build())
                                                                 .build())
                                               .withOperator(LogicalOperator.AND)
                                               .withRight(newNegationExpressionBuilder()
                                                                  .withExpression(EcoreUtil.copy(condition))
                                                                  .build())
                                               .build())
                        .build();
            default:
                throw new IllegalArgumentException("Unsupported type: " + booleanAggregatorType);
        }
    }

}
