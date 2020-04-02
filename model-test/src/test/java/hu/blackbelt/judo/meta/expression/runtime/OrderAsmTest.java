package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.adapters.asm.ExpressionEpsilonValidatorOnAsm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.LoadArguments.expressionLoadArgumentsBuilder;

class OrderAsmTest extends ExecutionContextOnAsmTest {

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected ExpressionModel getExpressionModel() throws Exception {
        return ExpressionModel.loadExpressionModel(expressionLoadArgumentsBuilder()
                .name("expression")
                .file(new File("src/test/model/t003.model"))
                .build());
    }

    @Test
    void test() throws Exception {
    	ExpressionEpsilonValidatorOnAsm.validateExpressionOnAsm(log,
        		asmModel, measureModel, expressionModel ,
        		ExpressionEpsilonValidator.calculateExpressionValidationScriptURI());
    }
}
