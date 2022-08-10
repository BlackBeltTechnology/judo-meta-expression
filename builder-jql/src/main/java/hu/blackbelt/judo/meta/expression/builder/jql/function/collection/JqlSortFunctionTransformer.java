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

import hu.blackbelt.judo.meta.expression.CollectionExpression;
import hu.blackbelt.judo.meta.expression.DataExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.collection.OrderByItem;
import hu.blackbelt.judo.meta.expression.collection.util.builder.SortExpressionBuilder;
import hu.blackbelt.judo.meta.expression.constant.BooleanConstant;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import java.util.ArrayList;
import java.util.List;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newOrderByItemBuilder;
import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newSortExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newBooleanConstantBuilder;

public class JqlSortFunctionTransformer extends AbstractJqlFunctionTransformer<CollectionExpression> {

    public JqlSortFunctionTransformer(ExpressionTransformer jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(CollectionExpression collection, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        List<OrderByItem> orderByItems = new ArrayList<>();
        functionCall.getParameters().forEach((param) -> {
                    DataExpression orderByItemExpression = (DataExpression) expressionTransformer.transform(param.getExpression(), context);
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

    public static boolean isDescending(String parameterExtension) {
        if (parameterExtension == null || parameterExtension.equalsIgnoreCase("asc")) {
            return false;
        } else if (parameterExtension.equalsIgnoreCase("desc")) {
            return true;
        } else {
            throw new IllegalArgumentException(("Invalid sorting direction " + parameterExtension));
        }
    }
}
