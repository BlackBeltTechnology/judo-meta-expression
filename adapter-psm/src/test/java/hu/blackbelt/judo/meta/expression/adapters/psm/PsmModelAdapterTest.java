package hu.blackbelt.judo.meta.expression.adapters.psm;

import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.TypeName;
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
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newMeasureNameBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;
import static hu.blackbelt.judo.meta.psm.data.util.builder.DataBuilders.newEntityTypeBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;

@Slf4j
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

    //TODO: fix whole
    @Test
    public void testGetTypeName() {
        log.info("Testing: getTypeName(Namespace)...");

        Optional<TypeName> typeName = modelAdapter.getTypeName(getEntityTypeByName("Category").get());

        Assert.assertTrue(typeName.isPresent());
        Assert.assertThat(typeName.get().getName(), is("Category"));
        Assert.assertThat(typeName.get().getNamespace(), is("northwind::entities"));
    }

    @Test
    void testGetNamespaceElementByTypeName() {
        log.info("Testing: get(Typename): NamespaceElemenet...");
        final TypeName typeName = newTypeNameBuilder()
                .withNamespace("northwind::entities")
                .withName("Order")
                .build();

        //TODO: neg testcase (NSE does not exist by TN)
        final Optional<? extends NamespaceElement> namespaceElement = modelAdapter.get(typeName);

        Assert.assertThat(namespaceElement.isPresent(), is(Boolean.TRUE));
        Assert.assertThat(namespaceElement.get(), instanceOf(EntityType.class));
        Assert.assertThat(namespaceElement.get().getName(), is("Order"));
        //TODO: check namespace as well
    }

    @Test
    void testGetMeasureByMeasureName() {
        log.info("Testing: get(MeasureName)...");
        final MeasureName measureName = newMeasureNameBuilder()
                .withNamespace("northwind::measures")
                .withName("Mass")
                .build();

        final Optional<? extends Measure> measure = modelAdapter.get(measureName);

        Assert.assertThat(measure.isPresent(), is(Boolean.TRUE));
        Assert.assertThat(measure.get(), instanceOf(Measure.class));
        Assert.assertThat(measure.get().getName(), is("Mass"));
    }

    @Test
    void testIsObjectType() {
        log.info("Testing: boolean isObjectType(NamespaceElement)...");

        EntityType entityType = newEntityTypeBuilder().withName("EntityType").build();
        //TODO: negative test case

        Assert.assertTrue(modelAdapter.isObjectType(entityType));

    }

    private Optional<EntityType> getEntityTypeByName(final String entityTypeName) {
        final Iterable<Notifier> psmContents = psmModel.getResourceSet()::getAllContents;
        return StreamSupport.stream(psmContents.spliterator(), true)
                .filter(e -> EntityType.class.isAssignableFrom(e.getClass())).map(e -> (EntityType) e)
                .filter(entityType -> Objects.equals(entityType.getName(), entityTypeName))
                .findAny();
    }
}
