package hu.blackbelt.judo.meta.expression.builder.jql.function.string;

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
import hu.blackbelt.judo.meta.expression.constant.BooleanConstant;
import hu.blackbelt.judo.meta.expression.constant.util.builder.BooleanConstantBuilder;
import hu.blackbelt.judo.meta.expression.logical.NegationExpression;
import hu.blackbelt.judo.meta.expression.logical.util.builder.LikeBuilder;
import hu.blackbelt.judo.meta.expression.logical.util.builder.NegationExpressionBuilder;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionParameter;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import java.util.List;

public class JqlLikeFunctionTransformer extends AbstractJqlFunctionTransformer<Expression> {

    private final boolean explicitCaseInsensitive;

    public JqlLikeFunctionTransformer(ExpressionTransformer jqlTransformers, boolean explicitCaseInsensitive) {
        super(jqlTransformers);
        this.explicitCaseInsensitive = explicitCaseInsensitive;
    }

    @Override
    public Expression apply(Expression expression, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        StringExpression string = JqlTransformerUtils.castExpression(StringExpression.class, () -> expression, functionCall.getName() + " is not supported on {1}");

        List<FunctionParameter> parameters = functionCall.getParameters();
        JqlTransformerUtils.validateParameterCount(parameters, 1, 2);

        StringExpression pattern = JqlTransformerUtils.castExpression(StringExpression.class, () -> expressionTransformer.transform(parameters.get(0).getExpression(), context));

        if (parameters.size() == 1) {
            BooleanConstant caseInsensitive = BooleanConstantBuilder.create().withValue(explicitCaseInsensitive).build();

            return LikeBuilder.create()
                              .withExpression(string)
                              .withPattern(pattern)
                              .withCaseInsensitive(caseInsensitive)
                              .build();
        } else {
            LogicalExpression exact = JqlTransformerUtils.castExpression(LogicalExpression.class, () -> expressionTransformer.transform(parameters.get(1).getExpression(), context));
            NegationExpression caseInsensitive = NegationExpressionBuilder.create().withExpression(exact).build();

            return LikeBuilder.create()
                              .withExpression(string)
                              .withPattern(pattern)
                              .withCaseInsensitive(caseInsensitive)
                              .build();
        }
    }

}
