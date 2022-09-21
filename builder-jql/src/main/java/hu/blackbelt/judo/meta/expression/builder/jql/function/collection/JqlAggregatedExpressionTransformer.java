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
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.JqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.operator.*;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalAggregatedExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerAggregatedExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringAggregatedExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.*;

public class JqlAggregatedExpressionTransformer extends AbstractJqlFunctionTransformer<CollectionExpression> {

    private final IntegerAggregator integerAggregator;
    private final DecimalAggregator decimalAggregator;
    private final StringAggregator stringAggregator;
    private final DateAggregator dateAggregator;
    private final TimestampAggregator timestampAggregator;
    private final TimeAggregator timeAggregator;

    private JqlAggregatedExpressionTransformer(ExpressionTransformer jqlTransformers, IntegerAggregator integerAggregator,
                                               DecimalAggregator decimalAggregator, StringAggregator stringAggregator,
                                               DateAggregator dateAggregator, TimestampAggregator timestampAggregator,
                                               TimeAggregator timeAggregator) {
        super(jqlTransformers);
        this.integerAggregator = integerAggregator;
        this.decimalAggregator = decimalAggregator;
        this.stringAggregator = stringAggregator;
        this.dateAggregator = dateAggregator;
        this.timestampAggregator = timestampAggregator;
        this.timeAggregator = timeAggregator;
    }

    public static JqlAggregatedExpressionTransformer createMinInstance(ExpressionTransformer jqlTransformers) {
        return new JqlAggregatedExpressionTransformer(jqlTransformers, IntegerAggregator.MIN, DecimalAggregator.MIN, StringAggregator.MIN, DateAggregator.MIN, TimestampAggregator.MIN, TimeAggregator.MIN);
    }

    public static JqlAggregatedExpressionTransformer createMaxInstance(ExpressionTransformer jqlTransformers) {
        return new JqlAggregatedExpressionTransformer(jqlTransformers, IntegerAggregator.MAX, DecimalAggregator.MAX, StringAggregator.MAX, DateAggregator.MAX, TimestampAggregator.MAX, TimeAggregator.MAX);
    }

    public static JqlAggregatedExpressionTransformer createSumInstance(ExpressionTransformer jqlTransformers) {
        return new JqlAggregatedExpressionTransformer(jqlTransformers, IntegerAggregator.SUM, DecimalAggregator.SUM, null, null, null, null);
    }

    public static JqlAggregatedExpressionTransformer createAvgInstance(ExpressionTransformer jqlTransformers) {
        return new JqlAggregatedExpressionTransformer(jqlTransformers, null, DecimalAggregator.AVG, null, DateAggregator.AVG, TimestampAggregator.AVG, TimeAggregator.AVG);
    }

    @Override
    public Expression apply(CollectionExpression collection, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        Expression parameter = expressionTransformer.transform(functionCall.getParameters().get(0).getExpression(), context);
        return createAggregatedExpression(collection, parameter);
    }

    public AggregatedExpression createAggregatedExpression(CollectionExpression collection, Expression parameter) {
        if (parameter instanceof IntegerExpression && integerAggregator != null) {
            return newIntegerAggregatedExpressionBuilder().withCollectionExpression(collection).withExpression((IntegerExpression) parameter).withOperator(integerAggregator).build();
        } else if ((parameter instanceof DecimalExpression || parameter instanceof IntegerExpression) && decimalAggregator != null) {
            return newDecimalAggregatedExpressionBuilder().withCollectionExpression(collection).withExpression((NumericExpression) parameter).withOperator(decimalAggregator).build();
        } else if (parameter instanceof StringExpression && stringAggregator != null) {
            return newStringAggregatedExpressionBuilder().withCollectionExpression(collection).withExpression((StringExpression) parameter).withOperator(stringAggregator).build();
        } else if (parameter instanceof DateExpression && dateAggregator != null) {
            return newDateAggregatedExpressionBuilder().withCollectionExpression(collection).withExpression((DateExpression) parameter).withOperator(dateAggregator).build();
        } else if (parameter instanceof TimestampExpression && timestampAggregator != null) {
            return newTimestampAggregatedExpressionBuilder().withCollectionExpression(collection).withExpression((TimestampExpression) parameter).withOperator(timestampAggregator).build();
        } else if (parameter instanceof TimeExpression && timeAggregator != null) {
            return newTimeAggregatedExpressionBuilder().withCollectionExpression(collection).withExpression((TimeExpression) parameter).withOperator(timeAggregator).build();
        } else {
            throw new IllegalArgumentException("Invalid expression for aggregation: " + parameter);
        }
    }
}
