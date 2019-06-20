package hu.blackbelt.judo.meta.expression.adapters.measure;

import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.model.Measure;
import hu.blackbelt.judo.meta.expression.adapters.measure.model.Unit;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EMap;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

@Slf4j
public class MeasureChangeAdapterTest {

    private MeasureProvider<Measure, Unit> measureProvider;
    private MeasureSupport<Object, Unit> measureSupport;
    private ModelAdapter modelAdapter;
    private MeasureAdapter measureAdapter;

    private MeasureChangedHandler<Measure> measureChangedHandler;

    @BeforeEach
    public void setUp() {
        measureProvider = mock(MeasureProvider.class);

        Mockito.doAnswer(invocationOnMock -> {
            final Object[] args = invocationOnMock.getArguments();
            measureChangedHandler = (MeasureChangedHandler<Measure>) args[0];
            return null;
        }).when(measureProvider).setMeasureChangeHandler(any(MeasureChangedHandler.class));

        Mockito.doAnswer(invocationOnMock -> {
            final Object[] args = invocationOnMock.getArguments();
            return Objects.equals(args[0], args[1]);
        }).when(measureProvider).equals(any(Measure.class), any(Measure.class));

        modelAdapter = mock(ModelAdapter.class);
        measureSupport = mock(MeasureSupport.class);

        measureAdapter = new MeasureAdapter(measureProvider, measureSupport, modelAdapter);
    }

    @AfterEach
    public void tearDown() {
        measureAdapter = null;
    }

    @Test
    public void testLogger() {
        Assert.assertEquals(0, measureAdapter.dimensions.size());

        Logger.getRootLogger().setLevel(Level.ALL);

        final EMap expected = ECollections.asEMap(new HashMap<>());

        final Measure length = Measure.builder().namespace("base").name("Length").units(Arrays.asList(
                Unit.builder().name("millimetre").symbol("mm").build(),
                Unit.builder().name("centimetre").symbol("cm").build(),
                Unit.builder().name("decimetre").symbol("dm").build(),
                Unit.builder().name("metre").symbol("m").build()
        )).build();

        final EMap<Measure, Integer> lengthDimension = ECollections.singletonEMap(length, 1);
        expected.put(lengthDimension, length);
        Mockito.when(measureProvider.getBaseMeasures(length)).thenReturn(lengthDimension.map());
        Mockito.when(measureProvider.isBaseMeasure(length)).thenReturn(true);

        measureChangedHandler.measureAdded(length);

        Assert.assertEquals(1, measureAdapter.dimensions.size());
    }

    @Test
    public void testBaseMeasureAddedChangedAndRemoved() {
        Assert.assertEquals(0, measureAdapter.dimensions.size());

        final EMap expected = ECollections.asEMap(new HashMap<>());

        final Measure length = Measure.builder().namespace("base").name("Length").units(Arrays.asList(
                Unit.builder().name("millimetre").symbol("mm").build(),
                Unit.builder().name("centimetre").symbol("cm").build(),
                Unit.builder().name("decimetre").symbol("dm").build(),
                Unit.builder().name("metre").symbol("m").build()
        )).build();
        final EMap<Measure, Integer> lengthDimension = ECollections.singletonEMap(length, 1);
        expected.put(lengthDimension, length);
        Mockito.when(measureProvider.getBaseMeasures(length)).thenReturn(lengthDimension.map());
        Mockito.when(measureProvider.isBaseMeasure(length)).thenReturn(true);

        measureChangedHandler.measureAdded(length);
        Assert.assertEquals(1, measureAdapter.dimensions.size());
        Assert.assertThat(measureAdapter.dimensions.entrySet(), is(expected.entrySet()));

        measureChangedHandler.measureChanged(length);
        Assert.assertEquals(1, measureAdapter.dimensions.size());
        Assert.assertThat(measureAdapter.dimensions.entrySet(), is(expected.entrySet()));

        expected.remove(lengthDimension);
        measureChangedHandler.measureRemoved(length);

        Assert.assertEquals(0, measureAdapter.dimensions.size());
        Assert.assertThat(measureAdapter.dimensions.entrySet(), is(expected.entrySet()));
    }

    @Test
    public void testDerivedMeasureAddedChangedAndRemoved() {
        Assert.assertEquals(0, measureAdapter.dimensions.size());

        final Map<EMap<Measure, Integer>, Measure> expected = new HashMap<>();

        final Measure length = Measure.builder().namespace("base").name("Length").units(Arrays.asList(
                Unit.builder().name("millimetre").symbol("mm").build(),
                Unit.builder().name("centimetre").symbol("cm").build(),
                Unit.builder().name("decimetre").symbol("dm").build(),
                Unit.builder().name("metre").symbol("m").build()
        )).build();
        final EMap<Measure, Integer> lengthDimension = ECollections.singletonEMap(length, 1);
        expected.put(lengthDimension, length);
        Mockito.when(measureProvider.getBaseMeasures(length)).thenReturn(lengthDimension.map());
        Mockito.when(measureProvider.isBaseMeasure(length)).thenReturn(true);

        final Measure time = Measure.builder().namespace("base").name("Time").units(Arrays.asList(
                Unit.builder().name("millisecond").symbol("ms").build(),
                Unit.builder().name("second").symbol("s").build()
        )).build();
        final EMap<Measure, Integer> timeDimension = ECollections.singletonEMap(time, 1);
        expected.put(timeDimension, time);
        Mockito.when(measureProvider.getBaseMeasures(time)).thenReturn(timeDimension.map());
        Mockito.when(measureProvider.isBaseMeasure(time)).thenReturn(true);

        measureChangedHandler.measureAdded(length);
        measureChangedHandler.measureAdded(time);

        Assert.assertEquals(2, measureAdapter.dimensions.size());

        final Measure velocity = Measure.builder().namespace("derived").name("Velocity").units(Arrays.asList(
                Unit.builder().name("metre per second").symbol("m/s").build()
        )).build();
        final EMap<Measure, Integer> velocityDimension = ECollections.asEMap(new HashMap<>());
        velocityDimension.put(length, 1);
        velocityDimension.put(time, -1);
        expected.put(velocityDimension, velocity);
        Mockito.when(measureProvider.getBaseMeasures(velocity)).thenReturn(velocityDimension.map());
        Mockito.when(measureProvider.isBaseMeasure(velocity)).thenReturn(false);

        measureChangedHandler.measureAdded(velocity);

        Assert.assertEquals(3, measureAdapter.dimensions.size());
        Assert.assertThat(measureAdapter.dimensions.entrySet(), is(expected.entrySet()));

        final Measure acceleration = Measure.builder().namespace("derived").name("Acceleration").units(Arrays.asList(
                Unit.builder().name("metre per second square").symbol("m/s²").build()
        )).build();
        final EMap<Measure, Integer> accelerationDimension = ECollections.asEMap(new HashMap<>());
        accelerationDimension.put(velocity, 1);
        Mockito.when(measureProvider.getBaseMeasures(acceleration)).thenReturn(accelerationDimension.map());
        Mockito.when(measureProvider.isBaseMeasure(acceleration)).thenReturn(false);

        Assertions.assertThrows(IllegalArgumentException.class, () -> measureChangedHandler.measureAdded(acceleration));

        accelerationDimension.put(time, -2);
        measureChangedHandler.measureAdded(acceleration);

        final EMap<Measure, Integer> expectedAccelerationDimension = ECollections.asEMap(new HashMap<>());
        expectedAccelerationDimension.put(length, 1);
        expectedAccelerationDimension.put(time, -3);
        expected.put(expectedAccelerationDimension, acceleration);

        Assert.assertEquals(4, measureAdapter.dimensions.size());
        Assert.assertThat(measureAdapter.dimensions.entrySet(), is(expected.entrySet()));

        accelerationDimension.put(time, -1);
        expectedAccelerationDimension.put(time, -2);
        measureChangedHandler.measureChanged(acceleration);

        Assert.assertEquals(4, measureAdapter.dimensions.size());
        Assert.assertThat(expected, equalTo(measureAdapter.dimensions));

        expected.clear();

        measureChangedHandler.measureRemoved(acceleration);
        measureChangedHandler.measureRemoved(velocity);
        measureChangedHandler.measureRemoved(length);
        measureChangedHandler.measureRemoved(time);

        Assert.assertEquals(0, measureAdapter.dimensions.size());
        Assert.assertThat(measureAdapter.dimensions.entrySet(), is(expected.entrySet()));
    }
}
