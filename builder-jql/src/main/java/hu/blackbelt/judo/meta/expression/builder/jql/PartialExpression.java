package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.Expression;

/**
 * Represents an expression that might not be fully built due to parsing errors.
 */
public class PartialExpression {

	private final Expression expression;
	private final JqlExpressionBuildingContext context;
	
	public PartialExpression(Expression expression, JqlExpressionBuildingContext context) {
		this.expression = expression;
		this.context = context;
	}

	public Expression getExpression() {
		return expression;
	}

	public JqlExpressionBuildingContext getContext() {
		return context;
	}

	
	
}
