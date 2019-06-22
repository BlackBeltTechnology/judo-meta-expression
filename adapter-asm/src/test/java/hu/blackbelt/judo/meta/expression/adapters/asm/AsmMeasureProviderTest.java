package hu.blackbelt.judo.meta.expression.adapters.asm;

import com.google.common.collect.ImmutableMap;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.measure.Measure;
import hu.blackbelt.judo.meta.measure.Unit;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModelLoader;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.hamcrest.Matchers.*;

public class AsmMeasureProviderTest {

    private MeasureModel measureModel;
    private MeasureProvider<Measure, Unit> measureProvider;

    private MeasureAdapter<Measure, Unit, EClass> measureAdapter;

    @BeforeEach
    public void setUp() throws IOException {
        measureModel = MeasureModelLoader.loadMeasureModel(
                URI.createURI(new File(srcDir(), "test/models/measure.model").getAbsolutePath()),
                "test",
                "1.0.0");
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

        Assert.assertTrue(length.isPresent());
        Assert.assertThat(measureProvider.getMeasureNamespace(length.get()), is("northwind.measures"));
    }

    @Test
    public void testGetMeasureName() {
        final Optional<Measure> length = measureProvider.getMeasures()
                .filter(m -> m.getUnits().parallelStream().anyMatch(u -> "metre".equals(u.getName())))
                .findAny();

        Assert.assertTrue(length.isPresent());
        Assert.assertThat(measureProvider.getMeasureName(length.get()), is("Length"));
    }

    @Test
    public void testGetMeasure() {
        final Optional<Measure> length = measureProvider.getMeasure("northwind.measures", "Length");

        final Optional<Measure> invalidName = measureProvider.getMeasure("northwind.measures", "Price");
        final Optional<Measure> invalidNamespace = measureProvider.getMeasure("demo.measures", "Length");
        final Optional<Measure> invalidNameAndNamespace = measureProvider.getMeasure("demo.measures", "Price");

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

        Assert.assertThat(measureProvider.getBaseMeasures(length), is(Collections.singletonMap(length, 1)));
    }

    @Test
    public void testBaseMeasuresOfDerivedMeasure() {
        final Measure length = getMeasureByName("Length").get();
        final Measure area = getMeasureByName("Area").get();
        final Measure mass = getMeasureByName("Mass").get();
        final Measure time = getMeasureByName("Time").get();
        final Measure force = getMeasureByName("Force").get();

        Assert.assertThat(measureProvider.getBaseMeasures(area), is(Collections.singletonMap(length, 2)));
        Assert.assertThat(measureProvider.getBaseMeasures(force), is(ImmutableMap.of(
                mass, 1,
                length, 1,
                time, -2
        )));
    }

    @Test
    public void testGetUnits() {
        final Measure length = getMeasureByName("Length").get();

        Assert.assertThat(new HashSet<>(measureProvider.getUnits(length)), is(new HashSet<>(length.getUnits())));
    }

    @Test
    public void testIsDurationSupportingAddition() {
        final Unit second = getUnitByName("millisecond").get();
        final Unit metre = getUnitByName("metre").get();
        final Unit halfDay = getUnitByName("halfDay").get();
        final Unit month = getUnitByName("month").get();

        Assert.assertTrue(measureProvider.isDurationSupportingAddition(second));
        Assert.assertFalse(measureProvider.isDurationSupportingAddition(metre));
        Assert.assertFalse(measureProvider.isDurationSupportingAddition(halfDay));
        Assert.assertFalse(measureProvider.isDurationSupportingAddition(month));
    }

    @Test
    public void testGetUnitByNameOrSymbol() {
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
        Assert.assertThat(measureProvider.getMeasures().count(), is(29L));
    }

    private Optional<Measure> getMeasureByName(final String measureName) {
        return measureProvider.getMeasures()
                .filter(m -> measureName.equals(m.getName()))
                .findAny();
    }

    private Optional<Unit> getUnitByName(final String unitName) {
        return measureProvider.getMeasures()
                .map(m -> m.getUnits().parallelStream().filter(u -> unitName.equals(u.getName())).findAny())
                .filter(u -> u.isPresent()).map(u -> u.get())
                .findAny();
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
