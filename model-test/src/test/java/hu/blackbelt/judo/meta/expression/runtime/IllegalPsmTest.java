package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.epsilon.runtime.execution.exceptions.ScriptExecutionException;
import hu.blackbelt.judo.meta.expression.adapters.psm.ExpressionEpsilonValidatorOnPsm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.LoadArguments.expressionLoadArgumentsBuilder;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IllegalPsmTest extends ExecutionContextOnPsmTest {
	
	@BeforeEach
    void setUp() throws Exception {
        super.setUp();
    }

	@Override
	protected ExpressionModel getExpressionModel() throws Exception {
		return ExpressionModel.loadExpressionModel(expressionLoadArgumentsBuilder()
				.name("expression")
				.file(new File("src/test/model/t004.model"))
				.build());
	}
	
	@Test
	void test() {
		assertThrows(ScriptExecutionException.class, () -> ExpressionEpsilonValidatorOnPsm.validateExpressionOnPsm(log,
        		psmModel, expressionModel,
        		ExpressionEpsilonValidator.calculateExpressionValidationScriptURI()));
	}

}
