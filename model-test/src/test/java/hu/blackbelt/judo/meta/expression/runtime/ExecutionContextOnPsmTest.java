package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;

import hu.blackbelt.judo.meta.expression.adapters.psm.PsmModelAdapter;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;
import hu.blackbelt.judo.meta.measure.support.MeasureModelResourceSupport;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.psm.support.PsmModelResourceSupport;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;

abstract class ExecutionContextOnPsmTest extends ExecutionContextTest {

    void setUp() throws Exception {
        final Resource psm = getPsmResource();
        final Resource measures = getMeasureResouce();

        modelAdapter = new PsmModelAdapter(psm.getResourceSet(), measures.getResourceSet());

        if (psm != null) {
            modelContexts.add(wrappedEmfModelContextBuilder()
                    .log(log)
                    .name("NORTHWIND")
                    .validateModel(false)
                    .aliases(ImmutableList.of("JUDOPSM"))
                    .resource(psm)
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

    protected Resource getPsmResource() throws IOException  {
        final ResourceSet psmModelResourceSet = PsmModelResourceSupport.createPsmResourceSet();

        PsmModel.loadPsmModel(PsmModel.LoadArguments.loadArgumentsBuilder()
        		.resourceSet(Optional.of(psmModelResourceSet))
                .uri(URI.createFileURI(new File("src/test/model/northwind-judopsm.model").getAbsolutePath()))
                .name("test")
                .build());
        
        return psmModelResourceSet.getResources().get(0);
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

}
