package hu.blackbelt.judo.meta.expression.runtime;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;

@Disabled
class MinimalPsmTest extends ExecutionContextOnPsmTest {

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    void tearDown() {
        super.tearDown();
    }

    @Override
    protected Resource getExpressionResource() {
        final String createdSourceModelName = "urn:expression.judo-meta-expression";

        final ResourceSet executionResourceSet = ExpressionModelResourceSupport.createExpressionResourceSet();
        final Resource expressionResource = executionResourceSet.createResource(
                URI.createURI(createdSourceModelName));

        expressionResource.getContents().addAll(MinimalExpressionFactory.createMinimalExpression());

        return expressionResource;
    }

    @Test
    void test() throws Exception {
        execute();
    }
}
