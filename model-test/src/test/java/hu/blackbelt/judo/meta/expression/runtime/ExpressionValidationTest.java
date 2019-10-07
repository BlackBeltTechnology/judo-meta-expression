package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.exceptions.EvlScriptExecutionException;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionUtils;
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


public class ExpressionValidationTest {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionValidationTest.class);
    private final String createdSourceModelName = "urn:Expression.model";
    private ExecutionContext executionContext;
    ExpressionModelResourceSupport expressionModelSupport;
    
    Log log = new Slf4jLog();

    private ExpressionModel expressionModel;
    private ExpressionUtils expressionUtils;

    @BeforeEach
    void setUp() {

        expressionModelSupport = expressionModelResourceSupportBuilder()
                .uri(URI.createFileURI(createdSourceModelName))
                .build();
        
        expressionModel = ExpressionModel.buildModel()
        		.asmModelResourceSupport(expressionModelSupport)
                .uri(URI.createURI(createdSourceModelName))
                .name("test")
                .build();
    }

    private void runEpsilon (Collection<String> expectedErrors, Collection<String> expectedWarnings) throws Exception {
        try {
            ExpressionEpsilonValidator.validateExpression(log,
                    expressionModel,
                    ExpressionEpsilonValidator.calculateExpressionValidationScriptURI(),
                    expectedErrors,
                    expectedWarnings);
        } catch (EvlScriptExecutionException ex) {
            logger.error("EVL failed", ex);
            logger.error("\u001B[31m - expected errors: {}\u001B[0m", expectedErrors);
            logger.error("\u001B[31m - unexpected errors: {}\u001B[0m", ex.getUnexpectedErrors());
            logger.error("\u001B[31m - errors not found: {}\u001B[0m", ex.getErrorsNotFound());
            logger.error("\u001B[33m - expected warnings: {}\u001B[0m", expectedWarnings);
            logger.error("\u001B[33m - unexpected warnings: {}\u001B[0m", ex.getUnexpectedWarnings());
            logger.error("\u001B[33m - warnings not found: {}\u001B[0m", ex.getWarningsNotFound());
            throw ex;
        }
    }
}
