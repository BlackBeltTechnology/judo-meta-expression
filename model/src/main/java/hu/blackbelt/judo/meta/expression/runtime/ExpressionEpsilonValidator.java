package hu.blackbelt.judo.meta.expression.runtime;

import java.net.URI;
import java.net.URISyntaxException;

public abstract class ExpressionEpsilonValidator {

	public static URI calculateExpressionValidationScriptURI() throws URISyntaxException {
		URI expressionRoot = ExpressionModel.class.getProtectionDomain().getCodeSource().getLocation().toURI();
		if (expressionRoot.toString().endsWith(".jar")) {
			expressionRoot = new URI("jar:" + expressionRoot.toString() + "!/validations/");
		} else if (expressionRoot.toString().startsWith("jar:bundle:")) {
// bundle://37.0:0/validations/
// jar:bundle://37.0:0/!/validations/expression.evl
			expressionRoot = new URI(
					expressionRoot.toString().substring(4, expressionRoot.toString().indexOf("!")) + "validations/");
		} else {
			expressionRoot = new URI(expressionRoot.toString() + "/validations/");
		}
		return expressionRoot;

	}

}
