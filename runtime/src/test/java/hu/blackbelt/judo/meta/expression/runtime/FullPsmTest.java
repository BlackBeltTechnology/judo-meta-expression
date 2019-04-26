package hu.blackbelt.judo.meta.expression.runtime;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

@Disabled
class FullPsmTest extends ExecutionContextOnPsmTest {

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    void tearDown() {
        super.tearDown();
    }

    protected Resource getExpressionResource() throws Exception {
        final ResourceSet expressionModelResourceSet = ExpressionModelLoader.createExpressionResourceSet();
        ExpressionModelLoader.loadExpressionModel(expressionModelResourceSet,
                URI.createURI(new File(srcDir(), "test/models/t002.model").getAbsolutePath()),
                "test",
                "1.0.0");

        return expressionModelResourceSet.getResources().get(0);
    }

    @Test
    void test() throws Exception {
        execute();
    }
}
