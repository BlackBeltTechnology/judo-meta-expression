package hu.blackbelt.judo.meta.expression.runtime;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;

import hu.blackbelt.judo.meta.expression.runtime.ExpressionModel;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.epsilon.common.util.UriUtil;

import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.exceptions.ScriptExecutionException;

public class ExpressionEpsilonValidator {

	public static void validateExpression(Log log, ExpressionModel expressionModel, URI scriptRoot)
			throws ScriptExecutionException, URISyntaxException {
		validateExpression(log, expressionModel, scriptRoot, emptyList(), emptyList());
	}

	public static void validateExpression(Log log, ExpressionModel expressionModel, URI scriptRoot,
			Collection<String> expectedErrors, Collection<String> expectedWarnings)
			throws ScriptExecutionException, URISyntaxException {
		
		ExecutionContext executionContext = executionContextBuilder()
                .log(log)
                .resourceSet(expressionModel.getResourceSet())
                .metaModels(emptyList())
                .modelContexts(Arrays.asList(
                        wrappedEmfModelContextBuilder()
                                .log(log)
                                .name("EXPR")
                                .resource(expressionModel.getResource())
                                .validateModel(false)
                                .build()))
                .injectContexts(singletonMap("evaluator", new ExpressionEvaluator()))
                .injectContexts(singletonMap("expressionUtils", new ExpressionUtils()))
                .build();

		try {
// run the model / metadata loading
			executionContext.load();

// Transformation script
			executionContext
					.executeProgram(evlExecutionContextBuilder().source(UriUtil.resolve("expression.evl", scriptRoot))
							.expectedErrors(expectedErrors).expectedWarnings(expectedWarnings).build());

		} finally {
			executionContext.commit();
			try {
				executionContext.close();
			} catch (Exception e) {
			}
		}
	}

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
