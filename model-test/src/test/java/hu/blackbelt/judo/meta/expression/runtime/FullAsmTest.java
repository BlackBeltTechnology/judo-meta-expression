package hu.blackbelt.judo.meta.expression.runtime;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.blackbelt.judo.meta.expression.adapters.asm.ExpressionEpsilonValidatorOnAsm;

class FullAsmTest extends ExecutionContextOnAsmTest {

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
        expressionModel = ExpressionModelForTest.createExpressionModel();
        log.info(expressionModel.getDiagnosticsAsString());
    	assertTrue(expressionModel.isValid());
    }

    @Test
    void test() throws Exception {
    	ExpressionEpsilonValidatorOnAsm.validateExpressionOnAsm(log,
        		asmModel, measureModel, expressionModel,
        		ExpressionEpsilonValidator.calculateExpressionValidationScriptURI());
    }
}
