package hu.blackbelt.judo.meta.expression.adapters.psm;

import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.psm.measure.Measure;
import hu.blackbelt.judo.meta.psm.measure.Unit;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModelLoader;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

public class PsmMeasureProviderTest {

    private PsmModel psmModel;
    private MeasureProvider<Measure, Unit> measureProvider;

    private MeasureAdapter<Measure, Unit, EClass> measureAdapter;

    @BeforeEach
    public void setUp() throws IOException {
        psmModel = PsmModelLoader.loadPsmModel(
                URI.createURI(new File(srcDir(), "test/models/psm.model").getAbsolutePath()),
                "test",
                "1.0.0");
        measureProvider = new PsmMeasureProvider(psmModel.getResourceSet());

        measureAdapter = new MeasureAdapter<>(measureProvider, Mockito.mock(ModelAdapter.class));
    }

    @AfterEach
    public void tearDown() {
        psmModel = null;
        measureProvider = null;
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
