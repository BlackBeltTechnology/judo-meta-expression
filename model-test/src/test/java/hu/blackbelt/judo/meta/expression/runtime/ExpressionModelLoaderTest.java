package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.runtime.ExpressionModel;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.LoadArguments.loadArgumentsBuilder;

public class ExpressionModelLoaderTest {

    static Logger log = LoggerFactory.getLogger(ExpressionModelLoaderTest.class);
	
    @Test
    @DisplayName("Load Expression Model")
    void loadExpressionModel() throws IOException {
        ResourceSet expressionResourceSet = ExpressionModelResourceSupport.createExpressionResourceSet();

        ExpressionModel expressionModel = ExpressionModel.loadExpressionModel(loadArgumentsBuilder()
                .resourceSet(Optional.of(expressionResourceSet))
                .uri(URI.createFileURI(new File("src/test/model/test.expression").getAbsolutePath()))
                .name("test")
                .build());

        for (Iterator<EObject> i = expressionModel.getResourceSet().getResource(expressionModel.getUri(), false).getAllContents(); i.hasNext(); ) {
            log.info(i.next().toString());
        }
    }
}