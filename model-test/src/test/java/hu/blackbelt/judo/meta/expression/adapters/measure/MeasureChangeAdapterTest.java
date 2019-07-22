package hu.blackbelt.judo.meta.expression.adapters.measure;

import com.google.common.collect.ImmutableMap;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.model.Measure;
import hu.blackbelt.judo.meta.expression.adapters.measure.model.Unit;
import org.eclipse.emf.common.util.ECollections;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

public class MeasureChangeAdapterTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MeasureChangeAdapterTest.class);
    private MeasureProvider<Measure, Unit> measureProvider;
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
            return ((Measure) args[0]).getName();
        }).when(measureProvider).getMeasureName(any(Measure.class));

        Mockito.doAnswer(invocationOnMock -> {
            final Object[] args = invocationOnMock.getArguments();
            return ((Measure) args[0]).getNamespace();
        }).when(measureProvider).getMeasureNamespace(any(Measure.class));

        modelAdapter = mock(ModelAdapter.class);

        measureAdapter = new MeasureAdapter(measureProvider, modelAdapter);
    }

    @AfterEach
    public void tearDown() {
        measureAdapter = null;
    }

    @Test
    public void testLogger() {
        Assert.assertEquals(0, measureAdapter.dimensions.size());

        final Map<Map<MeasureAdapter.MeasureId, Integer>, MeasureAdapter.MeasureId> expected = new HashMap<>();

        final Measure length = Measure.builder().namespace("base").name("Length").units(Arrays.asList(
                Unit.builder().name("millimetre").symbol("mm").build(),
                Unit.builder().name("centimetre").symbol("cm").build(),
                Unit.builder().name("decimetre").symbol("dm").build(),
                Unit.builder().name("metre").symbol("m").build()
        )).build();

        Mockito.when(measureProvider.getBaseMeasures(length)).thenReturn(ECollections.singletonEMap(length, 1));
        Mockito.when(measureProvider.isBaseMeasure(length)).thenReturn(true);

        measureChangedHandler.measureAdded(length);

        expected.put(Collections.singletonMap(measureIdFrom(length), 1), measureIdFrom(length));
        Assert.assertEquals(1, measureAdapter.dimensions.size());
        Assert.assertThat(measureAdapter.dimensions, is(expected));

        final Measure area = Measure.builder().namespace("derived").name("Area").units(Arrays.asList(
                Unit.builder().name("square millimetre").symbol("mm²").build(),
                Unit.builder().name("square centimetre").symbol("cm²").build(),
                Unit.builder().name("square decimetre").symbol("dm²").build()
        )).build();

        Mockito.when(measureProvider.getBaseMeasures(area)).thenReturn(ECollections.singletonEMap(length, 2));
        Mockito.when(measureProvider.isBaseMeasure(area)).thenReturn(false);

        measureChangedHandler.measureAdded(area);

        expected.put(Collections.singletonMap(measureIdFrom(length), 2), measureIdFrom(area));
        Assert.assertEquals(2, measureAdapter.dimensions.size());
        Assert.assertThat(measureAdapter.dimensions, is(expected));

    }

    @Test
    public void testBaseMeasureAddedChangedAndRemoved() {
        Assert.assertEquals(0, measureAdapter.dimensions.size());

        final Map<Map<MeasureAdapter.MeasureId, Integer>, MeasureAdapter.MeasureId> expected = new HashMap<>();

        final Measure length = Measure.builder().namespace("base").name("Length").units(Arrays.asList(
                Unit.builder().name("millimetre").symbol("mm").build(),
                Unit.builder().name("centimetre").symbol("cm").build(),
                Unit.builder().name("decimetre").symbol("dm").build(),
                Unit.builder().name("metre").symbol("m").build()
        )).build();
        Mockito.when(measureProvider.getBaseMeasures(length)).thenReturn(ECollections.singletonEMap(length, 1));
        Mockito.when(measureProvider.isBaseMeasure(length)).thenReturn(true);

        measureChangedHandler.measureAdded(length);

        expected.put(Collections.singletonMap(measureIdFrom(length), 1), measureIdFrom(length));
        Assert.assertEquals(1, measureAdapter.dimensions.size());
        Assert.assertThat(measureAdapter.dimensions, is(expected));

        measureChangedHandler.measureChanged(length);

        Assert.assertEquals(1, measureAdapter.dimensions.size());
        Assert.assertThat(measureAdapter.dimensions, is(expected));

        measureChangedHandler.measureRemoved(length);

        Assert.assertEquals(0, measureAdapter.dimensions.size());
        Assert.assertThat(measureAdapter.dimensions, is(Collections.emptyMap()));
    }

    @Test
    public void testDerivedMeasureAddedChangedAndRemoved() {
        Assert.assertEquals(0, measureAdapter.dimensions.size());

        final Map<Map<MeasureAdapter.MeasureId, Integer>, MeasureAdapter.MeasureId> expected = new HashMap<>();

        final Measure length = Measure.builder().namespace("base").name("Length").units(Arrays.asList(
                Unit.builder().name("millimetre").symbol("mm").build(),
                Unit.builder().name("centimetre").symbol("cm").build(),
                Unit.builder().name("decimetre").symbol("dm").build(),
                Unit.builder().name("metre").symbol("m").build()
        )).build();
        Mockito.when(measureProvider.getBaseMeasures(length)).thenReturn(ECollections.singletonEMap(length, 1));
        Mockito.when(measureProvider.isBaseMeasure(length)).thenReturn(true);

        final Measure time = Measure.builder().namespace("base").name("Time").units(Arrays.asList(
                Unit.builder().name("millisecond").symbol("ms").build(),
                Unit.builder().name("second").symbol("s").build()
        )).build();
        Mockito.when(measureProvider.getBaseMeasures(time)).thenReturn(ECollections.singletonEMap(time, 1));
        Mockito.when(measureProvider.isBaseMeasure(time)).thenReturn(true);

        measureChangedHandler.measureAdded(length);
        measureChangedHandler.measureAdded(time);

        Assert.assertEquals(2, measureAdapter.dimensions.size());

        final Measure velocity = Measure.builder().namespace("derived").name("Velocity").units(Arrays.asList(
                Unit.builder().name("metre per second").symbol("m/s").build()
        )).build();
        Mockito.when(measureProvider.getBaseMeasures(velocity)).thenReturn(ECollections.asEMap(ImmutableMap.of(length, 1, time, -1)));
        Mockito.when(measureProvider.isBaseMeasure(velocity)).thenReturn(false);

        measureChangedHandler.measureAdded(velocity);

        Assert.assertEquals(3, measureAdapter.dimensions.size());
        expected.put(Collections.singletonMap(measureIdFrom(length), 1), measureIdFrom(length));
        expected.put(Collections.singletonMap(measureIdFrom(time), 1), measureIdFrom(time));
        expected.put(ImmutableMap.of(measureIdFrom(length), 1, measureIdFrom(time), -1), measureIdFrom(velocity));
        Assert.assertThat(measureAdapter.dimensions.entrySet(), is(expected.entrySet()));

        final Measure acceleration = Measure.builder().namespace("derived").name("Acceleration").units(Arrays.asList(
                Unit.builder().name("metre per second square").symbol("m/s²").build()
        )).build();
        final Map<Measure, Integer> accelerationDimension = new HashMap<>();
        accelerationDimension.put(velocity, 1);

        Mockito.when(measureProvider.getBaseMeasures(acceleration)).thenReturn(ECollections.asEMap(accelerationDimension));
        Mockito.when(measureProvider.isBaseMeasure(acceleration)).thenReturn(false);

        Assertions.assertThrows(IllegalArgumentException.class, () -> measureChangedHandler.measureAdded(acceleration));

        accelerationDimension.put(time, -2);
        measureChangedHandler.measureAdded(acceleration);

        final Map<MeasureAdapter.MeasureId, Integer> expectedAccelerationDimension = new HashMap<>();
        expectedAccelerationDimension.put(measureIdFrom(length), 1);
        expectedAccelerationDimension.put(measureIdFrom(time), -3);
        expected.put(expectedAccelerationDimension, measureIdFrom(acceleration));
        Assert.assertEquals(4, measureAdapter.dimensions.size());
        Assert.assertThat(measureAdapter.dimensions.entrySet(), is(expected.entrySet()));

        accelerationDimension.put(time, -1);
        expectedAccelerationDimension.put(measureIdFrom(time), -2);
        measureChangedHandler.measureChanged(acceleration);

        Assert.assertEquals(4, measureAdapter.dimensions.size());
        Assert.assertThat(expected, equalTo(measureAdapter.dimensions));

        expected.clear();

        measureChangedHandler.measureRemoved(acceleration);
        measureChangedHandler.measureRemoved(velocity);
        measureChangedHandler.measureRemoved(length);
        measureChangedHandler.measureRemoved(time);

        Assert.assertEquals(0, measureAdapter.dimensions.size());
        Assert.assertThat(measureAdapter.dimensions, is(Collections.emptyMap()));
    }

    private MeasureAdapter.MeasureId measureIdFrom(final Measure measure) {
        return MeasureAdapter.MeasureId.fromMeasure(measureProvider, measure);
    }
}
