package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.Expression;

/**
 * Represents an expression that might not be fully built due to parsing errors.
 */
public class PartialExpression<TO> {

	private final Expression expression;
	private final JqlExpressionBuildingContext<TO> context;
	
	public PartialExpression(Expression expression, JqlExpressionBuildingContext<TO> context) {
		this.expression = expression;
		this.context = context;
	}

	public Expression getExpression() {
		return expression;
	}

	public JqlExpressionBuildingContext<TO> getContext() {
		return context;
	}

	
	
}
