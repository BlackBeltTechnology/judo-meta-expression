package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;
import hu.blackbelt.judo.meta.asm.support.AsmModelResourceSupport;
import hu.blackbelt.judo.meta.expression.adapters.asm.AsmModelAdapter;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;
import hu.blackbelt.judo.meta.measure.support.MeasureModelResourceSupport;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static hu.blackbelt.judo.meta.asm.runtime.AsmModel.loadAsmModel;
import static hu.blackbelt.judo.meta.asm.runtime.AsmModel.LoadArguments.asmLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.measure.runtime.MeasureModel.LoadArguments.measureLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.measure.runtime.MeasureModel.loadMeasureModel;

abstract class ExecutionContextOnAsmTest extends ExecutionContextTest {

    void setUp() throws Exception {
        final Resource asm = getAsmResource();
        final Resource measures = getMeasureResouce();
        modelAdapter = new AsmModelAdapter(asm.getResourceSet(), measures.getResourceSet());

        if (asm != null) {
            modelContexts.add(wrappedEmfModelContextBuilder()
                    .log(log)
                    .name("NORTHWIND")
                    .validateModel(false)
                    .aliases(ImmutableList.of("ASM"))
                    .resource(asm)
                    .build());
        }

        if (measures != null) {
            modelContexts.add(wrappedEmfModelContextBuilder()
                    .log(log)
                    .name("NORTHWIND_MEASURES")
                    .validateModel(false)
                    .aliases(ImmutableList.of("MEASURES"))
                    .resource(measures)
                    .build());
        }

        modelContexts.add(wrappedEmfModelContextBuilder()
                .log(log)
                .name("TEST")
                .validateModel(false)
                .aliases(ImmutableList.of("EXPR"))
                .resource(getExpressionResource())
                .build());
    }

    protected Resource getAsmResource() throws Exception {
        // Set our custom handler
        final File modelFile = new File("target/test-classes/model/northwind-asm.model");

//    	URIHandler uriHandler = new NioFilesystemnRelativePathURIHandlerImpl("urn", FileSystems.getDefault(), modelFile.getParentFile().getAbsolutePath());

        final ResourceSet asmModelResourceSet = AsmModelResourceSupport.createAsmResourceSet();
        asmModelResourceSet.setURIConverter(new ModelFileBasedUriConverter(modelFile));

        loadAsmModel(asmLoadArgumentsBuilder()
                .uri(URI.createFileURI(modelFile.getAbsolutePath()))
                .resourceSet(asmModelResourceSet)
                .name("test"));

        return asmModelResourceSet.getResources().get(0);
    }

    protected Resource getMeasureResouce() throws IOException, MeasureModel.MeasureValidationException {
        final ResourceSet measureModelResourceSet = MeasureModelResourceSupport.createMeasureResourceSet();

        loadMeasureModel(measureLoadArgumentsBuilder()
        		.resourceSet(measureModelResourceSet)
                .uri(URI.createFileURI(new File("target/test-classes/model/northwind-measure.model").getAbsolutePath()))
                .name("test"));
        
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
