package hu.blackbelt.judo.meta.expression.runtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.blackbelt.judo.meta.expression.adapters.psm.ExpressionEpsilonValidatorOnPsm;

class FullPsmTest extends ExecutionContextOnPsmTest {

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
    }

    @Test
    void test() throws Exception {
        ExpressionEpsilonValidatorOnPsm.validateExpressionOnPsm(log,
        		psmModel, expressionModel,
        		ExpressionEpsilonValidator.calculateExpressionValidationScriptURI());
    }
}
