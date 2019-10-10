package hu.blackbelt.judo.meta.expression.builder.jql.asm;

import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.asm.support.AsmModelResourceSupport;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.adapters.asm.AsmModelAdapter;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import hu.blackbelt.judo.meta.measure.Measure;
import hu.blackbelt.judo.meta.measure.Unit;
import hu.blackbelt.judo.meta.measure.support.MeasureModelResourceSupport;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport.SaveArguments.expressionSaveArgumentsBuilder;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class AsmJqlExpressionBuilderTest {

    private static final String TARGET_TEST_CLASSES = "target/test-classes";

    private JqlExpressionBuilder<EClassifier, EDataType, EAttribute, EEnum, EClass, EReference, Measure, Unit> asmJqlExpressionBuilder;

    private AsmUtils asmUtils;
    private String testName;
    private ExpressionModelResourceSupport expressionModelResourceSupport;

    @BeforeEach
    void setUp() throws Exception {
        final AsmModelResourceSupport asmModelResourceSupport = AsmModelResourceSupport.loadAsm(AsmModelResourceSupport.LoadArguments.asmLoadArgumentsBuilder()
                .file(new File("src/test/model/asm.model"))
                .uri(URI.createURI("urn:test.judo-meta-asm"))
                .build());
        final MeasureModelResourceSupport measureModelResourceSupport = MeasureModelResourceSupport.loadMeasure(MeasureModelResourceSupport.LoadArguments.measureLoadArgumentsBuilder()
                .file(new File("src/test/model/measure.model"))
                .uri(URI.createURI("urn:test.judo-meta-measure"))
                .build());

        expressionModelResourceSupport = ExpressionModelResourceSupport.expressionModelResourceSupportBuilder()
                .uri(URI.createURI("urn:test.judo-meta-expression"))
                .build();

        final AsmModelAdapter modelAdapter = new AsmModelAdapter(asmModelResourceSupport.getResourceSet(), measureModelResourceSupport.getResourceSet());
        asmJqlExpressionBuilder = new JqlExpressionBuilder<>(modelAdapter, expressionModelResourceSupport.getResource());

        asmUtils = new AsmUtils(asmModelResourceSupport.getResourceSet());
    }

    @AfterEach
    void tearDown() throws Exception {
        expressionModelResourceSupport.saveExpression(expressionSaveArgumentsBuilder()
                .file(new File(TARGET_TEST_CLASSES, testName + "-expression.model"))
                .build());

        asmJqlExpressionBuilder = null;
        asmUtils = null;
    }

    @Test
    void testOrderDate() {
        testName = "OrderDate";

        final EClass order = asmUtils.all(EClass.class).filter(c -> "Order".equals(c.getName())).findAny().get();
        final Expression expression = asmJqlExpressionBuilder.createExpression(order, "self.orderDate");
        assertNotNull(expression);

        final JqlExpressionBuilder.BindingContext bindingContext = new JqlExpressionBuilder.BindingContext("orderDate", JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.GETTER);
        asmJqlExpressionBuilder.createBinding(bindingContext, order, expression);

        log.info("Order date: {}", expression);
    }

    @Test
    void testOrderCategories() {
        testName = "OrderCategories";

        final EClass order = asmUtils.all(EClass.class).filter(c -> "Order".equals(c.getName())).findAny().get();
        final Expression expression = asmJqlExpressionBuilder.createExpression(order, "self.orderDetails.product.category");
        assertNotNull(expression);

        final JqlExpressionBuilder.BindingContext bindingContext = new JqlExpressionBuilder.BindingContext("categories", JqlExpressionBuilder.BindingType.RELATION, JqlExpressionBuilder.BindingRole.GETTER);
        asmJqlExpressionBuilder.createBinding(bindingContext, order, expression);

        log.info("Order categories: {}", expression);
    }

    @Test
    void testSimpleArithmeticOperation() {
        testName = "SimpleArithmeticOperation";

        final Expression expression = asmJqlExpressionBuilder.createExpression(null, "2*3");
        log.info("Simple arithmetic operation: {}", expression);
        assertNotNull(expression);
    }
}
