package hu.blackbelt.judo.meta.expression.builder.jql.asm;

import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.asm.support.AsmModelResourceSupport;
import hu.blackbelt.judo.meta.expression.DataExpression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.adapters.asm.AsmModelAdapter;
import hu.blackbelt.judo.meta.measure.support.MeasureModelResourceSupport;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

@Slf4j
public class AsmJqlExpressionBuilderTest {

    private AsmJqlExpressionBuilder asmJqlExpressionBuilder;

    private AsmUtils asmUtils;

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

        final URI uri = URI.createURI("urn:test.judo-meta-expression");

        final AsmModelAdapter modelAdapter = new AsmModelAdapter(asmModelResourceSupport.getResourceSet(), measureModelResourceSupport.getResourceSet());
        asmJqlExpressionBuilder = new AsmJqlExpressionBuilder(asmModelResourceSupport.getResourceSet(), measureModelResourceSupport.getResourceSet(), modelAdapter, uri);

        asmUtils = new AsmUtils(asmModelResourceSupport.getResourceSet());
    }

    @AfterEach
    void tearDown() {
        asmJqlExpressionBuilder = null;
        asmUtils = null;
    }

    @Test
    void testOrderDate() {
        final AsmJqlExpressionBuilder.EvaluationContext evaluationContext = new AsmJqlExpressionBuilder.EvaluationContext("orderDate", false);
        final DataExpression orderDate = asmJqlExpressionBuilder.createAttributeBinding(asmUtils.all(EClass.class).filter(c -> "Order".equals(c.getName())).findAny().get(), "self.orderDate", evaluationContext);
        log.info("Order date: {}", orderDate);
    }

    @Test
    void testOrderCategories() {
        final AsmJqlExpressionBuilder.EvaluationContext evaluationContext = new AsmJqlExpressionBuilder.EvaluationContext("categories", false);
        final ReferenceExpression categories = asmJqlExpressionBuilder.createRelationBinding(asmUtils.all(EClass.class).filter(c -> "Order".equals(c.getName())).findAny().get(), "self.orderDetails.product.category", evaluationContext);
        log.info("Order categories: {}", categories);
    }

    @Test
    void testSimpleArithmeticOperation() {
        final AsmJqlExpressionBuilder.EvaluationContext evaluationContext = new AsmJqlExpressionBuilder.EvaluationContext(null, false);
        final DataExpression sum = asmJqlExpressionBuilder.createAttributeBinding(null, "2*3", evaluationContext);
        log.info("Simple arithmetic operation: {}", sum);
    }
}
