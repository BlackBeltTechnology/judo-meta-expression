package hu.blackbelt.judo.meta.expression.adapters.asm;

import static hu.blackbelt.judo.meta.measure.runtime.MeasureModel.buildMeasureModel;
import static hu.blackbelt.judo.meta.measure.util.builder.MeasureBuilders.newBaseMeasureBuilder;
import static hu.blackbelt.judo.meta.measure.util.builder.MeasureBuilders.newBaseMeasureTermBuilder;
import static hu.blackbelt.judo.meta.measure.util.builder.MeasureBuilders.newDerivedMeasureBuilder;
import static hu.blackbelt.judo.meta.measure.util.builder.MeasureBuilders.newDurationUnitBuilder;
import static hu.blackbelt.judo.meta.measure.util.builder.MeasureBuilders.newUnitBuilder;
import static org.eclipse.emf.ecore.util.builder.EcoreBuilders.newEAttributeBuilder;
import static org.eclipse.emf.ecore.util.builder.EcoreBuilders.newEClassBuilder;
import static org.eclipse.emf.ecore.util.builder.EcoreBuilders.newEDataTypeBuilder;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.asm.support.AsmModelResourceSupport;
import hu.blackbelt.judo.meta.measure.BaseMeasure;
import hu.blackbelt.judo.meta.measure.DerivedMeasure;
import hu.blackbelt.judo.meta.measure.DurationType;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;

public class AsmMeasureSupportTest {

    private MeasureModel measureModel;
    private ResourceSet asmResourceSet;
    private AsmModelAdapter modelAdapter;

    private EClass product;
    private EDataType doubleType;

    private AsmUtils asmUtils;

    @BeforeEach
    public void setUp() throws Exception {

    	measureModel = buildMeasureModel()
                .name("demo")
                .build();
    	populateMeasureModel();

        asmResourceSet = AsmModelResourceSupport.createAsmResourceSet();
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

        asmUtils.addExtensionAnnotationDetails(product.getEStructuralFeature("unitPrice"), "constraints", ImmutableMap.of("currency", "EUR"));
        asmUtils.addExtensionAnnotationDetails(product.getEStructuralFeature("weight"), "constraints", ImmutableMap.of("unit", "kg"));
        asmUtils.addExtensionAnnotationDetails(product.getEStructuralFeature("height"), "constraints", ImmutableMap.of("unit", "cm", "measure", "demo.measures.Length"));

        asmResource.getContents().addAll(Arrays.asList(stringType, doubleType, product));

        modelAdapter = new AsmModelAdapter(asmResourceSet, measureModel.getResourceSet());
    }
    
    private void populateMeasureModel() {  	  
    	  
        BaseMeasure time = newBaseMeasureBuilder().withName("Time").withNamespace("demo::measures").withUnits(
        		newDurationUnitBuilder().withName("nanosecond").withSymbol("ns").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0E+9)).withType(DurationType.NANOSECOND).build(),
        		newDurationUnitBuilder().withName("microsecond").withSymbol("μs").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1000000.0)).withType(DurationType.MICROSECOND).build(),
    			newDurationUnitBuilder().withName("millisecond").withSymbol("ms").withRateDividend(new BigDecimal(0.001)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.MILLISECOND).build(),
    			newDurationUnitBuilder().withName("second").withSymbol("s").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.SECOND).build(),
    			newDurationUnitBuilder().withName("minute").withSymbol("min").withRateDividend(new BigDecimal(60.0)).withRateDivisor(new BigDecimal(1.0)).build(),
    			newDurationUnitBuilder().withName("hour").withSymbol("h").withRateDividend(new BigDecimal(3600.0)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.HOUR).build(),
    			newDurationUnitBuilder().withName("day").withSymbol("").withRateDividend(new BigDecimal(86400.0)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.DAY).build(),
    			newDurationUnitBuilder().withName("week").withSymbol("").withRateDividend(new BigDecimal(604800.0)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.WEEK).build(),
    			newDurationUnitBuilder().withName("halfDay").withSymbol("").withRateDividend(new BigDecimal(43200.0)).withRateDivisor(new BigDecimal(1.0)).build())
        	.build();
        
        BaseMeasure monthBasedTime = newBaseMeasureBuilder().withName("MonthBasedTime").withNamespace("demo::measures").withUnits(
    			newDurationUnitBuilder().withName("month").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.MONTH).build(),
    			newDurationUnitBuilder().withName("year").withRateDividend(new BigDecimal(12.0)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.YEAR).build())
        	.build();
        
        BaseMeasure mass = newBaseMeasureBuilder().withName("Mass").withNamespace("demo::measures").withUnits(
        		newUnitBuilder().withName("milligram").withSymbol("mg").withRateDividend(new BigDecimal(0.0000010)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("gram").withSymbol("g").withRateDividend(new BigDecimal(0.001)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("dekagram").withSymbol("dkg").withRateDividend(new BigDecimal(0.01)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("kilogram").withSymbol("kg").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("quintal").withSymbol("q").withRateDividend(new BigDecimal(100.0)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("tonne").withSymbol("t").withRateDividend(new BigDecimal(1000.0)).withRateDivisor(new BigDecimal(1.0)).build())
    		.build();
        
        BaseMeasure length = newBaseMeasureBuilder().withName("Length").withNamespace("demo::measures").withUnits(
        		newUnitBuilder().withName("nanometre").withSymbol("nm").withRateDividend(new BigDecimal(1.0E-9)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("micrometre").withSymbol("μm").withRateDividend(new BigDecimal(0.0000010)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("millimetre").withSymbol("mm").withRateDividend(new BigDecimal(0.001)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("centimetre").withSymbol("cm").withRateDividend(new BigDecimal(0.01)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("decimetre").withSymbol("dm").withRateDividend(new BigDecimal(0.1)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("metre").withSymbol("m").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("kilometre").withSymbol("km").withRateDividend(new BigDecimal(1000.0)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("inch").withSymbol("&quot;").withRateDividend(new BigDecimal(0.0254)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("foot").withSymbol("ft").withRateDividend(new BigDecimal(0.3048)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("mile").withSymbol("mi").withRateDividend(new BigDecimal(1609.344)).withRateDivisor(new BigDecimal(1.0)).build())
    		.build();
        
        DerivedMeasure velocity = newDerivedMeasureBuilder().withName("Velocity").withNamespace("demo::measures").withUnits(
        		newUnitBuilder().withName("kilometrePerHour").withSymbol("km/h").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(3.6)).build(),
        		newUnitBuilder().withName("metrePerSecond").withSymbol("m/s").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0)).build())
        		.withTerms(newBaseMeasureTermBuilder().withExponent(1).withBaseMeasure(length).build())
        		.withTerms(newBaseMeasureTermBuilder().withExponent(-1).withBaseMeasure(time).build())
    		.build();
        
    	DerivedMeasure area = newDerivedMeasureBuilder().withName("Area").withNamespace("demo::measures").withUnits(
      			newUnitBuilder().withName("squareMillimetre").withSymbol("mm²").withRateDividend(new BigDecimal(0.0000010)).withRateDivisor(new BigDecimal(1.0)).build(),
      			newUnitBuilder().withName("squareCentimetre").withSymbol("cm²").withRateDividend(new BigDecimal(0.00010)).withRateDivisor(new BigDecimal(1.0)).build(),
      			newUnitBuilder().withName("squareDecimetre").withSymbol("dm²").withRateDividend(new BigDecimal(0.01)).withRateDivisor(new BigDecimal(1.0)).build(),
      			newUnitBuilder().withName("squareMetre").withSymbol("m²").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0)).build(),
      			newUnitBuilder().withName("hectare").withSymbol("ha").withRateDividend(new BigDecimal(10000.0)).withRateDivisor(new BigDecimal(1.0)).build(),
      			newUnitBuilder().withName("squareKilometre").withSymbol("km²").withRateDividend(new BigDecimal(1000000.0)).withRateDivisor(new BigDecimal(1.0)).build())
    			.withTerms(newBaseMeasureTermBuilder().withExponent(2).withBaseMeasure(length).build())
    	.build();
    	
        DerivedMeasure force = newDerivedMeasureBuilder().withName("Force").withNamespace("demo::measures").withUnits(
        		newUnitBuilder().withName("newton").withSymbol("N").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0)).build())
        		.withTerms(newBaseMeasureTermBuilder().withExponent(-2).withBaseMeasure(time).build())
        		.withTerms(newBaseMeasureTermBuilder().withExponent(1).withBaseMeasure(mass).build())
        		.withTerms(newBaseMeasureTermBuilder().withExponent(1).withBaseMeasure(length).build())
    		.build();
        
        measureModel.addContent(time);
        measureModel.addContent(mass);
        measureModel.addContent(length);
        measureModel.addContent(velocity);
        measureModel.addContent(area);
        measureModel.addContent(force);
        measureModel.addContent(monthBasedTime);
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

        asmUtils.addExtensionAnnotationDetails(product.getEStructuralFeature("vat"), "constraints", ImmutableMap.of("unit", "EUR"));
        asmUtils.addExtensionAnnotationDetails(product.getEStructuralFeature("netWeight"), "constraints", ImmutableMap.of("unit", "kg", "measure", "demo::measures.Length"));
        asmUtils.addExtensionAnnotationDetails(product.getEStructuralFeature("grossWeight"), "constraints", ImmutableMap.of("unit", "kg", "measure", "Length"));
        asmUtils.addExtensionAnnotationDetails(product.getEStructuralFeature("width"), "constraints", ImmutableMap.of("unit", "m", "measure", "measures::Length"));

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
