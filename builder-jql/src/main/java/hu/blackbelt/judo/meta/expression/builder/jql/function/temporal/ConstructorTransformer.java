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
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders;
import hu.blackbelt.judo.meta.expression.constant.util.builder.IntegerConstantBuilder;
import hu.blackbelt.judo.meta.expression.temporal.TimestampConstructionExpression;
import hu.blackbelt.judo.meta.expression.temporal.util.builder.TimestampConstructionExpressionBuilder;
import hu.blackbelt.judo.meta.jql.jqldsl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.math.BigInteger;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.*;

public class ConstructorTransformer<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> extends AbstractJqlFunctionTransformer<Expression> {

    private JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers;

    public ConstructorTransformer(JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers) {
        super(jqlTransformers);
        this.jqlTransformers = jqlTransformers;
    }

    @Override
    public Expression apply(Expression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        if (!(argument instanceof TypeNameExpression)) {
            throw new IllegalArgumentException(String.format("Function %s not supported on %s", functionCall.getName(), argument.getClass().getSimpleName()));
        }
        NE ne = jqlTransformers.getModelAdapter().get((TypeNameExpression) argument).get();
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
                if (functionCall.getParameters().size() == 1 || functionCall.getParameters().size() == 2) {
                    return getTimestampConstructionWithDateAndTime(functionCall, context);
                } else if (functionCall.getParameters().size() == 6 || functionCall.getParameters().size() == 5) {
                    final IntegerExpression second;
                    if (functionCall.getParameters().size() == 6) {
                        second = (IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(5).getExpression(), context);
                    } else {
                        second = ConstantBuilders.newIntegerConstantBuilder().withValue(BigInteger.ZERO).build();
                    }
                    return newTimestampConstructionExpressionBuilder()
                            .withYear((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context))
                            .withMonth((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(1).getExpression(), context))
                            .withDay((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(2).getExpression(), context))
                            .withHour((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(3).getExpression(), context))
                            .withMinute((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(4).getExpression(), context))
                            .withSecond(second)
                            .build();
                } else {
                    throw new IllegalArgumentException("Invalid number of arguments. Expected: 1, 2, 5 or 6. Got: " + functionCall.getParameters().size());
                }
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

    private TimestampConstructionExpression getTimestampConstructionWithDateAndTime(JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        // expressions must be transformed each time with a "new" instance, because transformation has composite relation to argument
        List<FunctionParameter> parameters = functionCall.getParameters();
        final DateExpression dateExpression = (DateExpression) jqlTransformers.transform(parameters.get(0).getExpression(), context);
        final TimeExpression timeExpression;
        if (parameters.size() == 1) {
            timeExpression = null;
        } else if (parameters.size() == 2) {
            timeExpression = (TimeExpression) jqlTransformers.transform(parameters.get(1).getExpression(), context);
        } else {
            throw new IllegalArgumentException("Invalid number of timestamp construction arguments: " + parameters.size());
        }

        TimestampConstructionExpressionBuilder timestampConstructionExpression = newTimestampConstructionExpressionBuilder()
                .withYear((IntegerExpression) new ExtractTransformer(null, ChronoUnit.YEARS).apply(dateExpression, null, context))
                .withMonth((IntegerExpression) new ExtractTransformer(null, ChronoUnit.MONTHS).apply(EcoreUtil.copy(dateExpression), null, context))
                .withDay((IntegerExpression) new ExtractTransformer(null, ChronoUnit.DAYS).apply(EcoreUtil.copy(dateExpression), null, context));
        if (timeExpression != null) {
            timestampConstructionExpression
                    .withHour((IntegerExpression) new ExtractTransformer(null, ChronoUnit.HOURS).apply(timeExpression, null, context))
                    .withMinute((IntegerExpression) new ExtractTransformer(null, ChronoUnit.MINUTES).apply(EcoreUtil.copy(timeExpression), null, context))
                    .withSecond((IntegerExpression) new ExtractTransformer(null, ChronoUnit.SECONDS).apply(EcoreUtil.copy(timeExpression), null, context));
        } else {
            timestampConstructionExpression
                    .withHour(IntegerConstantBuilder.create().withValue(BigInteger.valueOf(0)).build())
                    .withMinute(IntegerConstantBuilder.create().withValue(BigInteger.valueOf(0)).build())
                    .withSecond(IntegerConstantBuilder.create().withValue(BigInteger.valueOf(0)).build());
        }
        return timestampConstructionExpression.build();
    }

}
