package hu.blackbelt.judo.meta.expression.builder.jql.asm;

import hu.blackbelt.judo.meta.expression.builder.jql.JqlExtractor;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;

import static hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport.SaveArguments.expressionSaveArgumentsBuilder;

@Slf4j
public class AsmJqlExtractorTest {

    private static final String TARGET_TEST_CLASSES = "target/test-classes";

    private JqlExtractor jqlExtractor;
    private ResourceSet expressionResourceSet;

    private ModelLoader modelLoader;

    @BeforeEach
    void setUp() {
        modelLoader = new ModelLoader();
        jqlExtractor = new AsmJqlExtractor(modelLoader.getAsmModel().getResourceSet(), modelLoader.getMeasureModel().getResourceSet(), URI.createURI("urn:test.judo-meta-expression"));
    }

    @AfterEach
    void tearDown(final TestInfo testInfo) throws Exception {
        final ExpressionModelResourceSupport expressionModelResourceSupport = ExpressionModelResourceSupport.expressionModelResourceSupportBuilder()
                .resourceSet(expressionResourceSet)
                .build();

        expressionModelResourceSupport.saveExpression(expressionSaveArgumentsBuilder()
                .file(new File(TARGET_TEST_CLASSES, testInfo.getDisplayName().replace("(", "").replace(")", "") + "-expression.model"))
                .build());

        jqlExtractor = null;
    }

    @Test
    void testExtract() {
        final long startTs = System.currentTimeMillis();
        expressionResourceSet = jqlExtractor.extractExpressions();
        final long endTs = System.currentTimeMillis();

        log.info("Extracted expressions in {} ms", (endTs - startTs));

        // test extracting expressions that are already extracted
        final long startTs2 = System.currentTimeMillis();
        expressionResourceSet = jqlExtractor.extractExpressions();
        final long endTs2 = System.currentTimeMillis();

        log.info("Extracted expressions again in {} ms", (endTs2 - startTs2));
    }
}
