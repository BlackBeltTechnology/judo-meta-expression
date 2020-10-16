package hu.blackbelt.judo.meta.expression.runtime;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newInstanceBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectNavigationExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.blackbelt.epsilon.runtime.execution.exceptions.ScriptExecutionException;
import hu.blackbelt.judo.meta.expression.ExecutionContextOnAsmTest;
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.asm.ExpressionEpsilonValidatorOnAsm;
import hu.blackbelt.judo.meta.expression.constant.Instance;

public class IllegalAsmTest extends ExecutionContextOnAsmTest {

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        
        expressionModel = ExpressionModelForTest.createExpressionModel();
        
        final TypeName orderType = newTypeNameBuilder().withNamespace("demo::entities").withName("InternationalOrder").build();
        final Instance orderVar = newInstanceBuilder()
                .withElementName(orderType)
                .withName("self")
                .build();

        final StringExpression shipperName = newStringAttributeBuilder()
                .withObjectExpression(newObjectNavigationExpressionBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(orderVar)
                                .build())
                        .withReferenceName("shippers")
                        .build())
                .withAttributeName("companyName")
                .build();
        
        expressionModel.addContent(orderType);
        expressionModel.addContent(orderVar);
        expressionModel.addContent(shipperName);
        
        log.info(expressionModel.getDiagnosticsAsString());
    	assertTrue(expressionModel.isValid());
    }
   
    @Test
    void test() {
        assertThrows(ScriptExecutionException.class, () -> ExpressionEpsilonValidatorOnAsm.validateExpressionOnAsm(log,
                asmModel, measureModel, expressionModel,
                ExpressionEpsilonValidator.calculateExpressionValidationScriptURI()));
    }
}
