package hu.blackbelt.judo.meta.expression.builder.jql.asm;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.asm.runtime.AsmModel;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.model.northwind.Demo;

import static hu.blackbelt.judo.meta.asm.runtime.AsmModel.buildAsmModel;
import static hu.blackbelt.judo.meta.measure.runtime.MeasureModel.buildMeasureModel;
import static hu.blackbelt.judo.tatami.psm2asm.Psm2Asm.calculatePsm2AsmTransformationScriptURI;
import static hu.blackbelt.judo.tatami.psm2asm.Psm2Asm.executePsm2AsmTransformation;
import static hu.blackbelt.judo.tatami.psm2measure.Psm2Measure.calculatePsm2MeasureTransformationScriptURI;
import static hu.blackbelt.judo.tatami.psm2measure.Psm2Measure.executePsm2MeasureTransformation;

public class ModelLoader {

    private static final Log slf4jLog = new Slf4jLog();

    private AsmModel asmModel;
    private MeasureModel measureModel;

    public ModelLoader() {
        try {
            final PsmModel psmModel = new Demo().fullDemo();
            asmModel = buildAsmModel()
                    .name(psmModel.getName())
                    .build();
            executePsm2AsmTransformation(psmModel, asmModel, slf4jLog, calculatePsm2AsmTransformationScriptURI());
            measureModel = buildMeasureModel()
                    .name(psmModel.getName())
                    .build();
            executePsm2MeasureTransformation(psmModel, measureModel, slf4jLog, calculatePsm2MeasureTransformationScriptURI());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AsmModel getAsmModel() {
        return asmModel;
    }

    public MeasureModel getMeasureModel() {
        return measureModel;
    }
}
