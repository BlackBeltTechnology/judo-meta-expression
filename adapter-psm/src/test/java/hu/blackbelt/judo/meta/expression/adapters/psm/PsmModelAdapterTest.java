package hu.blackbelt.judo.meta.expression.adapters.psm;

import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.data.PrimitiveTypedElement;
import hu.blackbelt.judo.meta.psm.data.ReferenceTypedElement;
import hu.blackbelt.judo.meta.psm.measure.Measure;
import hu.blackbelt.judo.meta.psm.measure.Unit;
import hu.blackbelt.judo.meta.psm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModelLoader;
import hu.blackbelt.judo.meta.psm.type.EnumerationType;
import hu.blackbelt.judo.meta.psm.type.Primitive;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;

public class PsmModelAdapterTest {

    private PsmModel measureModel;
    private PsmModel psmModel;

    private ModelAdapter<NamespaceElement, Primitive, PrimitiveTypedElement, EnumerationType, EntityType, ReferenceTypedElement, Measure, Unit> modelAdapter;

    @BeforeEach
    public void setUp() throws Exception {
        psmModel = PsmModelLoader.loadPsmModel(
                URI.createURI(new File(srcDir(), "test/models/psm.model").getAbsolutePath()),
                "test",
                "1.0.0");
        measureModel = psmModel;

        modelAdapter = new PsmModelAdapter(psmModel.getResourceSet(), measureModel.getResourceSet());
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
