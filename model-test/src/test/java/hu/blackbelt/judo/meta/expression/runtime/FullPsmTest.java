package hu.blackbelt.judo.meta.expression.runtime;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.blackbelt.judo.meta.expression.ExecutionContextOnPsmTest;
import hu.blackbelt.judo.meta.expression.adapters.psm.ExpressionEpsilonValidatorOnPsm;

class FullPsmTest extends ExecutionContextOnPsmTest {

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        expressionModel = ExpressionModelForTest.createExpressionModel();
        log.info(expressionModel.getDiagnosticsAsString());
    	assertTrue(expressionModel.isValid());
    }

    @Test
    void test() throws Exception {
        ExpressionEpsilonValidatorOnPsm.validateExpressionOnPsm(log,
        		psmModel, expressionModel,
        		ExpressionEpsilonValidator.calculateExpressionValidationScriptURI());
    }
}
