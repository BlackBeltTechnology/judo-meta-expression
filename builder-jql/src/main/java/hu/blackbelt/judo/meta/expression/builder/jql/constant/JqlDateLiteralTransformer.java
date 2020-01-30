package hu.blackbelt.judo.meta.expression.builder.jql.constant;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlExpressionTransformerFunction;
import hu.blackbelt.judo.meta.jql.jqldsl.DateLiteral;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newDateConstantBuilder;

public class JqlDateLiteralTransformer implements JqlExpressionTransformerFunction {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Expression apply(JqlExpression jqlExpression, ExpressionBuildingVariableResolver context) {
        DateLiteral dateLiteral = (DateLiteral) jqlExpression;
        try {
            Date dateValue = DATE_FORMAT.parse(dateLiteral.getValue());
            return newDateConstantBuilder().withValue(dateValue).build();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Date should be in yyyy-MM-dd format, got: " + dateLiteral.getValue());
        }
    }

}
