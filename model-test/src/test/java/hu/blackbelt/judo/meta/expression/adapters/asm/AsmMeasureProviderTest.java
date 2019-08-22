package hu.blackbelt.judo.meta.expression.adapters.asm;

import com.google.common.collect.ImmutableMap;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.measure.Measure;
import hu.blackbelt.judo.meta.measure.Unit;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.measure.runtime.MeasureModel.LoadArguments.measureLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.measure.runtime.MeasureModel.loadMeasureModel;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class AsmMeasureProviderTest {

    private MeasureModel measureModel;
    private MeasureProvider<Measure, Unit> measureProvider;

    private MeasureAdapter<Measure, Unit, EClass> measureAdapter;

    @BeforeEach
    public void setUp() throws IOException, MeasureModel.MeasureValidationException {
    	
        measureModel = loadMeasureModel(measureLoadArgumentsBuilder()
                .uri(URI.createFileURI(new File("src/test/model/measure.model").getAbsolutePath()))
                .name("test"));

        measureProvider = new AsmMeasureProvider(measureModel.getResourceSet());

        measureAdapter = new MeasureAdapter<>(measureProvider, Mockito.mock(ModelAdapter.class));
    }

    @AfterEach
    public void tearDown() {
        measureModel = null;
        measureProvider = null;
    }

    @Test
    public void testGetMeasureNamespace() {
        final Optional<Measure> length = getMeasureByName("Length");

        assertTrue(length.isPresent());
        assertThat(measureProvider.getMeasureNamespace(length.get()), is("demo::measures"));
    }

    @Test
    public void testGetMeasureName() {
        final Optional<Measure> length = measureProvider.getMeasures()
                .filter(m -> m.getUnits().parallelStream().anyMatch(u -> "metre".equals(u.getName())))
                .findAny();

        assertTrue(length.isPresent());
        assertThat(measureProvider.getMeasureName(length.get()), is("Length"));
    }

    @Test
    public void testGetMeasure() {
        final Optional<Measure> length = measureProvider.getMeasure("demo::measures", "Length");

        final Optional<Measure> invalidName = measureProvider.getMeasure("demo::measures", "Price");
        final Optional<Measure> invalidNamespace = measureProvider.getMeasure("invalid::measures", "Length");
        final Optional<Measure> invalidNameAndNamespace = measureProvider.getMeasure("demo::measures", "Price");

        final Optional<Measure> expectedLength = getMeasureByName("Length");

        assertTrue(length.isPresent());
        assertThat(length, is(expectedLength));
        assertFalse(invalidName.isPresent());
        assertFalse(invalidNamespace.isPresent());
        assertFalse(invalidNameAndNamespace.isPresent());
    }

    @Test
    public void testBaseMeasuresOfBaseMeasure() {
        final Measure length = getMeasureByName("Length").get();

        assertThat(measureProvider.getBaseMeasures(length).map(), is(Collections.singletonMap(length, 1)));
    }

    @Test
    public void testBaseMeasuresOfDerivedMeasure() {
        final Measure length = getMeasureByName("Length").get();
        final Measure area = getMeasureByName("Area").get();
        final Measure mass = getMeasureByName("Mass").get();
        final Measure time = getMeasureByName("Time").get();
        final Measure force = getMeasureByName("Force").get();

        assertThat(measureProvider.getBaseMeasures(area).map(), is(Collections.singletonMap(length, 2)));
        assertThat(measureProvider.getBaseMeasures(force).map(), is(ImmutableMap.of(
                mass, 1,
                length, 1,
                time, -2
        )));
    }

    @Test
    public void testGetUnits() {
        final Measure length = getMeasureByName("Length").get();

        assertThat(new HashSet<>(measureProvider.getUnits(length)), is(new HashSet<>(length.getUnits())));
    }

    @Test
    public void testIsDurationSupportingAddition() {
        final Unit second = getUnitByName("millisecond").get();
        final Unit metre = getUnitByName("metre").get();
        final Unit halfDay = getUnitByName("halfDay").get();
        final Unit month = getUnitByName("month").get();

        assertTrue(measureProvider.isDurationSupportingAddition(second));
        assertFalse(measureProvider.isDurationSupportingAddition(metre));
        assertFalse(measureProvider.isDurationSupportingAddition(halfDay));
        assertFalse(measureProvider.isDurationSupportingAddition(month));
    }

    @Test
    public void testGetUnitByNameOrSymbol() {
        final Optional<Measure> length = getMeasureByName("Length");
        final Optional<Unit> metre = getUnitByName("metre");
        final Optional<Measure> time = getMeasureByName("Time");
        final Optional<Unit> halfDay = getUnitByName("halfDay");

        assertThat(measureProvider.getUnitByNameOrSymbol(length, "metre"), is(metre));
        assertThat(measureProvider.getUnitByNameOrSymbol(Optional.empty(), "metre"), is(metre));
        assertThat(measureProvider.getUnitByNameOrSymbol(length, "m"), is(metre));
        assertThat(measureProvider.getUnitByNameOrSymbol(Optional.empty(), "m"), is(metre));
        assertThat(measureProvider.getUnitByNameOrSymbol(time, "halfDay"), is(halfDay));
        assertFalse(measureProvider.getUnitByNameOrSymbol(time, null).isPresent()); // units are not compared by symbol if is it not defined
        assertThat(measureProvider.getUnitByNameOrSymbol(Optional.empty(), "halfDay"), is(halfDay));
        assertFalse(measureProvider.getUnitByNameOrSymbol(Optional.empty(), null).isPresent()); // nothing is defined
    }

    @Test
    public void testGetMeasures() {
        assertThat(measureProvider.getMeasures().count(), is(29L));
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