package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;
import hu.blackbelt.epsilon.runtime.execution.impl.NioFilesystemnRelativePathURIHandlerImpl;
import hu.blackbelt.judo.meta.asm.runtime.AsmModelLoader;
import hu.blackbelt.judo.meta.expression.adapters.asm.AsmModelAdapter;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModelLoader;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;

abstract class ExecutionContextOnAsmTest extends ExecutionContextTest {

    void setUp() throws Exception {
        final Resource asm = getAsmResource();
        final Resource measures = getMeasureResouce();
        modelAdapter = new AsmModelAdapter(asm.getResourceSet(), measures.getResourceSet());

        if (asm != null) {
            modelContexts.add(wrappedEmfModelContextBuilder()
                    .log(log)
                    .name("NORTHWIND")
                    .aliases(ImmutableList.of("ASM"))
                    .resource(asm)
                    .build());
        }

        if (measures != null) {
            modelContexts.add(wrappedEmfModelContextBuilder()
                    .log(log)
                    .name("NORTHWIND_MEASURES")
                    .aliases(ImmutableList.of("MEASURES"))
                    .resource(measures)
                    .build());
        }

        modelContexts.add(wrappedEmfModelContextBuilder()
                .log(log)
                .name("TEST")
                .aliases(ImmutableList.of("EXPR"))
                .resource(getExpressionResource())
                .build());
    }

    protected Resource getAsmResource() throws Exception {
        // Set our custom handler
        URIHandler uriHandler = new NioFilesystemnRelativePathURIHandlerImpl("urn", FileSystems.getDefault(), targetDir().getAbsolutePath());

        final File modelFile = new File(srcDir(), "test/models/northwind-asm.model");
        final ResourceSet asmModelResourceSet = AsmModelLoader.createAsmResourceSet(uriHandler, new AsmModelLoader.LocalAsmPackageRegistration());
        asmModelResourceSet.setURIConverter(new ModelFileBasedUriConverter(modelFile));
        AsmModelLoader.loadAsmModel(asmModelResourceSet,
                URI.createURI(modelFile.getAbsolutePath()),"test","1.0.0");

        return asmModelResourceSet.getResources().get(0);
    }

    protected Resource getMeasureResouce() throws IOException  {
        final ResourceSet measureModelResourceSet = MeasureModelLoader.createMeasureResourceSet();
        MeasureModelLoader.loadMeasureModel(measureModelResourceSet,
                URI.createURI(new File(srcDir(), "test/models/measure.model").getAbsolutePath()),
                "test",
                "1.0.0");

        return measureModelResourceSet.getResources().get(0);
    }

    public File targetDir(){
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath);
        if(!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }

    public static class ModelFileBasedUriConverter extends ExtensibleURIConverterImpl {

        private final File baseFile;

        public ModelFileBasedUriConverter(final File baseFile) {
            this.baseFile = baseFile;
        }

        @Override
        public URI normalize(URI uri) {
            String fragment = uri.fragment();
            String query = uri.query();
            URI trimmedURI = uri.trimFragment().trimQuery();
            URI result = getInternalURIMap().getURI(trimmedURI);
            String scheme = result.scheme();
            if (scheme == null) {
                if (result.hasAbsolutePath()) {
                    result = URI.createURI("file:" + result);
                } else {
                    result = URI.createFileURI(new File(baseFile, result.toString()).getAbsolutePath());
                }
            }

            if (result == trimmedURI) {
                return uri;
            }

            if (query != null) {
                result = result.appendQuery(query);
            }
            if (fragment != null) {
                result = result.appendFragment(fragment);
            }


            return normalize(result);
        }
    }
}
