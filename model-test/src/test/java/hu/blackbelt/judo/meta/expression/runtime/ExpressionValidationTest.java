package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionUtils;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport.expressionModelResourceSupportBuilder;


public class ExpressionValidationTest {

    private static final Logger log = LoggerFactory.getLogger(ExpressionValidationTest.class);
    private final String createdSourceModelName = "urn:Expression.model";
    private ExecutionContext executionContext;
    ExpressionModelResourceSupport expressionModelSupport;

    private ExpressionUtils expressionUtils;

    @BeforeEach
    void setUp() {

        expressionModelSupport = expressionModelResourceSupportBuilder()
                .uri(URI.createFileURI(createdSourceModelName))
                .build();

        Log log = new Slf4jLog();

        expressionUtils = new ExpressionUtils(expressionModelSupport.getResourceSet(), false);

        // Execution context
        executionContext = executionContextBuilder()
                .log(log)
                .resourceSet(expressionModelSupport.getResourceSet())
                .metaModels(ImmutableList.of())
                .modelContexts(ImmutableList.of(
                        wrappedEmfModelContextBuilder()
                                .log(log)
                                .name("EXPR")
                                .resource(expressionModelSupport.getResource())
                                .build()))
                .injectContexts(ImmutableMap.of(
                        "evaluator", new ExpressionEvaluator(),
                        "expressionUtils", expressionUtils
                ))
                .build();
    }

    @Test
    public void test() throws Exception {
        runEpsilon(ImmutableList.of(), null);
    }

    private void runEpsilon(Collection<String> expectedErrors, Collection<String> expectedWarnings) throws Exception {
        // run the model / metadata loading
        executionContext.load();

        // Transformation script
        executionContext.executeProgram(
                evlExecutionContextBuilder()
                        .source(new File("../model/src/main/epsilon/validations/expression.evl").toURI())
                        .expectedErrors(expectedErrors)
                        .expectedWarnings(expectedWarnings)
                        .build());

        executionContext.commit();
        executionContext.close();
    }
}
