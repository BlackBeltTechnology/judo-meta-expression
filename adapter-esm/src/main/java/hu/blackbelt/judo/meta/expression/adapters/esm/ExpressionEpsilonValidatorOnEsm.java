package hu.blackbelt.judo.meta.expression.adapters.esm;

import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.exceptions.ScriptExecutionException;
import hu.blackbelt.judo.meta.esm.runtime.EsmModel;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionEpsilonValidator;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionEvaluator;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionModel;
import org.eclipse.epsilon.common.util.UriUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static java.util.Collections.emptyList;

public class ExpressionEpsilonValidatorOnEsm extends ExpressionEpsilonValidator {

    public static void validateExpressionOnEsm(Log log, EsmModel esmModel, ExpressionModel expressionModel, URI scriptRoot)
            throws ScriptExecutionException, URISyntaxException {
        validateExpressionOnEsm(log, esmModel, expressionModel, scriptRoot, emptyList(), emptyList());
    }

    public static void validateExpressionOnEsm(Log log, EsmModel esmModel, ExpressionModel expressionModel, URI scriptRoot,
                                               Collection<String> expectedErrors, Collection<String> expectedWarnings)
            throws ScriptExecutionException, URISyntaxException {

        final Map<String, Object> injections = new HashMap<>();
        injections.put("evaluator", new ExpressionEvaluator());
        injections.put("modelAdapter", new EsmModelAdapter(esmModel.getResourceSet(), esmModel.getResourceSet()));

        ExecutionContext executionContext = executionContextBuilder()
                .log(log)
                .resourceSet(esmModel.getResourceSet())
                .metaModels(emptyList())
                .modelContexts(Arrays.asList(
                        wrappedEmfModelContextBuilder()
                                .log(log)
                                .name("ESM")
                                .resource(esmModel.getResource())
                                .validateModel(false)
                                .build(),
                        wrappedEmfModelContextBuilder()
                                .log(log)
                                .name("MEASURES")
                                .resource(esmModel.getResource())
                                .validateModel(false)
                                .build(),
                        wrappedEmfModelContextBuilder()
                                .log(log)
                                .name("EXPR")
                                .resource(expressionModel.getResource())
                                .validateModel(false)
                                .build()))
                .injectContexts(injections)
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
}
