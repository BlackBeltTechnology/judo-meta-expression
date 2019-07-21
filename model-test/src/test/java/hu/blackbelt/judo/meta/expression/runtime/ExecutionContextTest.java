package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.api.ModelContext;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.*;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport.createExpressionResourceSet;

abstract class ExecutionContextTest {

    protected List<ModelContext> modelContexts = new ArrayList<>();
    protected ModelAdapter modelAdapter;

    protected final Log log = new Slf4jLog();

    void tearDown() {
        modelContexts.clear();
    }

    void execute() throws Exception {
        final ResourceSet executionResourceSet = createExpressionResourceSet();

        // Execution context
        ExecutionContext executionContext = executionContextBuilder()
                .log(log)
                .resourceSet(executionResourceSet)
                .metaModels(getMetaModels())
                .modelContexts(modelContexts)
                .injectContexts(ImmutableMap.of(
                        "evaluator", new ExpressionEvaluator(),
                        "modelAdapter", modelAdapter))
                .build();

        // run the model / metadata loading
        executionContext.load();

        // Validation script
        executionContext.executeProgram(
                evlExecutionContextBuilder()
                        .source(new File("../model/src/main/epsilon/validations/expression.evl").toURI())
                        .expectedErrors(getExpectedErrors())
                        .expectedWarnings(getExpectedWarnings())
                        .build());

        // Transformation script
//        final String expr2eval = getExpressionToEvaluationEtlSource();
//        if (expr2eval != null) {
//            executionContext.addModel();
//
//            executionContext.executeProgram(
//                    etlExecutionContextBuilder()
//                            .source(expr2eval)
//                            .build());
//        }

        executionContext.commit();
        executionContext.close();
    }

    protected List<String> getMetaModels() {
        return ImmutableList.of();
    }

    protected abstract Resource getExpressionResource() throws Exception;

    protected List<String> getExpectedErrors() {
        return null;
    }

    protected List<String> getExpectedWarnings() {
        return null;
    }
}
