package hu.blackbelt.judo.meta.expression.adapters.psm;

import com.google.common.collect.ImmutableMap;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.psm.measure.Measure;
import hu.blackbelt.judo.meta.psm.measure.Unit;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModelLoader;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.hamcrest.CoreMatchers.is;

@Slf4j
public class PsmMeasureProviderTest {

    private PsmModel measureModel;
    private PsmModel psmModel;
    private MeasureProvider<Measure, Unit> measureProvider;

    private MeasureAdapter<Measure, Unit, EClass> measureAdapter;

    @BeforeEach
    public void setUp() throws IOException {
        psmModel = PsmModelLoader.loadPsmModel(
                URI.createURI(new File(srcDir(), "test/models/psm.model").getAbsolutePath()),
                "test",
                "1.0.0");
        measureProvider = new PsmMeasureProvider(psmModel.getResourceSet());
        measureModel = psmModel;
        //TODO: uncomment after implementing PsmModelAdapter
        //measureAdapter = new MeasureAdapter<>(measureProvider, Mockito.mock(ModelAdapter.class));
    }

    @AfterEach
    public void tearDown() {
        psmModel = null;
        measureProvider = null;
    }

    private File srcDir() {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath + "../../src");
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }

    @Test
    void testGetMeasureNamespace() {
        log.info("Testing: getMeasureNamespace...");
        final Optional<Measure> mass = getMeasureByName("Mass");

        Assert.assertTrue(mass.isPresent());
        Assert.assertThat(measureProvider.getMeasureNamespace(mass.get()), is("northwind::measures"));
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
        final Optional<Measure> length = measureProvider.getMeasure("northwind::measures", "Length");

        final Optional<Measure> invalidName = measureProvider.getMeasure("northwind::measures", "Price");
        final Optional<Measure> invalidNamespace = measureProvider.getMeasure("demo::measures", "Length");
        final Optional<Measure> invalidNameAndNamespace = measureProvider.getMeasure("demo::measures", "Price");

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
        log.info("Testing: getUnits...");
        final Measure length = getMeasureByName("Length").get();

        Assert.assertThat(new HashSet<>(measureProvider.getUnits(length)), is(new HashSet<>(length.getUnits())));
    }

    @Test
    public void testIsDurationSupportingAddition() {
        log.info("Testing: isDurationSupportingAddition...");
        final Unit second = getUnitByName("millisecond").get();
        final Unit metre = getUnitByName("metre").get();
        final Unit halfDay = getUnitByName("halfDay").get(); //TODO: check, b\c halfDay not existing in asmmodel or psmmodel but measuremodel (but we are using psmmodel for measuremodel)
        final Unit month = getUnitByName("month").get();

        Assert.assertTrue(measureProvider.isDurationSupportingAddition(second));
        Assert.assertFalse(measureProvider.isDurationSupportingAddition(metre));
        Assert.assertFalse(measureProvider.isDurationSupportingAddition(halfDay)); //TODO: check
        Assert.assertFalse(measureProvider.isDurationSupportingAddition(month));
    }

    @Test
    public void testGetUnitByNameOrSymbol() {
        log.info("Testing: getUnitByNameOrSymbol...");
        final Optional<Measure> length = getMeasureByName("Length");
        final Optional<Unit> metre = getUnitByName("metre");
        final Optional<Measure> time = getMeasureByName("Time");
        final Optional<Unit> halfDay = getUnitByName("halfDay");  //TODO: check

        Assert.assertThat(measureProvider.getUnitByNameOrSymbol(length, "metre"), is(metre));
        Assert.assertThat(measureProvider.getUnitByNameOrSymbol(Optional.empty(), "metre"), is(metre));
        Assert.assertThat(measureProvider.getUnitByNameOrSymbol(length, "m"), is(metre));
        Assert.assertThat(measureProvider.getUnitByNameOrSymbol(Optional.empty(), "m"), is(metre));
        Assert.assertThat(measureProvider.getUnitByNameOrSymbol(time, "halfDay"), is(halfDay)); //TODO: check
        Assert.assertFalse(measureProvider.getUnitByNameOrSymbol(time, null).isPresent()); // units are not compared by symbol if is it not defined
        Assert.assertThat(measureProvider.getUnitByNameOrSymbol(Optional.empty(), "halfDay"), is(halfDay)); //TODO: check
        Assert.assertFalse(measureProvider.getUnitByNameOrSymbol(Optional.empty(), null).isPresent()); // nothing is defined
    }

    @Test
    public void testGetMeasures() {
        log.info("Testing: getMeasures...");
        Assert.assertThat(measureProvider.getMeasures().count(), is(29L)); //TODO: ask hwat da hek

    }

    /* TODO: remove if tested elsewhere
    @Test
    public void testSetMeasureChangeHandler() {


    }
     */

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
