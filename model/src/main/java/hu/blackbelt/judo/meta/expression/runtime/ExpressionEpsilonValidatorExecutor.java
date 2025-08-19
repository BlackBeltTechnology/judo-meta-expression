package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.exceptions.EvlScriptExecutionException;
import hu.blackbelt.epsilon.runtime.execution.exceptions.ScriptExecutionException;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.epsilon.common.util.UriUtil;
import org.slf4j.Logger;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static hu.blackbelt.judo.meta.expression.runtime.ExpressionScriptUriProvider.calculateExpressionValidationScriptURI;
import static java.util.Collections.emptyList;

public class ExpressionEpsilonValidatorExecutor implements ExpressionValidatorExecutor {

    ExpressionModel expressionModel;
    ModelAdapter modelAdapter;
    Resource adapted;
    Resource measure;
    Logger log;
    String adaptedName;
    String measuresName;

    public ExpressionEpsilonValidatorExecutor() {
    }

    public void setSource(Logger log, ExpressionModel expressionModel, ModelAdapter modelAdapter, String adaptedName, Resource adapted, String measuresName, Resource measure) {
        this.expressionModel = expressionModel;
        this.adapted = adapted;
        this.measure = measure;
        this.log = log;
        this.modelAdapter = modelAdapter;
        this.adaptedName = adaptedName;
        this.measuresName = measuresName;
    }

    public void execute(Collection<String> expectedErrors, Collection<String> expectedWarnings) throws ExpressionValidationException {
        final Map<String, Object> injections = new HashMap<>();
        injections.put("evaluator", new ExpressionEvaluator());
        injections.put("modelAdapter", modelAdapter);

        ExecutionContext executionContext = executionContextBuilder()
                .log(log)
                .resourceSet(adapted.getResourceSet())
                .metaModels(emptyList())
                .modelContexts(Arrays.asList(
                        wrappedEmfModelContextBuilder()
                                .log(log)
                                .name(adaptedName)
                                .resource(adapted)
                                .validateModel(false)
                                .useCache(true)
                                .build(),
                        wrappedEmfModelContextBuilder()
                                .log(log)
                                .name("MEASURES")
                                .resource(adapted)
                                .validateModel(false)
                                .useCache(true)
                                .build(),
                        wrappedEmfModelContextBuilder()
                                .log(log)
                                .name("EXPR")
                                .resource(expressionModel.getResource())
                                .validateModel(false)
                                .useCache(true)
                                .build()))
                .injectContexts(injections)
                .build();

        try {
            // run the model / metadata loading
            executionContext.load();

            // Transformation script
            executionContext
                    .executeProgram(evlExecutionContextBuilder()
                            .source(UriUtil.resolve("expression.evl", calculateExpressionValidationScriptURI()))
                            .parallel(true)
                            .expectedErrors(expectedErrors).expectedWarnings(expectedWarnings).build());

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (ScriptExecutionException e) {
            if (e instanceof EvlScriptExecutionException evl) {
                throw new ExpressionValidationException(ExpressionValidator.format(evl.getUnexpectedErrors(), evl.getUnexpectedWarnings()));
            }
            throw new ExpressionValidationException(e.getMessage());
        } finally {
            executionContext.commit();
            try {
                executionContext.close();
            } catch (Exception e) {
            }
        }
    }
}
