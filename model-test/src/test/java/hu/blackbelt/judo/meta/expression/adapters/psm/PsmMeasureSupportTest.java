package hu.blackbelt.judo.meta.expression.adapters.psm;

import com.google.common.collect.ImmutableList;
import hu.blackbelt.judo.meta.psm.PsmUtils;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.measure.Unit;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.psm.support.PsmModelResourceSupport;
import hu.blackbelt.judo.meta.psm.type.Primitive;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.psm.data.util.builder.DataBuilders.newAttributeBuilder;
import static hu.blackbelt.judo.meta.psm.data.util.builder.DataBuilders.newEntityTypeBuilder;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newMeasuredTypeBuilder;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.LoadArguments.psmLoadArgumentsBuilder;
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

    private PsmUtils psmUtils;

    @BeforeEach
    public void setUp() throws Exception {
        measureModel = PsmModel.loadPsmModel(psmLoadArgumentsBuilder()
                .uri(URI.createFileURI(new File("src/test/model/psm.model").getAbsolutePath()))
                .name("test")
                // TODO: check model
                .validateModel(false)
                .build());


        psmResourceSet = PsmModelResourceSupport.createPsmResourceSet();
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

        psmUtils = new PsmUtils();

        //TODO


        psmResource.getContents().addAll(Arrays.asList(stringType, doubleType, product));

        modelAdapter = new PsmModelAdapter(psmResourceSet, measureModel.getResourceSet());
    }

    @AfterEach
    public void tearDown() {
        measureModel = null;
        product = null;
        modelAdapter = null;
    }

    @Test
    public void testGetUnitOfNonMeasuredAttribute() {
        Assert.assertFalse(modelAdapter.getUnit(product, "discount").isPresent());
    }

    @Test
    public void testGetUnitOfMeasuredAttribute() {
        Assert.assertTrue(modelAdapter.getUnit(product, "weight").isPresent());
        Assert.assertTrue(modelAdapter.getUnit(product, "height").isPresent());
    }

    //@Test
    public void testGetUnitOfAttributeWithUnknownUnit() {
        product.getAttributes().addAll(ImmutableList.of(
           newAttributeBuilder().withName("vat").withDataType(doubleType).build(),
           newAttributeBuilder().withName("netWeight").withDataType(doubleType).build(),
           newAttributeBuilder().withName("grossWeight").withDataType(doubleType).build(),
           newAttributeBuilder().withName("width").withDataType(doubleType).build()
        ));

        //TODO

        Assert.assertFalse(modelAdapter.getUnit(product, "vat").isPresent());           // EUR not defined as unit
        Assert.assertFalse(modelAdapter.getUnit(product, "netWeight").isPresent());     // unit belongs to another measure
        Assert.assertFalse(modelAdapter.getUnit(product, "grossWeight").isPresent());   // measure name not matching expected pattern
        Assert.assertFalse(modelAdapter.getUnit(product, "width").isPresent());         // measure name invalid
    }

    @Test
    public void testGetUnitOfNonNumericAttribute() {
        Assert.assertFalse(modelAdapter.getUnit(product, "url").isPresent());           //NOT NUMERIC >:[
    }

    @Test
    public void testGetUnitOfNonExistingAttribute() {
        Assert.assertFalse(modelAdapter.getUnit(product, "width").isPresent());         // attribute is not defined
        Assert.assertFalse(modelAdapter.getUnit(product, "unitPrice").isPresent());     // annotation is added without 'unit' key
    }

    <T> Stream<T> getMeasureElement(final Class<T> clazz) {
        final Iterable<Notifier> measureContents = measureModel.getResourceSet()::getAllContents;
        return StreamSupport.stream(measureContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }
}
