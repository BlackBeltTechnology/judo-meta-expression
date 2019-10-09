package hu.blackbelt.judo.meta.expression.builder.jql.asm;

import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.asm.support.AsmModelResourceSupport;
import hu.blackbelt.judo.meta.expression.DataExpression;
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

        asmJqlExpressionBuilder = new AsmJqlExpressionBuilder(asmModelResourceSupport.getResourceSet(), measureModelResourceSupport.getResourceSet(), uri);

        asmUtils = new AsmUtils(asmModelResourceSupport.getResourceSet());
    }

    @AfterEach
    void tearDown() {
        asmJqlExpressionBuilder = null;
        asmUtils = null;
    }

    @Test
    void test() {
        final DataExpression orderDate = asmJqlExpressionBuilder.createAttributeBinding(asmUtils.all(EClass.class).filter(c -> "Order".equals(c.getName())).findAny().get(), "self.orderDate");
        log.info("Order date: {}", orderDate);
    }
}
