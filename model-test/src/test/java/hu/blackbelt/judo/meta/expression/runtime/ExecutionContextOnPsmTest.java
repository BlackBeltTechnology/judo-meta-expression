package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;

import hu.blackbelt.judo.meta.expression.adapters.psm.PsmModelAdapter;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

import java.io.File;
import java.io.IOException;

import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;

abstract class ExecutionContextOnPsmTest extends ExecutionContextTest {

    void setUp() throws Exception {
        final Resource psm = getPsmResource();
        final Resource measures = getMeasureResource();

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
        return PsmModel.loadPsmModel(PsmModel.LoadArguments.loadArgumentsBuilder()
        		//.resourceSet(Optional.of(psmModelResourceSet))
                .uri(URI.createFileURI(new File("src/test/model/northwind-judopsm.model").getAbsolutePath()))
                .name("test")
                .build()).getResourceSet().getResources().get(0);
    }


    protected Resource getMeasureResource() throws IOException  {
        return PsmModel.loadPsmModel(PsmModel.LoadArguments.loadArgumentsBuilder()
        		//.resourceSet(Optional.of(measureModelResourceSet))
                .uri(URI.createFileURI(new File("src/test/model/northwind-judopsm.model").getAbsolutePath()))
                .name("measures")
                .build()).getResourceSet().getResources().get(0);
    }

}
