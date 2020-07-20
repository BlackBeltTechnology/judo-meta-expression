package hu.blackbelt.judo.meta.expression.runtime;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newCollectionNavigationFromObjectExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newInstanceBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectNavigationExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.blackbelt.epsilon.runtime.execution.exceptions.ScriptExecutionException;
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.psm.ExpressionEpsilonValidatorOnPsm;
import hu.blackbelt.judo.meta.expression.collection.CollectionNavigationFromObjectExpression;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.object.ObjectVariableReference;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import hu.blackbelt.judo.meta.expression.temporal.TimestampAttribute;

public class IllegalPsmTest extends ExecutionContextOnPsmTest {
	
	@BeforeEach
    void setUp() throws Exception {
        super.setUp();
    }

	@Override
	protected ExpressionModel getExpressionModel() throws Exception {
        final ExpressionModelResourceSupport expressionModelResourceSupport = ExpressionModelResourceSupport.expressionModelResourceSupportBuilder()
                .uri(URI.createURI("expression:test"))
                .build();

        final TypeName orderType = newTypeNameBuilder().withNamespace("demo::entities").withName("InternationalOrder").build();
        final TypeName orderDetailType = newTypeNameBuilder().withNamespace("demo::entities").withName("OrderDetail").build();

        final Instance orderVar = newInstanceBuilder()
                .withElementName(orderType)
                .withName("self")
                .build();
        final ObjectVariableReference ref = newObjectVariableReferenceBuilder()
                .withVariable(orderVar)
                .build();
        final TimestampAttribute orderDate = newTimestampAttributeBuilder()
                .withObjectExpression(ref)
                .withAttributeName("orderDate")
                .build();

        final StringExpression shipperName = newStringAttributeBuilder()
                .withObjectExpression(newObjectNavigationExpressionBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(orderVar)
                                .build())
                        .withName("s")
                        .withReferenceName("shipper")
                        .build())
                .withAttributeName("companyName")
                .build();

        final CollectionNavigationFromObjectExpression orderDetails = newCollectionNavigationFromObjectExpressionBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(orderVar)
                        .build())
                .withReferenceName("orderDetails")
                .build();

        expressionModelResourceSupport.addContent(orderVar);
        expressionModelResourceSupport.addContent(orderType);
        expressionModelResourceSupport.addContent(orderDetailType);
        expressionModelResourceSupport.addContent(orderDate);
        expressionModelResourceSupport.addContent(shipperName);
        expressionModelResourceSupport.addContent(orderDetails);
        
        expressionModel = ExpressionModel.buildExpressionModel()
                .name("expression")
                .expressionModelResourceSupport(expressionModelResourceSupport)
                .build();
        
        return expressionModel;
	}
	
	@Test
	void test() {
		assertThrows(ScriptExecutionException.class, () -> ExpressionEpsilonValidatorOnPsm.validateExpressionOnPsm(log,
        		psmModel, getExpressionModel(),
        		ExpressionEpsilonValidator.calculateExpressionValidationScriptURI()));
	}

}
