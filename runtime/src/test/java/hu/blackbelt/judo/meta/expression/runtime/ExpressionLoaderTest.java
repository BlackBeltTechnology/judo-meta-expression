package hu.blackbelt.judo.meta.expression.runtime;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

@Slf4j
class ExpressionLoaderTest {

    @Test
    void loadEsmModel() throws IOException {
        final ExpressionModel expressionModel = ExpressionModelLoader.loadExpressionModel(
                URI.createURI(new File(srcDir(), "test/models/t001.model").getAbsolutePath()),
                "test",
                "1.0.0");

        for (Iterator<EObject> i = expressionModel.getResourceSet().getResource(expressionModel.getUri(), false).getAllContents(); i.hasNext(); ) {
            log.debug(i.next().toString());
        }
    }

    public File srcDir() {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath + "../../src");
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }
}
