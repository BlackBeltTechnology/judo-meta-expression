package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.constant.util.builder.MeasuredDecimalBuilder;
import hu.blackbelt.judo.meta.expression.constant.util.builder.MeasuredIntegerBuilder;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.DecimalLiteral;
import hu.blackbelt.judo.meta.jql.jqldsl.IntegerLiteral;
import hu.blackbelt.judo.meta.jql.jqldsl.MeasuredLiteral;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newMeasuredDecimalBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newMeasuredIntegerBuilder;

public class JqlMeasuredLiteralTransformer<NE, P, PTE, E, C extends NE, RTE, M, U> extends AbstractJqlExpressionTransformer<MeasuredLiteral, NE, P, PTE, E, C, RTE, M, U> {

    public JqlMeasuredLiteralTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    private String getUnitName(MeasuredLiteral measuredLiteral) {
        String unitName = measuredLiteral.getMeasure();
        if (unitName == null || unitName.isEmpty()) {
            throw new IllegalStateException("Unit name is empty: " + measuredLiteral);
        }
        return unitName;
    }

    private MeasureName getMeasureName(MeasuredLiteral measuredLiteral) {
        return measuredLiteral.getType() != null ? jqlTransformers.getMeasureNames().get(measuredLiteral.getType()) : null;
    }

    @Override
    protected Expression doTransform(MeasuredLiteral measuredLiteral, List<ObjectVariable> variables) {
        String unitName = getUnitName(measuredLiteral);
        MeasureName measureName = getMeasureName(measuredLiteral);
        hu.blackbelt.judo.meta.jql.jqldsl.Expression jqlValue = measuredLiteral.getValue();
        Expression result;
        if (jqlValue instanceof IntegerLiteral) {
            IntegerLiteral integerLiteral = (IntegerLiteral) jqlValue;
            BigInteger integerValue = integerLiteral.getValue();
            MeasuredIntegerBuilder builder = newMeasuredIntegerBuilder().withUnitName(unitName).withValue(integerValue);
            if (measureName != null) {
                builder = builder.withMeasure(measureName);
            }
            result = builder.build();
        } else if (jqlValue instanceof DecimalLiteral) {
            BigDecimal decimalValue = ((DecimalLiteral)jqlValue).getValue();
            MeasuredDecimalBuilder builder = newMeasuredDecimalBuilder().withUnitName(unitName).withValue(decimalValue);
            if (measureName != null) {
                builder = builder.withMeasure(measureName);
            }
            result = builder.build();
        } else {
            throw new IllegalStateException("Invalid type of JQL measured literal: " + measuredLiteral);
        }
        return result;
    }

}
