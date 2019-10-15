package hu.blackbelt.judo.meta.expression.builder.jql.constant;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlExpressionTransformerFunction;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.DateLiteral;
import hu.blackbelt.judo.meta.jql.jqldsl.TimeStampLiteral;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newDateConstantBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newTimestampConstantBuilder;

public class JqlTimestampLiteralTransformer implements JqlExpressionTransformerFunction {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Expression apply(hu.blackbelt.judo.meta.jql.jqldsl.Expression expression, List<ObjectVariable> variables) {
        TimeStampLiteral timestampLiteral = (TimeStampLiteral) expression;
        return newTimestampConstantBuilder().withValue(timestampLiteral.getValue()).build();
    }

}
