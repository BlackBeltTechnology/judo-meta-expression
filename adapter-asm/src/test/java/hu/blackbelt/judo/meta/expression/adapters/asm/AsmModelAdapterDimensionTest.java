package hu.blackbelt.judo.meta.expression.adapters.asm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureSupport;
import hu.blackbelt.judo.meta.measure.BaseMeasure;
import hu.blackbelt.judo.meta.measure.DerivedMeasure;
import hu.blackbelt.judo.meta.measure.Measure;
import hu.blackbelt.judo.meta.measure.Unit;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModelLoader;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Arrays;

import static hu.blackbelt.judo.meta.measure.util.builder.MeasureBuilders.*;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static org.hamcrest.Matchers.is;

public class AsmModelAdapterDimensionTest {

    private MeasureAdapter<Measure, Unit, EClass> measureAdapter;
    private MeasureProvider<Measure, Unit> measureProvider;
    private Resource resource;

    @BeforeEach
    public void setUp() {
        final ResourceSet resourceSet = MeasureModelLoader.createMeasureResourceSet();
        resource = resourceSet.createResource(URI.createURI("urn:measure.judo-meta-measure"));
        measureProvider = new AsmMeasureProvider(resourceSet);
        measureAdapter = new MeasureAdapter<>(measureProvider, Mockito.mock(MeasureSupport.class), Mockito.mock(ModelAdapter.class));
    }

    @AfterEach
    public void tearDown() {
        resource = null;
        measureAdapter = null;
    }

    @Test
    public void testBaseMeasuresChanged() {
        final Measure length = newBaseMeasureBuilder()
                .withNamespace("custom")
                .withName("Length")
                .withUnits(ImmutableList.of(
                        newUnitBuilder()
                                .withName("metre")
                                .withRateDividend(BigDecimal.ONE)
                                .withRateDivisor(BigDecimal.ONE)
                                .withSymbol("m")
                                .build()
                )).build();

        // base measure is added
        resource.getContents().add(length);
        Assert.assertThat(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("m").build()).get(),
                is(ECollections.singletonEMap(length, 1)));

        // test cleanup
        resource.getContents().remove(length);
        Assert.assertFalse(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("m").build()).isPresent());
    }

    @Test
    public void testDerivedMeasuresChanged() {
        final BaseMeasure length = newBaseMeasureBuilder()
                .withNamespace("custom")
                .withName("Length")
                .withUnits(ImmutableList.of(
                        newUnitBuilder()
                                .withName("metre")
                                .withRateDividend(BigDecimal.ONE)
                                .withRateDivisor(BigDecimal.ONE)
                                .withSymbol("m")
                                .build()
                )).build();
        final DerivedMeasure area = newDerivedMeasureBuilder()
                .withNamespace("custom")
                .withName("Area")
                .withTerms(
                        newBaseMeasureTermBuilder()
                                .withBaseMeasure(length)
                                .withExponent(2)
                                .build())
                .withUnits(ImmutableList.of(
                        newUnitBuilder()
                                .withName("square metre")
                                .withRateDividend(BigDecimal.ONE)
                                .withRateDivisor(BigDecimal.ONE)
                                .withSymbol("m²")
                                .build()
                )).build();
        final BaseMeasure time = newBaseMeasureBuilder()
                .withNamespace("custom")
                .withName("Time")
                .withUnits(ImmutableList.of(
                        newUnitBuilder()
                                .withName("second")
                                .withRateDividend(BigDecimal.ONE)
                                .withRateDivisor(BigDecimal.ONE)
                                .withSymbol("s")
                                .build()
                )).build();
        final Measure velocity = newDerivedMeasureBuilder()
                .withNamespace("custom")
                .withName("Velocity")
                .withTerms(ImmutableList.of(newBaseMeasureTermBuilder()
                                .withBaseMeasure(length)
                                .withExponent(1)
                                .build(),
                        newBaseMeasureTermBuilder()
                                .withBaseMeasure(time)
                                .withExponent(-1)
                                .build()))
                .withUnits(ImmutableList.of(
                        newUnitBuilder()
                                .withName("second")
                                .withRateDividend(BigDecimal.ONE)
                                .withRateDivisor(BigDecimal.ONE)
                                .withSymbol("m/s")
                                .build()
                )).build();

        resource.getContents().addAll(Arrays.asList(length, area, time));

        // base and derived measures are added as collections
        Assert.assertThat(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("m").build()).get(),
                is(ECollections.singletonEMap(length, 1)));
        Assert.assertThat(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("m²").build()).get(),
                is(ECollections.singletonEMap(length, 2)));
        Assert.assertThat(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("s").build()).get(),
                is(ECollections.singletonEMap(time, 1)));

        resource.getContents().add(velocity);

        // derived measure is added
        Assert.assertThat(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("m/s").build()).get().entrySet(),
                is(ECollections.asEMap(ImmutableMap.of(length, 1, time, -1)).entrySet()));

        final DerivedMeasure volume = newDerivedMeasureBuilder()
                .withNamespace("custom")
                .withName("Volume")
                .withUnits(ImmutableList.of(
                        newUnitBuilder()
                                .withName("cubic metre")
                                .withRateDividend(BigDecimal.ONE)
                                .withRateDivisor(BigDecimal.valueOf(1000L))
                                .withSymbol("dm³")
                                .build()
                )).build();
        resource.getContents().add(volume);

        // dimension of volume is not defined yet
        Assert.assertThat(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("dm³").build()).get(),
                is(ECollections.emptyEMap()));

        volume.getTerms().add(newBaseMeasureTermBuilder()
                .withBaseMeasure(length)
                .withExponent(3)
                .build());

        // dimension of volume is defined
        Assert.assertThat(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("dm³").build()).get(),
                is(ECollections.singletonEMap(length, 3)));

        resource.getContents().removeAll(Arrays.asList(length, area, volume, time));
        resource.getContents().remove(velocity);

        // test cleanup
        Assert.assertFalse(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("m").build()).isPresent());
        Assert.assertFalse(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("m²").build()).isPresent());
        Assert.assertFalse(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("s").build()).isPresent());
        Assert.assertFalse(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("m/s").build()).isPresent());
        Assert.assertFalse(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("dm³").build()).isPresent());
    }
}
