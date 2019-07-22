package hu.blackbelt.judo.meta.expression.runtime;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.LoadArguments.loadArgumentsBuilder;

class FullAsmTest extends ExecutionContextOnAsmTest {

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    void tearDown() {
        super.tearDown();
    }

    protected Resource getExpressionResource() throws Exception {
        return ExpressionModel.loadExpressionModel(loadArgumentsBuilder()
                .uri(URI.createFileURI(new File("src/test/model/t002.model").getAbsolutePath()))
                .name("test")
                .build()).getResourceSet().getResources().get(0);
    }

    @Test
    void test() throws Exception {
        execute();
    }
}
