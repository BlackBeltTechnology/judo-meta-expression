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

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.IntegerExpression;
import hu.blackbelt.judo.meta.expression.TypeNameExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.*;

public class ConstructorTransformer<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> extends AbstractJqlFunctionTransformer<TypeNameExpression> {

    private JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers;

    public ConstructorTransformer(JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers) {
        super(jqlTransformers);
        this.jqlTransformers = jqlTransformers;
    }

    @Override
    public Expression apply(TypeNameExpression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        NE ne = jqlTransformers.getModelAdapter().get(argument).get();
        if (jqlTransformers.getModelAdapter().isPrimitiveType(ne)) {
            P primitiveType = (P) ne;
            if (jqlTransformers.getModelAdapter().isDate(primitiveType)) {
                if (functionCall.getParameters().size() != 3) {
                    throw new IllegalArgumentException("3 parameters expected");
                }
                return newDateConstructionExpressionBuilder()
                        .withYear((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context))
                        .withMonth((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(1).getExpression(), context))
                        .withDay((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(2).getExpression(), context))
                        .build();
            } else if (jqlTransformers.getModelAdapter().isTimestamp(primitiveType)) {
                if (functionCall.getParameters().size() != 6) {
                    throw new IllegalArgumentException("6 parameters expected");
                }
                return newTimestampConstructionExpressionBuilder()
                        .withYear((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context))
                        .withMonth((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(1).getExpression(), context))
                        .withDay((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(2).getExpression(), context))
                        .withHour((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(3).getExpression(), context))
                        .withMinute((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(4).getExpression(), context))
                        .withSecond((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(5).getExpression(), context))
                        .build();
            } else if (jqlTransformers.getModelAdapter().isTime(primitiveType)) {
                if (functionCall.getParameters().size() != 3) {
                    throw new IllegalArgumentException("3 parameters expected");
                }
                return newTimeConstructionExpressionBuilder()
                        .withHour((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context))
                        .withMinute((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(1).getExpression(), context))
                        .withSecond((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(2).getExpression(), context))
                        .build();
            } else {
                throw new IllegalArgumentException("Function is not valid for given type");
            }
        } else {
            throw new IllegalArgumentException("Type is not valid for environment variable");
        }
    }
}
