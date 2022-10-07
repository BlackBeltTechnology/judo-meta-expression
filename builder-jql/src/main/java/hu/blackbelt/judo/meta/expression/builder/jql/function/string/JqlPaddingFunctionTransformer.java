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
import hu.blackbelt.judo.meta.expression.constant.util.builder.StringConstantBuilder;
import hu.blackbelt.judo.meta.expression.string.PaddignType;
import hu.blackbelt.judo.meta.expression.string.util.builder.PaddingExpressionBuilder;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionParameter;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import java.util.List;

public class JqlPaddingFunctionTransformer extends AbstractJqlFunctionTransformer<Expression> {

    private final PaddignType paddignType;

    public JqlPaddingFunctionTransformer(ExpressionTransformer jqlTransformers, PaddignType paddignType) {
        super(jqlTransformers);
        this.paddignType = paddignType;
    }

    @Override
    public Expression apply(Expression expression, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        StringExpression stringExpression = JqlTransformerUtils.castExpression(StringExpression.class, () -> expression, "Padding is not supported on {1}");
        List<FunctionParameter> parameters = functionCall.getParameters();

        if (parameters.size() < 1 || parameters.size() > 2) {
            throw new IllegalArgumentException("Invalid number of arguments: Expected: 1 or 2, Got: " + parameters.size());
        }

        IntegerExpression length = JqlTransformerUtils.castExpression(IntegerExpression.class, () -> expressionTransformer.transform(parameters.get(0).getExpression(), context));
        StringExpression padding;
        if (parameters.size() == 1) {
            padding = StringConstantBuilder.create().withValue(" ").build();
        } else {
            padding = JqlTransformerUtils.castExpression(StringExpression.class, () -> expressionTransformer.transform(parameters.get(1).getExpression(), context));
        }

        return PaddingExpressionBuilder.create()
                                       .withLength(length)
                                       .withPadding(padding)
                                       .withPaddingType(paddignType)
                                       .withExpression(stringExpression)
                                       .build();

    }

}
