package hu.blackbelt.judo.meta.expression.builder.jql.expression;

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
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal;
import hu.blackbelt.judo.meta.expression.constant.util.builder.MeasuredDecimalBuilder;
import hu.blackbelt.judo.meta.jql.jqldsl.DecimalLiteral;
import hu.blackbelt.judo.meta.jql.jqldsl.IntegerLiteral;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.MeasuredLiteral;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newMeasuredDecimalBuilder;

public class JqlMeasuredLiteralTransformer<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> extends AbstractJqlExpressionTransformer<MeasuredLiteral, NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> {

    private static final String MEASURE_NAME_REGEX = "((.*)#)?(.*)";

    public JqlMeasuredLiteralTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    private String getUnitName(MeasureIdentifier measureIdentifier) {
        String unitName = measureIdentifier.unitName;
        if (unitName == null || unitName.isEmpty()) {
            throw new IllegalStateException("Unit name is empty");
        }
        return unitName;
    }

    private MeasureName getMeasureName(MeasureIdentifier measureIdentifier) {
        if (measureIdentifier.measureName == null) {
            return null;
        } else {
            MeasureName measureName = jqlTransformers.getMeasureName(measureIdentifier.measureName);
            if (measureName == null) {
                throw new IllegalArgumentException("Unknown measure: " + measureIdentifier.measureName);
            }
            return measureName;
        }
    }

    @Override
    protected Expression doTransform(MeasuredLiteral measuredLiteral, ExpressionBuildingVariableResolver context) {
        MeasureIdentifier measureIdentifier = new MeasureIdentifier(measuredLiteral.getMeasure());
        String unitName = getUnitName(measureIdentifier);
        MeasureName measureName = getMeasureName(measureIdentifier);
        JqlExpression jqlValue = measuredLiteral.getValue();
        MeasuredDecimal result;
        if (jqlValue instanceof IntegerLiteral) {
            IntegerLiteral integerLiteral = (IntegerLiteral) jqlValue;
            BigDecimal decimalValue = new BigDecimal(integerLiteral.getValue());
            MeasuredDecimalBuilder builder = newMeasuredDecimalBuilder().withUnitName(unitName).withValue(decimalValue);
            if (measureName != null) {
                builder.withMeasure(measureName);
            }
            result = builder.build();
        } else if (jqlValue instanceof DecimalLiteral) {
            BigDecimal decimalValue = ((DecimalLiteral) jqlValue).getValue();
            MeasuredDecimalBuilder builder = newMeasuredDecimalBuilder().withUnitName(unitName).withValue(decimalValue);
            if (measureName != null) {
                builder.withMeasure(measureName);
            }
            result = builder.build();
        } else {
            throw new IllegalStateException("Invalid type of JQL measured literal: " + measuredLiteral);
        }
        U unit = getModelAdapter().getUnit(result).orElseThrow(() -> new IllegalArgumentException("No unit found in model: " + unitName));
        M measure = getModelAdapter().getMeasure(result).orElseThrow(() -> new IllegalArgumentException("No measure found in model: " + unitName));
        String resolvedUnitName =  getModelAdapter().getUnitName(unit);
        MeasureName resolvedMeasureName = getModelAdapter().buildMeasureName(measure).orElseThrow(() -> new IllegalArgumentException("Measure name not found: " + unitName));
        MeasureName measureNameInModel = jqlTransformers.getExpressionBuilder().getMeasureName(resolvedMeasureName.getNamespace(), resolvedMeasureName.getName());
        result.setUnitName(resolvedUnitName);
        result.setMeasure(measureNameInModel);
        ModelAdapter.UnitFraction unitRates = getModelAdapter().getUnitRates(unit);
        result.setRateDividend(unitRates.getDividend());
        result.setRateDivisor(unitRates.getDivisor());
        return result;
    }

    private static class MeasureIdentifier {
        private final String unitName;
        private final String measureName;

        private MeasureIdentifier(String jqlMeasureString) {
            Matcher matcher = Pattern.compile(MEASURE_NAME_REGEX).matcher(jqlMeasureString);
            if (matcher.matches()) {
                unitName = matcher.group(3);
                measureName = matcher.group(2);
            } else {
                throw new IllegalArgumentException("Cannot be parsed as measure name: " + jqlMeasureString);
            }

        }
    }

}
