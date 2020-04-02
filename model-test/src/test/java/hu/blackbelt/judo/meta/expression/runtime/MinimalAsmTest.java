package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.adapters.asm.ExpressionEpsilonValidatorOnAsm;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MinimalAsmTest extends ExecutionContextOnAsmTest {

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected ExpressionModel getExpressionModel() {
        final ExpressionModelResourceSupport expressionModelResourceSupport = ExpressionModelResourceSupport.expressionModelResourceSupportBuilder()
                .uri(URI.createURI("expr:test"))
                .build();

        MinimalExpressionFactory.createMinimalExpression().forEach(e -> expressionModelResourceSupport.addContent(e));

        return ExpressionModel.buildExpressionModel()
                .name("expr")
                .expressionModelResourceSupport(expressionModelResourceSupport)
                .build();
    }

    @Test
    void test() throws Exception {
    	ExpressionEpsilonValidatorOnAsm.validateExpressionOnAsm(log,
        		asmModel, measureModel, expressionModel ,
        		ExpressionEpsilonValidator.calculateExpressionValidationScriptURI());
    }
}
