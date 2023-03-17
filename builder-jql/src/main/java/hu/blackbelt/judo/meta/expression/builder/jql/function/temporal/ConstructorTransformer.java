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
import hu.blackbelt.judo.meta.expression.builder.jql.*;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.constant.util.builder.IntegerConstantBuilder;
import hu.blackbelt.judo.meta.expression.temporal.*;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionParameter;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;
import org.eclipse.emf.common.util.EList;
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
            throw new IllegalArgumentException("Function " + functionCall.getName() + " is not supported on " + argument.getClass().getSimpleName());
        }
        NE ne = jqlTransformers.getModelAdapter().get((TypeNameExpression) argument).get();
        if (jqlTransformers.getModelAdapter().isPrimitiveType(ne)) {
            P primitiveType = (P) ne;
            if (jqlTransformers.getModelAdapter().isDate(primitiveType)) {
                return getDateConstructionExpression(functionCall, context);
            } else if (jqlTransformers.getModelAdapter().isTime(primitiveType)) {
                return getTimeConstructionExpression(functionCall, context);
            } else if (jqlTransformers.getModelAdapter().isTimestamp(primitiveType)) {
                return getTimestampConstructionExpression(functionCall, context);
            } else {
                throw new IllegalArgumentException("Function is not valid for given type");
            }
        } else {
            throw new IllegalArgumentException("Type is not valid for environment variable");
        }
    }

    private DateConstructionExpression getDateConstructionExpression(JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        EList<FunctionParameter> parameters = functionCall.getParameters();

        JqlTransformerUtils.validateParameterCount(functionCall.getName(), parameters, 3);

        return newDateConstructionExpressionBuilder()
                .withYear((IntegerExpression) jqlTransformers.transform(parameters.get(0).getExpression(), context))
                .withMonth((IntegerExpression) jqlTransformers.transform(parameters.get(1).getExpression(), context))
                .withDay((IntegerExpression) jqlTransformers.transform(parameters.get(2).getExpression(), context))
                .build();
    }

    private TimeConstructionExpression getTimeConstructionExpression(JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        EList<FunctionParameter> parameters = functionCall.getParameters();

        int parameterCount = JqlTransformerUtils.validateParameterCount(functionCall.getName(), parameters, 2, 3, 4);

        final IntegerExpression second;
        if (parameterCount > 2) {
            second = (IntegerExpression) jqlTransformers.transform(parameters.get(2).getExpression(), context);
        } else {
            second = IntegerConstantBuilder.create().withValue(BigInteger.ZERO).build();
        }

        final IntegerExpression millisecond;
        if (parameterCount == 4) {
            millisecond = (IntegerExpression) jqlTransformers.transform(parameters.get(3).getExpression(), context);
        } else {
            millisecond = IntegerConstantBuilder.create().withValue(BigInteger.ZERO).build();
        }

        return newTimeConstructionExpressionBuilder()
                .withHour((IntegerExpression) jqlTransformers.transform(parameters.get(0).getExpression(), context))
                .withMinute((IntegerExpression) jqlTransformers.transform(parameters.get(1).getExpression(), context))
                .withSecond(second)
                .withMillisecond(millisecond)
                .build();
    }

    private TimestampConstructionExpression getTimestampConstructionExpression(JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        int parameterCount = JqlTransformerUtils.validateParameterCount(functionCall.getName(), functionCall.getParameters(), 1, 2, 5, 6, 7);

        if (parameterCount == 1 || parameterCount == 2) {
            return getTimestampConstructionWithDateAndTime(functionCall, context);
        } else {
            return getTimestampConstructionWithNumbers(functionCall, context);
        }
    }

    private TimestampConstructionExpression getTimestampConstructionWithDateAndTime(JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        // expressions must be transformed each time with a "new" instance, because transformation has composite relation to argument
        List<FunctionParameter> parameters = functionCall.getParameters();

        final IntegerExpression hour;
        final IntegerExpression minute;
        final IntegerExpression second;
        final IntegerExpression millisecond;
        if (parameters.size() == 2) {
            TimeExpression timeExpression = (TimeExpression) jqlTransformers.transform(parameters.get(1).getExpression(), context);
            hour = (IntegerExpression) new ExtractTransformer(null, ChronoUnit.HOURS).apply(timeExpression, null, context);
            minute = (IntegerExpression) new ExtractTransformer(null, ChronoUnit.MINUTES).apply(EcoreUtil.copy(timeExpression), null, context);
            second = (IntegerExpression) new ExtractTransformer(null, ChronoUnit.SECONDS).apply(EcoreUtil.copy(timeExpression), null, context);
            millisecond = (IntegerExpression) new ExtractTransformer(null, ChronoUnit.MILLIS).apply(EcoreUtil.copy(timeExpression), null, context);
        } else {
            hour = IntegerConstantBuilder.create().withValue(BigInteger.ZERO).build();
            minute = IntegerConstantBuilder.create().withValue(BigInteger.ZERO).build();
            second = IntegerConstantBuilder.create().withValue(BigInteger.ZERO).build();
            millisecond = IntegerConstantBuilder.create().withValue(BigInteger.ZERO).build();
        }

        final DateExpression dateExpression = (DateExpression) jqlTransformers.transform(parameters.get(0).getExpression(), context);
        return newTimestampConstructionExpressionBuilder()
                .withYear((IntegerExpression) new ExtractTransformer(null, ChronoUnit.YEARS).apply(dateExpression, null, context))
                .withMonth((IntegerExpression) new ExtractTransformer(null, ChronoUnit.MONTHS).apply(EcoreUtil.copy(dateExpression), null, context))
                .withDay((IntegerExpression) new ExtractTransformer(null, ChronoUnit.DAYS).apply(EcoreUtil.copy(dateExpression), null, context))
                .withHour(hour)
                .withMinute(minute)
                .withSecond(second)
                .withMillisecond(millisecond)
                .build();
    }

    private TimestampConstructionExpression getTimestampConstructionWithNumbers(JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        EList<FunctionParameter> parameters = functionCall.getParameters();

        final IntegerExpression second;
        if (parameters.size() > 5) {
            second = (IntegerExpression) jqlTransformers.transform(parameters.get(5).getExpression(), context);
        } else {
            second = IntegerConstantBuilder.create().withValue(BigInteger.ZERO).build();
        }

        final IntegerExpression millisecond;
        if (parameters.size() == 7) {
            millisecond = (IntegerExpression) jqlTransformers.transform(parameters.get(6).getExpression(), context);
        } else {
            millisecond = IntegerConstantBuilder.create().withValue(BigInteger.ZERO).build();
        }

        return newTimestampConstructionExpressionBuilder()
                .withYear((IntegerExpression) jqlTransformers.transform(parameters.get(0).getExpression(), context))
                .withMonth((IntegerExpression) jqlTransformers.transform(parameters.get(1).getExpression(), context))
                .withDay((IntegerExpression) jqlTransformers.transform(parameters.get(2).getExpression(), context))
                .withHour((IntegerExpression) jqlTransformers.transform(parameters.get(3).getExpression(), context))
                .withMinute((IntegerExpression) jqlTransformers.transform(parameters.get(4).getExpression(), context))
                .withSecond(second)
                .withMillisecond(millisecond)
                .build();
    }

}
