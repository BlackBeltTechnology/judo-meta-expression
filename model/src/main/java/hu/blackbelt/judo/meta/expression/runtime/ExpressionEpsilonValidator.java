package hu.blackbelt.judo.meta.expression.runtime;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static java.util.Collections.emptyList;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionModel;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.epsilon.common.util.UriUtil;

import com.google.common.collect.ImmutableMap;

import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.api.ModelContext;
import hu.blackbelt.epsilon.runtime.execution.exceptions.ScriptExecutionException;
import static hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport.createExpressionResourceSet;

public class ExpressionEpsilonValidator {

	public static void validateExpression(Log log, List<ModelContext> modelContexts, URI scriptRoot, ModelAdapter modelAdapter)
			throws ScriptExecutionException, URISyntaxException {
		validateExpression(log, modelContexts, scriptRoot, modelAdapter, emptyList(), emptyList());
	}

	public static void validateExpression(Log log, List<ModelContext> modelContexts, URI scriptRoot, ModelAdapter modelAdapter,
			Collection<String> expectedErrors, Collection<String> expectedWarnings)
			throws ScriptExecutionException, URISyntaxException {
		
		final ResourceSet executionResourceSet = createExpressionResourceSet();
		
		ExecutionContext executionContext = executionContextBuilder()
                .log(log)
                .resourceSet(executionResourceSet)
                .metaModels(emptyList())
                .modelContexts(modelContexts)
                .injectContexts(ImmutableMap.of(
                        "evaluator", new ExpressionEvaluator(),
                        "modelAdapter", modelAdapter))
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
