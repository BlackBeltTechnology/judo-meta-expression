package hu.blackbelt.judo.meta.expression.adapters.asm;

import hu.blackbelt.judo.meta.asm.runtime.AsmModel;
import hu.blackbelt.judo.meta.asm.runtime.AsmModelLoader;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.measure.Measure;
import hu.blackbelt.judo.meta.measure.Unit;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModelLoader;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;

public class AsmModelAdapterTest {

    private MeasureModel measureModel;
    private AsmModel asmModel;

    private ModelAdapter<EClassifier, EDataType, EAttribute, EEnum, EClass, EReference, Measure, Unit> modelAdapter;

    @BeforeEach
    public void setUp() throws Exception {
        measureModel = MeasureModelLoader.loadMeasureModel(
                URI.createURI(new File(srcDir(), "test/models/measure.model").getAbsolutePath()),
                "test",
                "1.0.0");
        asmModel = AsmModelLoader.loadAsmModel(
                AsmModelLoader.createAsmResourceSet(),
                URI.createURI(new File(srcDir(), "test/models/asm.model").getAbsolutePath()),
                "test",
                "1.0.0");
        modelAdapter = new AsmModelAdapter(asmModel.getResourceSet(), measureModel.getResourceSet());
    }

    @AfterEach
    public void tearDown() {
        modelAdapter = null;
    }

    private File srcDir() {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath + "../../src");
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }
}
