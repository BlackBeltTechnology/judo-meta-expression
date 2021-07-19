package hu.blackbelt.judo.meta.expression.builder.jql;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;

import hu.blackbelt.judo.meta.expression.Expression;

public class JqlExpressionBuildException extends RuntimeException {

	private final EObject jclObject;
	private final List<JqlExpressionBuildingError> errors = new ArrayList<>();
	private final Expression partialExpression;

	public Expression getPartialExpression() {
		return partialExpression;
	}

	public EObject getJclObject() {
		return jclObject;
	}

	public JqlExpressionBuildException(Expression partialExpression, List<JqlExpressionBuildingError> errors) {
		super("Errors during building expression: " + errorDescriptions(errors));
		this.jclObject = null;
		this.partialExpression = partialExpression;
		this.errors.addAll(errors);
	}

	private static String errorDescriptions(List<JqlExpressionBuildingError> errors) {
		return errors.stream().map(JqlExpressionBuildingError::getDescription).collect(Collectors.joining());
	}

	public JqlExpressionBuildException(Expression partialExpression, List<JqlExpressionBuildingError> errors, final Throwable throwable) {
		super("Errors during building expression: " + errorDescriptions(errors), throwable);
		this.jclObject = null;
		this.partialExpression = partialExpression;
		this.errors.addAll(errors);
	}
	
	public List<JqlExpressionBuildingError> getErrors() {
		return errors;
	}
	
}
