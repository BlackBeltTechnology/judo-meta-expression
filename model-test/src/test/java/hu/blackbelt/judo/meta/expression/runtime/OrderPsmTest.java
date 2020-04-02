package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.adapters.psm.ExpressionEpsilonValidatorOnPsm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.LoadArguments.expressionLoadArgumentsBuilder;

@Disabled
class OrderPsmTest extends ExecutionContextOnPsmTest {

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
        ExpressionEpsilonValidatorOnPsm.validateExpressionOnPsm(log,
                psmModel, expressionModel,
                ExpressionEpsilonValidator.calculateExpressionValidationScriptURI());
    }
}
