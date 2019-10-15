package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.constant.util.builder.MeasuredDecimalBuilder;
import hu.blackbelt.judo.meta.expression.constant.util.builder.MeasuredIntegerBuilder;
import hu.blackbelt.judo.meta.expression.util.builder.MeasureNameBuilder;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.DecimalLiteral;
import hu.blackbelt.judo.meta.jql.jqldsl.IntegerLiteral;
import hu.blackbelt.judo.meta.jql.jqldsl.MeasuredLiteral;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newMeasuredDecimalBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newMeasuredIntegerBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newMeasureNameBuilder;

public class JqlMeasuredLiteralTransformer implements JqlExpressionTransformerFunction {

    private JqlTransformers transformers;

    public JqlMeasuredLiteralTransformer(JqlTransformers transformers) {
        this.transformers = transformers;
    }

    private String getUnitName(MeasuredLiteral measuredLiteral) {
        String unitName = measuredLiteral.getMeasure();
        if (unitName == null || unitName.isEmpty()) {
            throw new IllegalStateException("Unit name is empty: " + measuredLiteral);
        }
        return unitName;
    }

    private MeasureName getMeasureName(MeasuredLiteral measuredLiteral) {
        MeasureName measureName;
        if (measuredLiteral.getType() != null) {
            List<String> qMeasureNameParts = Arrays.asList(measuredLiteral.getType().split("::"));
            String unitName = qMeasureNameParts.get(qMeasureNameParts.size() - 1);
            MeasureNameBuilder measureNameBuilder = newMeasureNameBuilder().withName(unitName);
            if (qMeasureNameParts.size() > 1) {
                String namespace = String.join("::", qMeasureNameParts.subList(0, qMeasureNameParts.size() - 1));
                measureNameBuilder = measureNameBuilder.withNamespace(namespace);
            }
            measureName = measureNameBuilder.build();
        } else {
            measureName = null;
        }
        return measureName;
    }

    @Override
    public Expression apply(hu.blackbelt.judo.meta.jql.jqldsl.Expression expression, List<ObjectVariable> variables) {
        MeasuredLiteral measuredLiteral = (MeasuredLiteral) expression;
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
