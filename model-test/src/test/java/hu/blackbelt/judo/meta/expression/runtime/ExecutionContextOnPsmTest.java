package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.psm.PsmModelAdapter;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;

import java.io.File;

import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.LoadArguments.psmLoadArgumentsBuilder;

abstract class ExecutionContextOnPsmTest {

    final Log log = new Slf4jLog();

    ModelAdapter modelAdapter;

    PsmModel psmModel;
    ExpressionModel expressionModel;

    void setUp() throws Exception {
        psmModel = getPsmModel();
        expressionModel = getExpressionModel();

        modelAdapter = new PsmModelAdapter(psmModel.getResourceSet(), psmModel.getResourceSet());
    }

    protected PsmModel getPsmModel() throws Exception {
        return PsmModel.loadPsmModel(psmLoadArgumentsBuilder()
                .name("data")
                .file(new File("target/test-classes/model/northwind-psm.model"))
                .build());
    }

    protected abstract ExpressionModel getExpressionModel() throws Exception;

}
