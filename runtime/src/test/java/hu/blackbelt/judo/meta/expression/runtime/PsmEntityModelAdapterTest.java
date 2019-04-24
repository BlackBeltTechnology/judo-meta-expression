package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.runtime.adapters.PsmEntityModelAdapter;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModelLoader;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
public class PsmEntityModelAdapterTest {

    private ExpressionModel expressionModel;
    private PsmModel psmModel;
    private PsmEntityModelAdapter modelAdapter;

    @BeforeEach
    public void setUp() throws IOException {
        expressionModel = ExpressionModelLoader.loadExpressionModel(
                URI.createURI(new File(srcDir(), "test/models/t001.model").getAbsolutePath()),
                "test",
                "1.0.0");

        psmModel = PsmModelLoader.loadPsmModel(
                URI.createURI(new File(srcDir(), "test/models/northwind-judopsm.model").getAbsolutePath()),
                "test",
                "1.0.0");

        modelAdapter = new PsmEntityModelAdapter(psmModel.getResourceSet());
    }

    @AfterEach
    public void tearDown() {
        modelAdapter = null;
        expressionModel = null;
        psmModel = null;
    }

    @Test
    void testGet() {
        final TypeName typeName = newTypeNameBuilder()
                .withNamespace("northwind.entities")
                .withName("Order")
                .build();

        final Optional<? extends NamespaceElement> namespaceElement = modelAdapter.get(typeName);

        assertThat(namespaceElement.isPresent(), is(Boolean.TRUE));
        assertThat(namespaceElement.get(), instanceOf(EntityType.class));
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
