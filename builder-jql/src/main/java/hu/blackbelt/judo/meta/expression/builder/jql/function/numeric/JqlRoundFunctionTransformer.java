package hu.blackbelt.judo.meta.expression.builder.jql.function.numeric;

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
import hu.blackbelt.judo.meta.expression.numeric.util.builder.DecimalRoundExpressionBuilder;
import hu.blackbelt.judo.meta.expression.numeric.util.builder.IntegerRoundExpressionBuilder;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

public class JqlRoundFunctionTransformer extends AbstractJqlFunctionTransformer<Expression> {

    public JqlRoundFunctionTransformer(ExpressionTransformer jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(Expression expression, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        NumericExpression numericExpression = JqlTransformerUtils.castExpression(NumericExpression.class, () -> expression, functionCall.getName() + " no supported on {1}");
        JqlTransformerUtils.validateParameterCount(functionCall.getName(), functionCall.getParameters(), 0, 1);
        if (functionCall.getParameters().size() == 1) {
            IntegerExpression scale = JqlTransformerUtils.castExpression(IntegerExpression.class, () -> expressionTransformer.transform(functionCall.getParameters().get(0).getExpression(), context));
            return DecimalRoundExpressionBuilder.create().withExpression(numericExpression).withScale(scale).build();
        } else {
            return IntegerRoundExpressionBuilder.create().withExpression(numericExpression).build();
        }
    }

}
