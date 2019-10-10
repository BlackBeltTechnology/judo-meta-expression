package hu.blackbelt.judo.meta.expression.runtime;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.LoadArguments.expressionLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.loadExpressionModel;

import java.io.File;

class OrderPsmTest extends ExecutionContextOnPsmTest {

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    void tearDown() {
        modelContexts.clear();
    }

    protected Resource getExpressionResource() throws Exception {
        return loadExpressionModel(expressionLoadArgumentsBuilder()
                .uri(URI.createFileURI(new File("src/test/model/t003.model").getAbsolutePath()))
                .name("test")
                // TODO: check model
                .validateModel(false)
                .build()).getResourceSet().getResources().get(0);
    }

    @Test
    void test() throws Exception {
    	ExpressionEpsilonValidator.validateExpression(log, 
        		modelContexts ,
        		ExpressionEpsilonValidator.calculateExpressionValidationScriptURI(), 
        		modelAdapter);
    }
}
