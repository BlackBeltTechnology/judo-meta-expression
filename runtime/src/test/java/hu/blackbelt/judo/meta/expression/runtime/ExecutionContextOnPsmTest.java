package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;
import hu.blackbelt.judo.meta.expression.runtime.adapters.PsmEntityModelAdapter;
import hu.blackbelt.judo.meta.psm.runtime.PsmModelLoader;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.io.File;

import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;

abstract class ExecutionContextOnPsmTest extends ExecutionContextTest {

    void setUp() throws Exception {
        final Resource psm = getPsmResource();
        modelAdapter = new PsmEntityModelAdapter(psm.getResourceSet());

        if (psm != null) {
            modelContexts.add(wrappedEmfModelContextBuilder()
                    .log(log)
                    .name("NORTHWIND")
                    .aliases(ImmutableList.of("JUDOPSM"))
                    .resource(psm)
                    .build());
        }

        modelContexts.add(wrappedEmfModelContextBuilder()
                .log(log)
                .name("TEST")
                .aliases(ImmutableList.of("EXPR"))
                .resource(getExpressionResource())
                .build());
    }

    protected Resource getPsmResource() throws Exception {
        final ResourceSet psmModelResourceSet = PsmModelLoader.createPsmResourceSet();
        PsmModelLoader.loadPsmModel(psmModelResourceSet,
                URI.createURI(new File(srcDir(), "test/models/northwind-judopsm.model").getAbsolutePath()),
                "test",
                "1.0.0");

        return psmModelResourceSet.getResources().get(0);
    }

    @Override
    protected String getEvlSource() {
        return "epsilon/validations/expressionOnPsm.evl";
    }

    @Override
    protected String getExpressionToEvaluationEtlSource() {
        return "epsilon/transformations/expression2evaluationOnPsm.etl";
    }
}
