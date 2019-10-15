package hu.blackbelt.judo.meta.expression.runtime;

import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.loadExpressionModel;
import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.LoadArguments.expressionLoadArgumentsBuilder;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.blackbelt.epsilon.runtime.execution.exceptions.ScriptExecutionException;

public class IllegalAsmTest extends ExecutionContextOnAsmTest {

	@BeforeEach
    void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    void tearDown() {
        modelContexts.clear();
    }

	@Override
	protected Resource getExpressionResource() throws Exception {
		return loadExpressionModel(expressionLoadArgumentsBuilder()
                .uri(URI.createFileURI(new File("src/test/model/t004.model").getAbsolutePath()))
                .name("test")
                .build()).getResourceSet().getResources().get(0);
	}
	
	@Test
	void test() throws Exception {
		
		assertThrows(ScriptExecutionException.class, () -> ExpressionEpsilonValidator.validateExpression(log, 
        		modelContexts,
        		ExpressionEpsilonValidator.calculateExpressionValidationScriptURI(), 
        		modelAdapter));
	}

}
