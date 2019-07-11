package hu.blackbelt.judo.meta.expression.adapters.psm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.data.PrimitiveTypedElement;
import hu.blackbelt.judo.meta.psm.data.ReferenceTypedElement;
import hu.blackbelt.judo.meta.psm.measure.DerivedMeasure;
import hu.blackbelt.judo.meta.psm.measure.Measure;
import hu.blackbelt.judo.meta.psm.measure.Unit;
import hu.blackbelt.judo.meta.psm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.psm.runtime.PsmModelLoader;
import hu.blackbelt.judo.meta.psm.type.EnumerationType;
import hu.blackbelt.judo.meta.psm.type.Primitive;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newMeasuredDecimalBuilder;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;

public class PsmModelAdapterDimensionTest {

    private MeasureAdapter<Measure, Unit, EntityType> measureAdapter;
    private MeasureProvider<Measure, Unit> measureProvider;
    private Resource resource;

    @BeforeEach
    public void setUp() {
        final ResourceSet resourceSet = PsmModelLoader.createPsmResourceSet();
        resource = resourceSet.createResource(URI.createURI("urn:psm.judo-meta-psm"));
        measureProvider = new PsmMeasureProvider(resourceSet);

        final ModelAdapter<NamespaceElement, Primitive, PrimitiveTypedElement, EnumerationType, EntityType, ReferenceTypedElement, Measure, Unit> modelAdapter = Mockito.mock(ModelAdapter.class);

        Mockito.doAnswer(invocationOnMock -> {
            final Object[] args = invocationOnMock.getArguments();
            if (args[0] instanceof MeasuredDecimal) {
                final MeasuredDecimal measuredDecimal = (MeasuredDecimal) args[0];
                return measureAdapter.getUnit(measuredDecimal.getMeasure() != null ? Optional.ofNullable(measuredDecimal.getMeasure().getNamespace()) : Optional.empty(),
                        measuredDecimal.getMeasure() != null ? Optional.ofNullable(measuredDecimal.getMeasure().getName()) : Optional.empty(),
                        measuredDecimal.getUnitName());
            } else {
                throw new IllegalStateException("Not supported by mock");
            }
        }).when(modelAdapter).getUnit(any(NumericExpression.class));

        measureAdapter = new MeasureAdapter<>(measureProvider, modelAdapter);
    }

    @AfterEach
    public void tearDown() {
        resource = null;
        measureAdapter = null;
    }

    @Test
    public void testBaseMeasuresChanged() {
        final Measure length = newMeasureBuilder()
                .withName("Length")
                .withUnits(ImmutableList.of(
                        newUnitBuilder()
                                .withName("metre")
                                .withRateDividend(1.0)
                                .withRateDivisor(1.0)
                                .withSymbol("m")
                                .build()
                ))
                .build();

        //(nonDerived)Measure added
        resource.getContents().add(length);
        try {
            Assert.assertThat(
                    measureAdapter.getDimension(
                    newMeasuredDecimalBuilder()
                            .withValue(BigDecimal.ONE)
                            .withUnitName("m")
                            .build()).get()
                    , is(Collections.singletonMap(measureIdFrom(length), 1))
                    );

        } catch (RuntimeException up) { //TODO: rename, 4th graders joke, not funny anymore
            up.printStackTrace();
            throw up;
        }
    }

    public void testDerivedMeasuresChanged() {
        final Unit metre = newUnitBuilder()
                .withName("metre")
                .withRateDividend(1.0)
                .withRateDivisor(1.0)
                .withSymbol("m")
                .build();

        final Measure length = newMeasureBuilder()
                .withName("Length")
                .withUnits(ImmutableList.of(metre)).build();

        final DerivedMeasure area = newDerivedMeasureBuilder()
                .withName("Area")
                .withTerms(
                        newMeasureDefinitionTermBuilder()
                                .withUnit(metre)
                                .withExponent(2)
                                .build()
                )
                .withUnits(ImmutableList.of(
                        newUnitBuilder()
                                .withName("square metre")
                                .withRateDivisor(1.0)
                                .withRateDividend(1.0)
                                .withSymbol("m²")
                                .build()
                )).build();

        final Unit second = newUnitBuilder()
                .withName("second")
                .withRateDividend(1.0)
                .withRateDivisor(1.0)
                .withSymbol("s")
                .build();

        final Measure time = newMeasureBuilder()
                .withName("Time")
                .withUnits(ImmutableList.of(second)).build();

        final Measure velocity = newDerivedMeasureBuilder()
                .withName("Velocity")
                .withTerms(ImmutableList.of(
                        newMeasureDefinitionTermBuilder()
                                .withUnit(metre)
                                .withExponent(1)
                                .build(),
                        newMeasureDefinitionTermBuilder()
                                .withUnit(second)
                                .withExponent(-1)
                                .build()))
                .withUnits(ImmutableList.of(
                        newUnitBuilder()
                                .withName("second")
                                .withRateDividend(1.0)
                                .withRateDivisor(1.0)
                                .withSymbol("m/s")
                                .build()
                )).build();

        resource.getContents().addAll(Arrays.asList(length, area, time));
        //(nonDerived)Measures added as collections


        Assert.assertThat(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("m").build()).get(),
                is(Collections.singletonMap(measureIdFrom(length), 1)));
        Assert.assertThat(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("m²").build()).get(),
                is(Collections.singletonMap(measureIdFrom(length), 2)));
        Assert.assertThat(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("s").build()).get(),
                is(Collections.singletonMap(measureIdFrom(time), 1)));

        resource.getContents().add(velocity);
        //DerivedMeasure added
        Assert.assertThat(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("m/s").build()).get(),
                is(ImmutableMap.of(measureIdFrom(length), 1, measureIdFrom(time), 2)));

        final DerivedMeasure volume = newDerivedMeasureBuilder()
                .withName("Volume")
                .withUnits(ImmutableList.of(
                        newUnitBuilder()
                                .withName("cubic metre")
                                .withRateDividend(1.)
                                .withRateDivisor(1000.)
                                .withSymbol("dm³")
                                .build()
                )).build();
        resource.getContents().add(volume);

        //dimension of volume not defined yet
        Assert.assertThat(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("dm³").build()).get(),
                is(Collections.emptyMap()));

        volume.getTerms().add(newMeasureDefinitionTermBuilder()
                .withUnit(metre)
                .withExponent(3)
                .build());

        //dimension of volume defined
        Assert.assertThat(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("dm³").build()).get(),
                is(Collections.singletonMap(measureIdFrom(length), 3)));

        resource.getContents().removeAll(Arrays.asList(length, area, volume, time));
        resource.getContents().remove(velocity);

        // test cleanup
        Assert.assertFalse(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("m").build()).isPresent());
        Assert.assertFalse(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("m²").build()).isPresent());
        Assert.assertFalse(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("s").build()).isPresent());
        Assert.assertFalse(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("m/s").build()).isPresent());
        Assert.assertFalse(measureAdapter.getDimension(newMeasuredDecimalBuilder().withValue(BigDecimal.ONE).withUnitName("dm³").build()).isPresent());
    }

    private MeasureAdapter.MeasureId measureIdFrom(final Measure measure) {
        return MeasureAdapter.MeasureId.fromMeasure(measureProvider, measure);
    }
}
