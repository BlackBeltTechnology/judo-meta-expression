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
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionMeasureProvider;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.temporal.DateDifferenceExpression;
import hu.blackbelt.judo.meta.expression.temporal.TimestampDifferenceExpression;
import hu.blackbelt.judo.meta.expression.temporal.TimeDifferenceExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.*;

import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newDateDifferenceExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampDifferenceExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimeDifferenceExpressionBuilder;

public class JqlDifferenceFunctionTransformer extends AbstractJqlFunctionTransformer<DataExpression> {

    private JqlTransformers jqlTransformers;
    private ExpressionMeasureProvider measureProvider;

    public JqlDifferenceFunctionTransformer(JqlTransformers expressionTransformer, ExpressionMeasureProvider measureProvider) {
        super(expressionTransformer);
        jqlTransformers = expressionTransformer;
        this.measureProvider = measureProvider;
    }

    @Override
    public Expression apply(DataExpression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        MeasureName measureName = findMeasure(functionCall);
        Object measure = jqlTransformers.getModelAdapter().get(measureName).get();
        Object unit = jqlTransformers.getModelAdapter().getUnits(measure).get(0);
        if (argument instanceof DateExpression) {
            DateExpression startDate = (DateExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context);
            DateExpression endDate = (DateExpression) argument;
            DateDifferenceExpression differenceExpression = newDateDifferenceExpressionBuilder()
                    .withStartDate(startDate)
                    .withEndDate(endDate)
                    .withMeasure(measureName)
                    .build();
            ModelAdapter.UnitFraction durationRatio = jqlTransformers.getModelAdapter().getBaseDurationRatio(unit, ModelAdapter.DurationType.DAY);
            differenceExpression.setDayDividend(durationRatio.getDividend());
            differenceExpression.setDayDivisor(durationRatio.getDivisor());
            return differenceExpression;
        } else if (argument instanceof TimestampExpression) {
            TimestampExpression startTimestamp = (TimestampExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context);
            TimestampExpression endTimestamp = (TimestampExpression) argument;
            TimestampDifferenceExpression differenceExpression = newTimestampDifferenceExpressionBuilder()
                    .withStartTimestamp(startTimestamp)
                    .withEndTimestamp(endTimestamp)
                    .withMeasure(measureName)
                    .build();
            ModelAdapter.UnitFraction durationRatio = jqlTransformers.getModelAdapter().getBaseDurationRatio(unit, ModelAdapter.DurationType.SECOND);
            differenceExpression.setSecondDividend(durationRatio.getDividend());
            differenceExpression.setSecondDivisor(durationRatio.getDivisor());
            return differenceExpression;
        } else if (argument instanceof TimeExpression) {
            TimeExpression startTime = (TimeExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context);
            TimeExpression endTime = (TimeExpression) argument;
            TimeDifferenceExpression differenceExpression = newTimeDifferenceExpressionBuilder()
                    .withStartTime(startTime)
                    .withEndTime(endTime)
                    .withMeasure(measureName)
                    .build();
            ModelAdapter.UnitFraction durationRatio = jqlTransformers.getModelAdapter().getBaseDurationRatio(unit, ModelAdapter.DurationType.SECOND);
            differenceExpression.setSecondDividend(durationRatio.getDividend());
            differenceExpression.setSecondDivisor(durationRatio.getDivisor());
            return differenceExpression;
        } else {
            throw new IllegalArgumentException(("Unsupported argument type: " + argument));
        }
    }

    private MeasureName findMeasure(JqlFunction functionCall) {
        if (functionCall.getParameters().size() > 1) {
            FunctionParameter measureNameParam = functionCall.getParameters().get(1);
            QualifiedName measureQName = ((NavigationExpression) measureNameParam.getExpression()).getQName();
            MeasureName measureName = measureProvider.getDurationMeasureName(measureQName);
            if (measureName == null) {
                throw new IllegalArgumentException("Measure is not a duration");
            }
            return measureName;
        } else {
            Optional<MeasureName> measureName = measureProvider.getDefaultDurationMeasure();
            return measureName.orElseThrow(() -> new IllegalArgumentException("No single duration measure in the model"));
        }
    }
}
