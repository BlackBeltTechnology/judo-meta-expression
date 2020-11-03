package hu.blackbelt.judo.meta.expression.builder.jql.constant;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newDateConstantBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlExpressionTransformerFunction;
import hu.blackbelt.judo.meta.jql.jqldsl.DateLiteral;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;

public class JqlDateLiteralTransformer implements JqlExpressionTransformerFunction {

	@Override
	public Expression apply(JqlExpression jqlExpression, ExpressionBuildingVariableResolver context) {
		DateLiteral dateLiteral = (DateLiteral) jqlExpression;
		LocalDate localDate = LocalDate.parse(dateLiteral.getValue(), DateTimeFormatter.ISO_LOCAL_DATE);
		return newDateConstantBuilder().withValue(localDate).build();
	}

}
