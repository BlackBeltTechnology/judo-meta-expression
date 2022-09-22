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
import hu.blackbelt.judo.meta.expression.constant.util.builder.IntegerConstantBuilder;
import hu.blackbelt.judo.meta.expression.temporal.TimestampConstructionExpression;
import hu.blackbelt.judo.meta.expression.temporal.util.builder.TimestampConstructionExpressionBuilder;
import hu.blackbelt.judo.meta.jql.jqldsl.*;

import java.math.BigInteger;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
                if (functionCall.getParameters().size() == 1 || functionCall.getParameters().size() == 2) {
                    return getTimestampConstructionWithDateAndTime(functionCall, context);
                } else if (functionCall.getParameters().size() == 6) {
                    return newTimestampConstructionExpressionBuilder()
                            .withYear((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context))
                            .withMonth((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(1).getExpression(), context))
                            .withDay((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(2).getExpression(), context))
                            .withHour((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(3).getExpression(), context))
                            .withMinute((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(4).getExpression(), context))
                            .withSecond((IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(5).getExpression(), context))
                            .build();
                } else {
                    throw new IllegalArgumentException("Invalid number of arguments. Expected: 1, 2 or 6. Got: " + functionCall.getParameters().size());
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
        final DateExpression dateExpressionForYear;
        final DateExpression dateExpressionForMonth;
        final DateExpression dateExpressionForDays;
        final TimeExpression timeExpressionForHour;
        final TimeExpression timeExpressionForMinute;
        final TimeExpression timeExpressionForSecond;
        List<FunctionParameter> parameters = functionCall.getParameters();
        int parameterNumber = parameters.size();
        if (parameterNumber == 1) {
            JqlExpression dateParameter = parameters.get(0).getExpression();
            dateExpressionForYear = (DateExpression) jqlTransformers.transform(dateParameter, context);
            dateExpressionForMonth = (DateExpression) jqlTransformers.transform(dateParameter, context);
            dateExpressionForDays = (DateExpression) jqlTransformers.transform(dateParameter, context);
            timeExpressionForHour = null;
            timeExpressionForMinute = null;
            timeExpressionForSecond = null;
        } else if (parameterNumber == 2) {
            JqlExpression dateParameter = parameters.get(0).getExpression();
            dateExpressionForYear = (DateExpression) jqlTransformers.transform(dateParameter, context);
            dateExpressionForMonth = (DateExpression) jqlTransformers.transform(dateParameter, context);
            dateExpressionForDays = (DateExpression) jqlTransformers.transform(dateParameter, context);
            JqlExpression timeParameter = parameters.get(1).getExpression();
            timeExpressionForHour = (TimeExpression) jqlTransformers.transform(timeParameter, context);
            timeExpressionForMinute = (TimeExpression) jqlTransformers.transform(timeParameter, context);
            timeExpressionForSecond = (TimeExpression) jqlTransformers.transform(timeParameter, context);
        } else {
            throw new IllegalArgumentException("Invalid number of timestamp construction arguments: " + parameterNumber);
        }

        TimestampConstructionExpressionBuilder timestampConstructionExpression = newTimestampConstructionExpressionBuilder()
                .withYear((IntegerExpression) new ExtractTransformer(null, ChronoUnit.YEARS).apply(dateExpressionForYear, null, context))
                .withMonth((IntegerExpression) new ExtractTransformer(null, ChronoUnit.MONTHS).apply(dateExpressionForMonth, null, context))
                .withDay((IntegerExpression) new ExtractTransformer(null, ChronoUnit.DAYS).apply(dateExpressionForDays, null, context));
        if (timeExpressionForHour != null) {
            timestampConstructionExpression
                    .withHour((IntegerExpression) new ExtractTransformer(null, ChronoUnit.HOURS).apply(timeExpressionForHour, null, context))
                    .withMinute((IntegerExpression) new ExtractTransformer(null, ChronoUnit.MINUTES).apply(timeExpressionForMinute, null, context))
                    .withSecond((IntegerExpression) new ExtractTransformer(null, ChronoUnit.SECONDS).apply(timeExpressionForSecond, null, context));
        } else {
            timestampConstructionExpression
                    .withHour(IntegerConstantBuilder.create().withValue(BigInteger.valueOf(0)).build())
                    .withMinute(IntegerConstantBuilder.create().withValue(BigInteger.valueOf(0)).build())
                    .withSecond(IntegerConstantBuilder.create().withValue(BigInteger.valueOf(0)).build());
        }
        return timestampConstructionExpression.build();
    }

}
