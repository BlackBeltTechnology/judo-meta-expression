package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.adapters.psm.ExpressionEpsilonValidatorOnPsm;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class MinimalPsmTest extends ExecutionContextOnPsmTest {

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
        ExpressionEpsilonValidatorOnPsm.validateExpressionOnPsm(log,
                psmModel, expressionModel,
                ExpressionEpsilonValidator.calculateExpressionValidationScriptURI());
    }
}
