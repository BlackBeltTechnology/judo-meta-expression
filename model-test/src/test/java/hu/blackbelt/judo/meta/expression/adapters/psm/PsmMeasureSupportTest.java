package hu.blackbelt.judo.meta.expression.adapters.psm;

import static hu.blackbelt.judo.meta.psm.data.util.builder.DataBuilders.newAttributeBuilder;
import static hu.blackbelt.judo.meta.psm.data.util.builder.DataBuilders.newEntityTypeBuilder;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newDerivedMeasureBuilder;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newDurationUnitBuilder;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newMeasureBuilder;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newMeasureDefinitionTermBuilder;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newMeasuredTypeBuilder;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newUnitBuilder;
import static hu.blackbelt.judo.meta.psm.namespace.util.builder.NamespaceBuilders.newModelBuilder;
import static hu.blackbelt.judo.meta.psm.namespace.util.builder.NamespaceBuilders.newPackageBuilder;
import static hu.blackbelt.judo.meta.psm.namespace.util.builder.NamespaceBuilders.useModel;
import static hu.blackbelt.judo.meta.psm.namespace.util.builder.NamespaceBuilders.usePackage;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.buildPsmModel;
import static hu.blackbelt.judo.meta.psm.type.util.builder.TypeBuilders.newNumericTypeBuilder;
import static hu.blackbelt.judo.meta.psm.type.util.builder.TypeBuilders.newStringTypeBuilder;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.measure.DerivedMeasure;
import hu.blackbelt.judo.meta.psm.measure.DurationType;
import hu.blackbelt.judo.meta.psm.measure.Measure;
import hu.blackbelt.judo.meta.psm.measure.Unit;
import hu.blackbelt.judo.meta.psm.namespace.Model;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.psm.type.Primitive;

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
        measureModel = buildPsmModel().name("test").build();
        populateMeasureModel();

        PsmModel psmModel = buildPsmModel().name("test").build();
        
        Model model = newModelBuilder().withName("demo").build();
        
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
        
        useModel(model).withElements(stringType,doubleType,kgType,cmType,product).build();
        psmModel.addContent(model);
        
        psmResourceSet = psmModel.getResourceSet();
        final Resource psmResource = psmResourceSet.createResource(URI.createURI("urn:psm.judo-meta-psm"));

        modelAdapter = new PsmModelAdapter(psmResourceSet, measureModel.getResourceSet());
    }
    
    private void populateMeasureModel() {  	  
    	  
    	Model demo = newModelBuilder().withName("demo").build();
    	Package measures = newPackageBuilder().withName("measures").build();
    	useModel(demo).withPackages(measures).build();
    	
        Measure time = newMeasureBuilder().withName("Time").withUnits(
        		newDurationUnitBuilder().withName("nanosecond").withSymbol("ns").withRateDividend(new Double(1.0)).withRateDivisor(new Double(1.0E+9)).withUnitType(DurationType.NANOSECOND).build(),
        		newDurationUnitBuilder().withName("microsecond").withSymbol("μs").withRateDividend(new Double(1.0)).withRateDivisor(new Double(1000000.0)).withUnitType(DurationType.MICROSECOND).build(),
    			newDurationUnitBuilder().withName("millisecond").withSymbol("ms").withRateDividend(new Double(0.001)).withRateDivisor(new Double(1.0)).withUnitType(DurationType.MILLISECOND).build(),
    			newDurationUnitBuilder().withName("second").withSymbol("s").withRateDividend(new Double(1.0)).withRateDivisor(new Double(1.0)).withUnitType(DurationType.SECOND).build(),
    			newDurationUnitBuilder().withName("minute").withSymbol("min").withRateDividend(new Double(60.0)).withRateDivisor(new Double(1.0)).build(),
    			newDurationUnitBuilder().withName("hour").withSymbol("h").withRateDividend(new Double(3600.0)).withRateDivisor(new Double(1.0)).withUnitType(DurationType.HOUR).build(),
    			newDurationUnitBuilder().withName("day").withSymbol("").withRateDividend(new Double(86400.0)).withRateDivisor(new Double(1.0)).withUnitType(DurationType.DAY).build(),
    			newDurationUnitBuilder().withName("week").withSymbol("").withRateDividend(new Double(604800.0)).withRateDivisor(new Double(1.0)).withUnitType(DurationType.WEEK).build(),
    			newDurationUnitBuilder().withName("halfDay").withSymbol("").withRateDividend(new Double(43200.0)).withRateDivisor(new Double(1.0)).build())
        	.build();
        
        Measure monthBasedTime = newMeasureBuilder().withName("MonthBasedTime").withUnits(
    			newDurationUnitBuilder().withName("month").withRateDividend(new Double(1.0)).withRateDivisor(new Double(1.0)).withUnitType(DurationType.MONTH).build(),
    			newDurationUnitBuilder().withName("year").withRateDividend(new Double(12.0)).withRateDivisor(new Double(1.0)).withUnitType(DurationType.YEAR).build())
        	.build();
        
        Measure mass = newMeasureBuilder().withName("Mass").withUnits(
        		newUnitBuilder().withName("milligram").withSymbol("mg").withRateDividend(new Double(0.0000010)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("gram").withSymbol("g").withRateDividend(new Double(0.001)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("dekagram").withSymbol("dkg").withRateDividend(new Double(0.01)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("kilogram").withSymbol("kg").withRateDividend(new Double(1.0)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("quintal").withSymbol("q").withRateDividend(new Double(100.0)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("tonne").withSymbol("t").withRateDividend(new Double(1000.0)).withRateDivisor(new Double(1.0)).build())
    		.build();
        
        Measure length = newMeasureBuilder().withName("Length").withUnits(
        		newUnitBuilder().withName("nanometre").withSymbol("nm").withRateDividend(new Double(1.0E-9)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("micrometre").withSymbol("μm").withRateDividend(new Double(0.0000010)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("millimetre").withSymbol("mm").withRateDividend(new Double(0.001)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("centimetre").withSymbol("cm").withRateDividend(new Double(0.01)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("decimetre").withSymbol("dm").withRateDividend(new Double(0.1)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("metre").withSymbol("m").withRateDividend(new Double(1.0)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("kilometre").withSymbol("km").withRateDividend(new Double(1000.0)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("inch").withSymbol("&quot;").withRateDividend(new Double(0.0254)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("foot").withSymbol("ft").withRateDividend(new Double(0.3048)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("mile").withSymbol("mi").withRateDividend(new Double(1609.344)).withRateDivisor(new Double(1.0)).build())
    		.build();
        
        DerivedMeasure velocity = newDerivedMeasureBuilder().withName("Velocity").withUnits(
        		newUnitBuilder().withName("kilometrePerHour").withSymbol("km/h").withRateDividend(new Double(1.0)).withRateDivisor(new Double(3.6)).build(),
        		newUnitBuilder().withName("metrePerSecond").withSymbol("m/s").withRateDividend(new Double(1.0)).withRateDivisor(new Double(1.0)).build())
        		.withTerms(newMeasureDefinitionTermBuilder().withExponent(1).withUnit(length.getUnits().get(6)).build())
        		.withTerms(newMeasureDefinitionTermBuilder().withExponent(-1).withUnit(time.getUnits().get(5)).build())
    		.build();
        
    	DerivedMeasure area = newDerivedMeasureBuilder().withName("Area").withUnits(
      			newUnitBuilder().withName("squareMillimetre").withSymbol("mm²").withRateDividend(new Double(0.0000010)).withRateDivisor(new Double(1.0)).build(),
      			newUnitBuilder().withName("squareCentimetre").withSymbol("cm²").withRateDividend(new Double(0.00010)).withRateDivisor(new Double(1.0)).build(),
      			newUnitBuilder().withName("squareDecimetre").withSymbol("dm²").withRateDividend(new Double(0.01)).withRateDivisor(new Double(1.0)).build(),
      			newUnitBuilder().withName("squareMetre").withSymbol("m²").withRateDividend(new Double(1.0)).withRateDivisor(new Double(1.0)).build(),
      			newUnitBuilder().withName("hectare").withSymbol("ha").withRateDividend(new Double(10000.0)).withRateDivisor(new Double(1.0)).build(),
      			newUnitBuilder().withName("squareKilometre").withSymbol("km²").withRateDividend(new Double(1000000.0)).withRateDivisor(new Double(1.0)).build())
    			.withTerms(newMeasureDefinitionTermBuilder().withExponent(2).withUnit(length.getUnits().get(0)).build())
    	.build();
    	
        DerivedMeasure force = newDerivedMeasureBuilder().withName("Force").withUnits(
        		newUnitBuilder().withName("newton").withSymbol("N").withRateDividend(new Double(1.0)).withRateDivisor(new Double(1.0)).build())
        		.withTerms(newMeasureDefinitionTermBuilder().withExponent(-2).withUnit(time.getUnits().get(0)).build())
        		.withTerms(newMeasureDefinitionTermBuilder().withExponent(1).withUnit(mass.getUnits().get(0)).build())
        		.withTerms(newMeasureDefinitionTermBuilder().withExponent(1).withUnit(length.getUnits().get(0)).build())
    		.build();
        
        usePackage(measures).withElements(time,mass,length,velocity,area,force,monthBasedTime).build();
        
        measureModel.addContent(demo);
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
