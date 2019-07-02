package hu.blackbelt.judo.meta.expression.adapters.psm;

import com.google.common.collect.ImmutableList;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.measure.Unit;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModelLoader;
import hu.blackbelt.judo.meta.psm.type.Primitive;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.psm.data.util.builder.DataBuilders.newAttributeBuilder;
import static hu.blackbelt.judo.meta.psm.data.util.builder.DataBuilders.newEntityTypeBuilder;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newMeasuredTypeBuilder;
import static hu.blackbelt.judo.meta.psm.type.util.builder.TypeBuilders.newNumericTypeBuilder;
import static hu.blackbelt.judo.meta.psm.type.util.builder.TypeBuilders.newStringTypeBuilder;

public class PsmMeasureSupportTest {

    private PsmModel measureModel;
    private ResourceSet psmResourceSet;
    private PsmModelAdapter modelAdapter;

    private EntityType product;
    private Primitive doubleType;
    private Primitive kgType;
    private Primitive cmType;

    @BeforeEach
    public void setUp() throws Exception {
        measureModel = PsmModelLoader.loadPsmModel(
                URI.createURI(new File(srcDir(), "test/models/psm.model").getAbsolutePath()),
                "test",
                "1.0.0");

        psmResourceSet = PsmModelLoader.createPsmResourceSet();
        final Resource psmResource = psmResourceSet.createResource(URI.createURI("urn:psm.judo-meta-psm"));

        final Primitive stringType = newStringTypeBuilder()
                .withName("String")
                .build();

        doubleType = newNumericTypeBuilder()
                .withName("Integer")
                .withScale(7)
                .withPrecision(18)
                .build();

        kgType = newMeasuredTypeBuilder()
                .withName("kg")
                .withPrecision(7)
                .withScale(18)
                .withStoreUnit(getMeasureElement(Unit.class).filter(u -> "kg".equals(u.getSymbol())).findAny().get())
                .build();

        cmType = newMeasuredTypeBuilder()
                .withName("cm")
                .withPrecision(7)
                .withScale(18)
                .withStoreUnit(getMeasureElement(Unit.class).filter(u -> "cm".equals(u.getSymbol())).findAny().get())
                .build();

        product = newEntityTypeBuilder()
                .withName("Product")
                .withAttributes(ImmutableList.of(
                        newAttributeBuilder().withName("discount").withDataType(doubleType).build(),
                        newAttributeBuilder().withName("url").withDataType(stringType).build(),
                        newAttributeBuilder().withName("unitPrice").withDataType(doubleType).build(),
                        newAttributeBuilder().withName("weight").withDataType(kgType).build(),
                        newAttributeBuilder().withName("height").withDataType(cmType).build()
                )).build();

        psmResource.getContents().addAll(Arrays.asList(stringType, doubleType, product));

        modelAdapter = new PsmModelAdapter(psmResourceSet, measureModel.getResourceSet());
    }

    @AfterEach
    public void tearDown() {
        measureModel = null;
        product = null;
        modelAdapter = null;
    }

    <T> Stream<T> getMeasureElement(final Class<T> clazz) {
        final Iterable<Notifier> measureContents = measureModel.getResourceSet()::getAllContents;
        return StreamSupport.stream(measureContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
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
