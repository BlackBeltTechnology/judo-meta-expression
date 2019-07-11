package hu.blackbelt.judo.meta.expression.adapters.asm;

import com.google.common.collect.ImmutableList;
import hu.blackbelt.judo.meta.asm.runtime.AsmModelLoader;
import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModelLoader;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;

import static org.eclipse.emf.ecore.util.builder.EcoreBuilders.*;

public class AsmMeasureSupportTest {

    private MeasureModel measureModel;
    private ResourceSet asmResourceSet;
    private AsmModelAdapter modelAdapter;

    private EClass product;
    private EDataType doubleType;

    private AsmUtils asmUtils;

    @BeforeEach
    public void setUp() throws Exception {
        measureModel = MeasureModelLoader.loadMeasureModel(
                URI.createURI(new File(srcDir(), "test/models/measure.model").getAbsolutePath()),
                "test",
                "1.0.0");

        asmResourceSet = AsmModelLoader.createAsmResourceSet();
        final Resource asmResource = asmResourceSet.createResource(URI.createURI("urn:asm.judo-meta-asm"));

        final EDataType stringType = newEDataTypeBuilder()
                .withName("String")
                .withInstanceTypeName("java.lang.String")
                .build();

        doubleType = newEDataTypeBuilder()
                .withName("Integer")
                .withInstanceTypeName("java.lang.Double")
                .build();

        product = newEClassBuilder()
                .withName("Product")
                .withEStructuralFeatures(ImmutableList.of(
                        newEAttributeBuilder().withName("discount").withEType(doubleType).build(),
                        newEAttributeBuilder().withName("url").withEType(stringType).build(),
                        newEAttributeBuilder().withName("unitPrice").withEType(doubleType).build(),
                        newEAttributeBuilder().withName("weight").withEType(doubleType).build(),
                        newEAttributeBuilder().withName("height").withEType(doubleType).build()
                )).build();

        asmUtils = new AsmUtils(asmResourceSet);

        asmUtils.getExtensionAnnotation(product.getEStructuralFeature("unitPrice"), true).get().getDetails().put("currency", "EUR");
        asmUtils.getExtensionAnnotation(product.getEStructuralFeature("weight"), true).get().getDetails().put("unit", "kg");
        asmUtils.getExtensionAnnotation(product.getEStructuralFeature("height"), true).get().getDetails().put("unit", "cm");
        asmUtils.getExtensionAnnotation(product.getEStructuralFeature("height"), true).get().getDetails().put("measure", "northwind.measures.Length");

        asmResource.getContents().addAll(Arrays.asList(stringType, doubleType, product));

        modelAdapter = new AsmModelAdapter(asmResourceSet, measureModel.getResourceSet());
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

    @Test
    public void testGetUnitOfAttributeWithUnknownUnit() {
        product.getEStructuralFeatures().addAll(ImmutableList.of(
                newEAttributeBuilder().withName("vat").withEType(doubleType).build(),
                newEAttributeBuilder().withName("netWeight").withEType(doubleType).build(),
                newEAttributeBuilder().withName("grossWeight").withEType(doubleType).build(),
                newEAttributeBuilder().withName("width").withEType(doubleType).build()
        ));

        asmUtils.getExtensionAnnotation(product.getEStructuralFeature("vat"), true).get().getDetails().put("unit", "EUR");
        asmUtils.getExtensionAnnotation(product.getEStructuralFeature("netWeight"), true).get().getDetails().put("unit", "kg");
        asmUtils.getExtensionAnnotation(product.getEStructuralFeature("netWeight"), true).get().getDetails().put("measure", "northwind.measures.Length");
        asmUtils.getExtensionAnnotation(product.getEStructuralFeature("grossWeight"), true).get().getDetails().put("unit", "kg");
        asmUtils.getExtensionAnnotation(product.getEStructuralFeature("grossWeight"), true).get().getDetails().put("measure", "Length");
        asmUtils.getExtensionAnnotation(product.getEStructuralFeature("width"), true).get().getDetails().put("unit", "m");
        asmUtils.getExtensionAnnotation(product.getEStructuralFeature("width"), true).get().getDetails().put("measure", "measures.Length");

        Assert.assertFalse(modelAdapter.getUnit(product, "vat").isPresent());          // EUR is not defined as unit
        Assert.assertFalse(modelAdapter.getUnit(product, "netWeight").isPresent());    // unit belongs to another measure
        Assert.assertFalse(modelAdapter.getUnit(product, "grossWeight").isPresent());  // measure name is not matching expected pattern
        Assert.assertFalse(modelAdapter.getUnit(product, "width").isPresent());        // measure name is invalid
    }

    @Test
    public void testGetUnitOfNonNumericAttribute() {
        Assert.assertFalse(modelAdapter.getUnit(product, "url").isPresent());          // attribute is not numeric
    }

    @Test
    public void getGetUnitOfNonExistingAttribute() {
        Assert.assertFalse(modelAdapter.getUnit(product, "width").isPresent());        // attribute is not defined
        Assert.assertFalse(modelAdapter.getUnit(product, "unitPrice").isPresent());    // annotation is added without 'unit' key
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
