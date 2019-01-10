package hu.blackbelt.judo.meta.expression;

import hu.blackbelt.judo.meta.expression.util.ExpressionResourceFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ExpressionModelLoader {

    public static EObject loadModelFromString(final String modelString) {
        try {
            final File tmpFile = File.createTempFile("expression", "expr");
            final BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));
            writer.write(modelString);
            writer.close();

            final URI uri = URI.createFileURI(tmpFile.getAbsolutePath());
            final Resource resource = new ExpressionResourceFactoryImpl().createResource(uri);

            final Map options = new HashMap();
            resource.load(options);

            return null;
        } catch (IOException ex) {
            log.error("Unable to create temporary file for expression", ex);

            return null;
        }
    }
}
