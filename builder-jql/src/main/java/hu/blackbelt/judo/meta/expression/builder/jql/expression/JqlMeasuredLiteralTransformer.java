package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.constant.util.builder.MeasuredDecimalBuilder;
import hu.blackbelt.judo.meta.expression.constant.util.builder.MeasuredIntegerBuilder;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.DecimalLiteral;
import hu.blackbelt.judo.meta.jql.jqldsl.IntegerLiteral;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.MeasuredLiteral;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newMeasuredDecimalBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newMeasuredIntegerBuilder;

public class JqlMeasuredLiteralTransformer<NE, P, PTE, E extends NE, C extends NE, RTE, M, U> extends AbstractJqlExpressionTransformer<MeasuredLiteral, NE, P, PTE, E, C, RTE, M, U> {

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
    protected Expression doTransform(MeasuredLiteral measuredLiteral, List<ObjectVariable> variables) {
        MeasureIdentifier measureIdentifier = new MeasureIdentifier(measuredLiteral.getMeasure());
        String unitName = getUnitName(measureIdentifier);
        MeasureName measureName = getMeasureName(measureIdentifier);
        JqlExpression jqlValue = measuredLiteral.getValue();
        Expression result;
        if (jqlValue instanceof IntegerLiteral) {
            IntegerLiteral integerLiteral = (IntegerLiteral) jqlValue;
            BigInteger integerValue = integerLiteral.getValue();
            MeasuredIntegerBuilder builder = newMeasuredIntegerBuilder().withUnitName(unitName).withValue(integerValue);
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