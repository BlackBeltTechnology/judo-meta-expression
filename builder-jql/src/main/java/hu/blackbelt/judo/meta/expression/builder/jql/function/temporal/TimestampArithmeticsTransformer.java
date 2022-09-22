package hu.blackbelt.judo.meta.expression.builder.jql.function.temporal;

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
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.temporal.TimestampPart;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionParameter;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import java.util.List;

import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampArithmeticExpressionBuilder;

public class TimestampArithmeticsTransformer extends AbstractJqlFunctionTransformer<TimestampExpression> {

    private final ExpressionTransformer jqlTransformers;
    private final TimestampPart timestampPart;

    public TimestampArithmeticsTransformer(ExpressionTransformer jqlTransformers, TimestampPart timestampPart) {
        super(jqlTransformers);
        this.jqlTransformers = jqlTransformers;
        this.timestampPart = timestampPart;
    }

    @Override
    public Expression apply(TimestampExpression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        List<FunctionParameter> parameters = functionCall.getParameters();
        if (parameters.size() != 1) {
            throw new IllegalArgumentException("Unexpected number of arguments: Expected: 1. Got: " + parameters.size());
        }
        Expression parameterExpression = jqlTransformers.transform(parameters.get(0).getExpression(), context);
        if (!(parameterExpression instanceof IntegerExpression)) {
            throw new IllegalArgumentException("Unsupported parameter type: " + parameterExpression.getClass().getSimpleName());
        }

        return newTimestampArithmeticExpressionBuilder()
                .withTimestamp(argument)
                .withAmount((IntegerExpression) parameterExpression)
                .withTimestampPart(timestampPart)
                .build();
    }

}
