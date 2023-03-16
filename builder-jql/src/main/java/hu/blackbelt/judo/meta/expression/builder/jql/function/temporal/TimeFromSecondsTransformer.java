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
import hu.blackbelt.judo.meta.expression.builder.jql.*;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.temporal.util.builder.TimeFromSecondsExpressionBuilder;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

public class TimeFromSecondsTransformer<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> extends AbstractJqlFunctionTransformer<Expression> {

    private final JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers;

    public TimeFromSecondsTransformer(JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers) {
        super(jqlTransformers);
        this.jqlTransformers = jqlTransformers;
    }

    @Override
    public Expression apply(Expression expression, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        TypeNameExpression typeNameExpression = JqlTransformerUtils.castExpression(TypeNameExpression.class, () -> expression, functionCall.getName() + " is not supported on {1}");

        NE ne = jqlTransformers.getModelAdapter().get(typeNameExpression).orElseThrow();
        if (!(jqlTransformers.getModelAdapter().isPrimitiveType(ne) && jqlTransformers.getModelAdapter().isTime((P) ne))) {
            throw new IllegalArgumentException(functionCall.getName() + " is not supported on " + jqlTransformers.getModelAdapter().getName(ne).orElse("<unknown>"));
        }

        JqlTransformerUtils.validateParameterCount(functionCall.getName(), functionCall.getParameters(), 1);
        IntegerExpression seconds = JqlTransformerUtils.castExpression(IntegerExpression.class, () -> expressionTransformer.transform(functionCall.getParameters().get(0).getExpression(), context));
        return TimeFromSecondsExpressionBuilder.create().withSeconds(seconds).build();
    }

}
