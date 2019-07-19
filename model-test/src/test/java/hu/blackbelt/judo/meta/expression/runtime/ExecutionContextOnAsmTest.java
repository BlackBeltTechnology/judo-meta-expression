package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;
import hu.blackbelt.epsilon.runtime.execution.impl.NioFilesystemnRelativePathURIHandlerImpl;
import hu.blackbelt.judo.meta.asm.runtime.AsmModel;
import hu.blackbelt.judo.meta.asm.support.AsmModelResourceSupport;
import hu.blackbelt.judo.meta.expression.adapters.asm.AsmModelAdapter;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;
import hu.blackbelt.judo.meta.measure.support.MeasureModelResourceSupport;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Optional;

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
        final File modelFile = new File("src/test/model/northwind-asm.model");

//    	URIHandler uriHandler = new NioFilesystemnRelativePathURIHandlerImpl("urn", FileSystems.getDefault(), modelFile.getParentFile().getAbsolutePath());

        final ResourceSet asmModelResourceSet = AsmModelResourceSupport.createAsmResourceSet();
        asmModelResourceSet.setURIConverter(new ModelFileBasedUriConverter(modelFile));

        AsmModel.loadAsmModel(AsmModel.LoadArguments.loadArgumentsBuilder()
                .uri(URI.createFileURI(modelFile.getAbsolutePath()))
                .name("test")
                .build());

        return asmModelResourceSet.getResources().get(0);
    }

    protected Resource getMeasureResouce() throws IOException  {
        final ResourceSet measureModelResourceSet = MeasureModelResourceSupport.createMeasureResourceSet();

        MeasureModel.loadMeasureModel(MeasureModel.LoadArguments.loadArgumentsBuilder()
        		.resourceSet(Optional.of(measureModelResourceSet))
                .uri(URI.createFileURI(new File("src/test/model/measure.model").getAbsolutePath()))
                .name("test")
                .build());
        
        return measureModelResourceSet.getResources().get(0);
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
