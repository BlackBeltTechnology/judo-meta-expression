package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionModel;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport.expressionModelResourceSupportBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;

class ExecutionContextTest {

    @Test
    @DisplayName("Create Expression model with builder pattern")
    void testExpressionReflectiveCreated() throws Exception {


        String createdSourceModelName = "urn:expression.judo-meta-expression";

        ExpressionModelResourceSupport expressionModelSupport = expressionModelResourceSupportBuilder().build();
        Resource expressionResource = expressionModelSupport.getResourceSet().createResource(
                URI.createFileURI(createdSourceModelName));

        // Build model here
    }
}