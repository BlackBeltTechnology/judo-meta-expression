package hu.blackbelt.judo.meta.expression.builder.jql.asm;

import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.asm.support.AsmModelResourceSupport;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.adapters.asm.AsmModelAdapter;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import hu.blackbelt.judo.meta.measure.support.MeasureModelResourceSupport;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport.SaveArguments.expressionSaveArgumentsBuilder;

@Slf4j
public class AsmJqlExpressionBuilderTest {

    private static final String TARGET_TEST_CLASSES = "target/test-classes";

    private AsmJqlExpressionBuilder asmJqlExpressionBuilder;

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
        asmJqlExpressionBuilder = new AsmJqlExpressionBuilder(asmModelResourceSupport.getResourceSet(), measureModelResourceSupport.getResourceSet(), expressionModelResourceSupport.getResource(), modelAdapter);

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

        final EStructuralFeature orderDate = order.getEStructuralFeature("orderDate");
        final AsmJqlExpressionBuilder.BindingContext bindingContext = new AsmJqlExpressionBuilder.BindingContext(orderDate, false);
        asmJqlExpressionBuilder.createBinding(bindingContext, expression);

        log.info("Order date: {}", expression);
    }

    @Test
    void testOrderCategories() {
        testName = "OrderCategories";

        final EClass order = asmUtils.all(EClass.class).filter(c -> "Order".equals(c.getName())).findAny().get();
        final Expression expression = asmJqlExpressionBuilder.createExpression(order, "self.orderDetails.product.category");

        final EStructuralFeature orderDate = order.getEStructuralFeature("categories");
        final AsmJqlExpressionBuilder.BindingContext bindingContext = new AsmJqlExpressionBuilder.BindingContext(orderDate, false);
        asmJqlExpressionBuilder.createBinding(bindingContext, expression);

        log.info("Order categories: {}", expression);
    }

    @Test
    void testSimpleArithmeticOperation() {
        testName = "SimpleArithmeticOperation";

        final Expression expression = asmJqlExpressionBuilder.createExpression(null, "2*3");
        log.info("Simple arithmetic operation: {}", expression);
    }
}
