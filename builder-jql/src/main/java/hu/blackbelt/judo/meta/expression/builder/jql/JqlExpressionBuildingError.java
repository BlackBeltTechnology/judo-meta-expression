package hu.blackbelt.judo.meta.expression.builder.jql;

import org.eclipse.emf.ecore.EObject;

public class JqlExpressionBuildingError {
	private final String description;
	private final EObject jclObject;
	
	public JqlExpressionBuildingError(String description, EObject jclObject) {
		this.description = description;
		this.jclObject = jclObject;
	}

	public String getDescription() {
		return description;
	}

	public EObject getJclObject() {
		return jclObject;
	}
	
	@Override
	public String toString() {
		return description + " " + jclObject;
	}

}
