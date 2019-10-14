package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.api.ModelContext;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.psm.PsmModelAdapter;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.LoadArguments.psmLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.loadPsmModel;

abstract class ExecutionContextOnPsmTest {
	
    final Log log = new Slf4jLog();

    ModelAdapter modelAdapter;
    
    List<ModelContext> modelContexts = new ArrayList<>();

    void setUp() throws Exception {
        
    	final Resource psm = getMeasureResource();
    	final Resource measures = psm;
    	
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

    protected Resource getMeasureResource() throws IOException, PsmModel.PsmValidationException {
        return loadPsmModel(psmLoadArgumentsBuilder()
                .uri(URI.createFileURI(new File("target/test-classes/model/northwind-psm.model").getAbsolutePath()))
                .name("measures"))
                .getResourceSet().getResources().get(0);
    }
    
    protected abstract Resource getExpressionResource() throws Exception;
    
}
