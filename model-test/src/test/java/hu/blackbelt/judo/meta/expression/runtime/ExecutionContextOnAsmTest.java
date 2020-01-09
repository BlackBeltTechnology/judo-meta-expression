package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.asm.runtime.AsmModel;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.asm.AsmModelAdapter;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;

import java.io.File;
import java.io.IOException;

import static hu.blackbelt.judo.meta.asm.runtime.AsmModel.LoadArguments.asmLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.measure.runtime.MeasureModel.LoadArguments.measureLoadArgumentsBuilder;

abstract class ExecutionContextOnAsmTest {

	final Log log = new Slf4jLog();

    ModelAdapter modelAdapter;

    AsmModel asmModel;
    MeasureModel measureModel;
    ExpressionModel expressionModel;
    
    void setUp() throws Exception {
        asmModel = getAsmModel();
        measureModel = getMeasureModel();
        expressionModel = getExpressionModel();

        modelAdapter = new AsmModelAdapter(asmModel.getResourceSet(), measureModel.getResourceSet());
    }

    protected AsmModel getAsmModel() throws Exception {
        // Set our custom handler
        final File modelFile = new File("target/test-classes/model/northwind-asm.model");

        return AsmModel.loadAsmModel(asmLoadArgumentsBuilder()
                .name("data")
                .file(modelFile)
                .build());
    }

    protected MeasureModel getMeasureModel() throws IOException, MeasureModel.MeasureValidationException {

        final File modelFile = new File("target/test-classes/model/northwind-measure.model");

        return MeasureModel.loadMeasureModel(measureLoadArgumentsBuilder()
                .name("measures")
                .file(modelFile)
                .build());
    }
    
    protected abstract ExpressionModel getExpressionModel() throws Exception;

}
