package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.ExecutionContextOnAsmTest;
import hu.blackbelt.judo.meta.expression.adapters.asm.ExpressionEpsilonValidatorOnAsm;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MinimalAsmTest extends ExecutionContextOnAsmTest {

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        final ExpressionModelResourceSupport expressionModelResourceSupport = ExpressionModelResourceSupport.expressionModelResourceSupportBuilder()
                .uri(URI.createURI("expr:test"))
                .build();

        MinimalExpressionFactory.createMinimalExpression().forEach(e -> expressionModelResourceSupport.addContent(e));

        expressionModel = ExpressionModel.buildExpressionModel()
                .name("expr")
                .expressionModelResourceSupport(expressionModelResourceSupport)
                .build();
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
