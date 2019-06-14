package hu.blackbelt.judo.meta.expression.adapters.measure;

import hu.blackbelt.judo.meta.expression.adapters.measure.model.Measure;
import hu.blackbelt.judo.meta.expression.adapters.measure.model.Unit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;
import static org.hamcrest.Matchers.is;

@Slf4j
public class MeasureAdapterTest {

    private DummyMeasureProvider measureProvider = new DummyMeasureProvider();
    private MeasureSupport<Class, Unit> measureSupport = new DummyMeasureSupport();
    private MeasureAdapter measureAdapter;

    @BeforeEach
    public void setUp() {
        measureAdapter = new MeasureAdapter(null, measureProvider, measureSupport);
    }

    @AfterEach
    public void tearDown() {
        measureAdapter = null;
    }

    @Test
    public void testGet() {
        final Optional<Measure> time = measureAdapter.get(newMeasureNameBuilder().withNamespace("system").withName("Time").build());
        Assert.assertTrue(time.isPresent());
        Assert.assertThat(time.get(), is(measureProvider.getTime()));

        final Optional<Measure> length = measureAdapter.get(newMeasureNameBuilder().withNamespace("base").withName("Length").build());
        Assert.assertTrue(length.isPresent());
        Assert.assertThat(length.get(), is(measureProvider.getLength()));

        final Optional<Measure> mass = measureAdapter.get(newMeasureNameBuilder().withNamespace("base").withName("Mass").build());
        Assert.assertFalse(mass.isPresent());

        final Optional<Measure> velocity = measureAdapter.get(newMeasureNameBuilder().withNamespace("derived").withName("Velocity").build());
        Assert.assertTrue(velocity.isPresent());
        Assert.assertThat(velocity.get(), is(measureProvider.getVelocity()));
    }

    @Test
    public void testGetDurationMeasure() {
        final Optional<Measure> time = measureAdapter.getDurationMeasure();
        Assert.assertTrue(time.isPresent());
        Assert.assertThat(time.get(), is(measureProvider.getTime()));
    }

    @Test
    public void testRefresh() {
        measureAdapter.refreshDimensions();

        final Map<Map<Measure, Integer>, Measure> dimensions = measureAdapter.dimensions;

        Assert.assertThat(dimensions.size(), is(3));

        final Map<Map<Measure, Integer>, Measure> expected = new HashMap<>();
        expected.put(Collections.singletonMap(measureProvider.getTime(), 1), measureProvider.getTime());
        expected.put(Collections.singletonMap(measureProvider.getLength(), 1), measureProvider.getLength());

        final Map<Measure, Integer> velocityBase = new HashMap<>();
        velocityBase.put(measureProvider.getLength(), 1);
        velocityBase.put(measureProvider.getTime(), -1);
        expected.put(velocityBase, measureProvider.getVelocity());

        Assert.assertThat(dimensions, is(expected));
    }
}
