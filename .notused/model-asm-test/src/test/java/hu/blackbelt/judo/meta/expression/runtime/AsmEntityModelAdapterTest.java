package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.asm.runtime.AsmModel;
import hu.blackbelt.judo.meta.asm.runtime.AsmModelLoader;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.asm.AsmModelAdapter;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModelLoader;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.File;
import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.LoadArguments.loadArgumentsBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class AsmEntityModelAdapterTest {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AsmEntityModelAdapterTest.class);
    private ExpressionModel expressionModel;
    private AsmModel asmModel;
    private MeasureModel measureModel;
    private AsmModelAdapter modelAdapter;

    @BeforeEach
    public void setUp() throws Exception {

        expressionModel = ExpressionModel.loadExpressionModel(loadArgumentsBuilder()
                .uri(URI.createFileURI(new File("test/models/t001.model").getAbsolutePath()))
                .name("test")
                .build());

        asmModel = AsmModelLoader.loadAsmModel(AsmModelLoader.createAsmResourceSet(),
                URI.createURI(new File(srcDir(), "test/model/northwind-asm.model").getAbsolutePath()),
                "test",
                "1.0.0");

        measureModel = MeasureModelLoader.loadMeasureModel(MeasureModelLoader.createMeasureResourceSet(),
                URI.createURI(new File(srcDir(), "test/model/measure.model").getAbsolutePath()),
                "test",
                "1.0.0");

        modelAdapter = new AsmModelAdapter(asmModel.getResourceSet(), measureModel.getResourceSet());
    }

    @AfterEach
    public void tearDown() {
        modelAdapter = null;
        expressionModel = null;
        asmModel = null;
    }

    @Test
    void testGet() {
        final TypeName typeName = newTypeNameBuilder()
                .withNamespace("demo::entities")
                .withName("Order")
                .build();

        final Optional<? extends EClassifier> namespaceElement = modelAdapter.get(typeName);

        assertThat(namespaceElement.isPresent(), is(Boolean.TRUE));
        assertThat(namespaceElement.get(), instanceOf(EClass.class));
        assertThat(namespaceElement.get().getName(), is("Order"));
    }

    public File srcDir() {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath + "../../src");
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }
}
