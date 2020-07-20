package hu.blackbelt.judo.meta.expression.adapters.psm;

import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newDerivedMeasureBuilder;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newDurationUnitBuilder;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newMeasureBuilder;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newMeasureDefinitionTermBuilder;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newUnitBuilder;
import static hu.blackbelt.judo.meta.psm.namespace.util.builder.NamespaceBuilders.newModelBuilder;
import static hu.blackbelt.judo.meta.psm.namespace.util.builder.NamespaceBuilders.newPackageBuilder;
import static hu.blackbelt.judo.meta.psm.namespace.util.builder.NamespaceBuilders.useModel;
import static hu.blackbelt.judo.meta.psm.namespace.util.builder.NamespaceBuilders.usePackage;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.buildPsmModel;
import static org.hamcrest.CoreMatchers.is;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.eclipse.emf.common.notify.Notifier;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;

import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.psm.measure.DerivedMeasure;
import hu.blackbelt.judo.meta.psm.measure.DurationType;
import hu.blackbelt.judo.meta.psm.measure.Measure;
import hu.blackbelt.judo.meta.psm.measure.Unit;
import hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders;
import hu.blackbelt.judo.meta.psm.namespace.Model;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;

public class PsmMeasureProviderTest {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PsmMeasureProviderTest.class);
    private PsmModel measureModel;
    private PsmModel psmModel;
    private MeasureProvider<Measure, Unit> measureProvider;

    //private MeasureAdapter<Measure, Unit, EClass> measureAdapter;

    @BeforeEach
    public void setUp() throws IOException, PsmModel.PsmValidationException {
        psmModel = buildPsmModel().name("test").build();

        measureProvider = new PsmMeasureProvider(psmModel.getResourceSet());
        measureModel = psmModel;
        populateMeasureModel();
        
        //TODO: uncomment after implementing PsmModelAdapter
        //measureAdapter = new MeasureAdapter<>(measureProvider, Mockito.mock(ModelAdapter.class));
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
        psmModel = null;
        measureProvider = null;
    }

    @Test
    void testGetMeasureNamespace() {
        log.info("Testing: getMeasureNamespace...");
        final Optional<Measure> mass = getMeasureByName("Mass");
        final Measure negtestMeasure = MeasureBuilders.newMeasureBuilder().withName("NegtestMeasure").build();

        Assert.assertTrue(mass.isPresent());
        Assert.assertThat(measureProvider.getMeasureNamespace(mass.get()), is("demo::measures"));
        Assert.assertTrue(measureProvider.getMeasureNamespace(negtestMeasure) == null);
    }

    @Test
    void testGetMeasureName() {
        log.info("Testing: getMeasureName...");
        final Optional<Measure> mass = measureProvider.getMeasures()
                .filter(m -> m.getUnits().parallelStream().anyMatch(u -> "kilogram".equals(u.getName())))
                .findAny();

        Assert.assertTrue(mass.isPresent());
        Assert.assertThat(measureProvider.getMeasureName(mass.get()), is("Mass"));

    }

    @Test
    void testGetMeasure() {
        log.info("Testing: getMeasure...");
        final Optional<Measure> length = measureProvider.getMeasure("demo::measures", "Length");

        final Optional<Measure> invalidName = measureProvider.getMeasure("demo::measures", "Price");
        final Optional<Measure> invalidNamespace = measureProvider.getMeasure("northwind::measures", "Length");
        final Optional<Measure> invalidNameAndNamespace = measureProvider.getMeasure("northwind::measures", "Price");

        final Optional<Measure> expectedLength = getMeasureByName("Length");

        Assert.assertTrue(length.isPresent());
        Assert.assertThat(length, is(expectedLength));
        Assert.assertFalse(invalidName.isPresent());
        Assert.assertFalse(invalidNamespace.isPresent());
        Assert.assertFalse(invalidNameAndNamespace.isPresent());
    }

    @Test
    public void testBaseMeasuresOfBaseMeasure() {
        final Measure length = getMeasureByName("Length").get();

        Assert.assertThat(measureProvider.getBaseMeasures(length).map(), is(Collections.singletonMap(length, 1)));
    }

    @Test
    public void testBaseMeasuresOfDerivedMeasure() {
        final Measure length = getMeasureByName("Length").get();
        final Measure area = getMeasureByName("Area").get();
        final Measure mass = getMeasureByName("Mass").get();
        final Measure time = getMeasureByName("Time").get();
        final Measure force = getMeasureByName("Force").get();

        Assert.assertThat(measureProvider.getBaseMeasures(area).map(), is(Collections.singletonMap(length, 2)));
        Assert.assertThat(measureProvider.getBaseMeasures(force).map(), is(ImmutableMap.of(
                mass, 1,
                length, 1,
                time, -2
        )));
    }

    @Test
    public void testGetUnits() {
        log.info("Testing: getUnits...");
        final Measure length = getMeasureByName("Length").get();

        Assert.assertThat(new HashSet<>(measureProvider.getUnits(length)), is(new HashSet<>(length.getUnits())));
    }

    @Test
    public void testIsDurationSupportingAddition() {
        log.info("Testing: isDurationSupportingAddition...");
        final Unit second = getUnitByName("millisecond").get();
        final Unit metre = getUnitByName("metre").get();
        final Unit microsecond = getUnitByName("microsecond").get();
        final Unit month = getUnitByName("month").get();

        Assert.assertTrue(measureProvider.isDurationSupportingAddition(second));
        Assert.assertFalse(measureProvider.isDurationSupportingAddition(metre));
        Assert.assertFalse(measureProvider.isDurationSupportingAddition(microsecond));
        Assert.assertFalse(measureProvider.isDurationSupportingAddition(month));
    }

    @Test
    public void testGetUnitByNameOrSymbol() {
        log.info("Testing: getUnitByNameOrSymbol...");
        final Optional<Measure> length = getMeasureByName("Length");
        final Optional<Unit> metre = getUnitByName("metre");
        final Optional<Measure> time = getMeasureByName("Time");
        final Optional<Unit> halfDay = getUnitByName("halfDay");

        Assert.assertThat(measureProvider.getUnitByNameOrSymbol(length, "metre"), is(metre));
        Assert.assertThat(measureProvider.getUnitByNameOrSymbol(Optional.empty(), "metre"), is(metre));
        Assert.assertThat(measureProvider.getUnitByNameOrSymbol(length, "m"), is(metre));
        Assert.assertThat(measureProvider.getUnitByNameOrSymbol(Optional.empty(), "m"), is(metre));
        Assert.assertThat(measureProvider.getUnitByNameOrSymbol(time, "halfDay"), is(halfDay));
        Assert.assertFalse(measureProvider.getUnitByNameOrSymbol(time, null).isPresent()); // units are not compared by symbol if is it not defined
        Assert.assertThat(measureProvider.getUnitByNameOrSymbol(Optional.empty(), "halfDay"), is(halfDay));
        Assert.assertFalse(measureProvider.getUnitByNameOrSymbol(Optional.empty(), null).isPresent()); // nothing is defined
    }

    @Test
    public void testGetMeasures() {
        log.info("Testing: getMeasures...");
        Assert.assertThat(measureProvider.getMeasures().count(), is(7L));

    }

    private Optional<Measure> getMeasureByName(final String measureName) {
        final Iterable<Notifier> measureContents = measureModel.getResourceSet()::getAllContents;
        return StreamSupport.stream(measureContents.spliterator(), true)
                .filter(e -> Measure.class.isAssignableFrom(e.getClass())).map(e -> (Measure) e)
                .filter(m -> measureName.equals(m.getName()))
                .findAny();
    }

    private Optional<Unit> getUnitByName(final String unitName) {
        final Iterable<Notifier> measureContents = measureModel.getResourceSet()::getAllContents;
        return StreamSupport.stream(measureContents.spliterator(), true)
                .filter(e -> Measure.class.isAssignableFrom(e.getClass())).map(e -> (Measure) e)
                .map(m -> m.getUnits().parallelStream().filter(u -> unitName.equals(u.getName())).findAny())
                .filter(u -> u.isPresent()).map(u -> u.get())
                .findAny();
    }
}
