package hu.blackbelt.judo.meta.expression.runtime;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;

import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.LoadArguments.loadArgumentsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

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

    protected Resource getExpressionResource() throws IOException {
        ResourceSet expressionResourceSet = ExpressionModelResourceSupport.createExpressionResourceSet();

        ExpressionModel expressionModel = ExpressionModel.loadExpressionModel(loadArgumentsBuilder()
                .resourceSet(Optional.of(expressionResourceSet))
                .uri(URI.createFileURI(new File("src/test/model/t002.model").getAbsolutePath()))
                .name("test")
                .build());

        return expressionResourceSet.getResources().get(0);
    }

    @Test
    void test() throws Exception {
        execute();
    }
}
