package hu.blackbelt.judo.meta.expression.builder.jql.function.temporal;

/*-
 * #%L
 * JUDO :: Expression :: Model :: JQL builder
 * %%
 * Copyright (C) 2018 - 2023 BlackBelt Technology
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
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionParameter;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import java.util.List;

import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampFromMillisecondsExpressionBuilder;

public class TimestampFromMillisecondsTransformer extends AbstractJqlFunctionTransformer<Expression> {

    public TimestampFromMillisecondsTransformer(ExpressionTransformer expressionTransformer) {
        super(expressionTransformer);
    }

    @Override
    public Expression apply(Expression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        if (argument instanceof TypeNameExpression){
            List<FunctionParameter> parameters = functionCall.getParameters();
            if (parameters.size() == 1) {
                Expression transformResult = expressionTransformer.transform(parameters.get(0).getExpression(), context);
                if (transformResult instanceof IntegerExpression) {
                    return newTimestampFromMillisecondsExpressionBuilder()
                            .withMilliseconds((IntegerExpression) transformResult)
                            .build();
                } else {
                    throw new IllegalArgumentException(String.format("Invalid parameter type: Expected %s. Got: %s",
                                                                     IntegerExpression.class.getSimpleName(),
                                                                     transformResult.getClass().getSimpleName()));
                }
            } else {
                throw new IllegalArgumentException("Invalid number of arguments. Expected: 1. Got: " + functionCall.getParameters().size());
            }
        } else {
            throw new IllegalArgumentException(String.format("Function %s not supported on %s", functionCall.getName(), argument.getClass().getSimpleName()));
        }
    }

}
