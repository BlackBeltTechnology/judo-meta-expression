package hu.blackbelt.judo.meta.expression.adapters.measure;

/*-
 * #%L
 * JUDO :: Expression :: Model
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */


import hu.blackbelt.judo.meta.expression.CollectionExpression;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.model.Measure;
import hu.blackbelt.judo.meta.expression.adapters.measure.model.Unit;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal;
import hu.blackbelt.judo.meta.expression.numeric.NumericAttribute;
import hu.blackbelt.judo.meta.expression.operator.DecimalOperator;
import hu.blackbelt.judo.meta.expression.operator.NumericComparator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newImmutableCollectionBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newDecimalComparisonBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.*;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampDifferenceExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MeasureAdapterTest {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(MeasureAdapterTest.class);
    private DummyMeasureProvider measureProvider = new DummyMeasureProvider();
    private ModelAdapter modelAdapter;
    private MeasureAdapter<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, Measure, Unit> measureAdapter;

    private final Map<String, Unit> attributeUnits = new TreeMap<>();

    @BeforeEach
    public void setUp() {
        modelAdapter = mock(ModelAdapter.class);

        Mockito.doAnswer(invocationOnMock -> {
            final Object[] args = invocationOnMock.getArguments();
            final NumericExpression numericExpression = (NumericExpression) args[0];

            if (numericExpression instanceof NumericAttribute) {
                final String attributeName = ((NumericAttribute) numericExpression).getAttributeName();
                return Optional.ofNullable(attributeUnits.get(attributeName));
            } else if (numericExpression instanceof MeasuredDecimal) {
                final MeasuredDecimal measuredDecimal = (MeasuredDecimal) numericExpression;
                return measureAdapter.getUnit(measuredDecimal.getMeasure() != null ? Optional.ofNullable(measuredDecimal.getMeasure().getNamespace()) : Optional.empty(),
                        measuredDecimal.getMeasure() != null ? Optional.ofNullable(measuredDecimal.getMeasure().getName()) : Optional.empty(),
                        measuredDecimal.getUnitName());
            } else {
                return Optional.empty();
            }
        }).when(modelAdapter).getUnit(any(NumericExpression.class));

        measureAdapter = new MeasureAdapter(measureProvider, modelAdapter);
    }

    @AfterEach
    public void tearDown() {
        measureAdapter = null;
        attributeUnits.clear();
    }

    @Test
    public void testGet() {
        final Optional<Measure> time = measureAdapter.get(newMeasureNameBuilder().withNamespace("system").withName("Time").build());
        assertTrue(time.isPresent());
        assertThat(time.get(), is(measureProvider.getTime()));

        final Optional<Measure> length = measureAdapter.get(newMeasureNameBuilder().withNamespace("base").withName("Length").build());
        assertTrue(length.isPresent());
        assertThat(length.get(), is(measureProvider.getLength()));

        final Optional<Measure> mass = measureAdapter.get(newMeasureNameBuilder().withNamespace("base").withName("Mass").build());
        assertTrue(mass.isPresent());

        final Optional<Measure> electricCurrent = measureAdapter.get(newMeasureNameBuilder().withNamespace("base").withName("ElectricCurrent").build());
        assertFalse(electricCurrent.isPresent());

        final Optional<Measure> velocity = measureAdapter.get(newMeasureNameBuilder().withNamespace("derived").withName("Velocity").build());
        assertTrue(velocity.isPresent());
        assertThat(velocity.get(), is(measureProvider.getVelocity()));
    }

    @Test
    public void testGetDurationMeasure() {
        final Optional<Measure> time = measureAdapter.getDurationMeasure();
        assertTrue(time.isPresent());
        assertThat(time.get(), is(measureProvider.getTime()));
    }

    @Test
    public void testRefreshDimensions() {
        final Map<Map<MeasureAdapter.MeasureId, Integer>, MeasureAdapter.MeasureId> dimensions = measureAdapter.dimensions;

        assertThat(dimensions.size(), is(6));

        final Map<Map<MeasureAdapter.MeasureId, Integer>, MeasureAdapter.MeasureId> expected = new HashMap<>();


        expected.put(Collections.singletonMap(measureIdFrom(measureProvider.getTime()), 1), measureIdFrom(measureProvider.getTime()));
        expected.put(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 1), measureIdFrom(measureProvider.getLength()));
        expected.put(Collections.singletonMap(measureIdFrom(measureProvider.getMass()), 1), measureIdFrom(measureProvider.getMass()));
        expected.put(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 2), measureIdFrom(measureProvider.getArea()));
        expected.put(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 3), measureIdFrom(measureProvider.getVolume()));

        final Map<MeasureAdapter.MeasureId, Integer> velocityBase = new HashMap<>();
        velocityBase.put(measureIdFrom(measureProvider.getLength()), 1);
        velocityBase.put(measureIdFrom(measureProvider.getTime()), -1);
        expected.put(velocityBase, measureIdFrom(measureProvider.getVelocity()));

        assertThat(dimensions, is(expected));
    }

    @Test
    public void testIsMeasuredMeasuredDecimal() {
        final NumericExpression numericExpressionWithoutMeasure = newMeasuredDecimalBuilder().withUnitName("kg").withValue(BigDecimal.ONE).build();
        assertTrue(measureAdapter.isMeasured(numericExpressionWithoutMeasure));
        assertThat(measureAdapter.getDimension(numericExpressionWithoutMeasure).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getMass()), 1)));
    }

    @Test
    public void testIsMeasuredNumericAttribute() {
        final TypeName personType = newTypeNameBuilder().withNamespace("dummy").withName("Person").build();
        final TypeName orderItemType = newTypeNameBuilder().withNamespace("dummy").withName("OrderItem").build();

        final Instance personInstance = newInstanceBuilder().withElementName(personType).build();
        final Instance orderItemInstance = newInstanceBuilder().withElementName(orderItemType).build();

        final NumericExpression weight = newDecimalAttributeBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(personInstance)
                        .build())
                .withAttributeName("weight")
                .build();
        final NumericExpression daysLeftToShip = newIntegerAttributeBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(orderItemInstance)
                        .build())
                .withAttributeName("daysLeftToShip")
                .build();
        final NumericExpression quantity = newIntegerAttributeBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(orderItemInstance)
                        .build())
                .withAttributeName("quantity")
                .build();

        final Object person = new Object();
        final Object orderItem = new Object();

        when(modelAdapter.get(personType)).thenReturn(Optional.of(person));
        when(modelAdapter.get(orderItemType)).thenReturn(Optional.of(orderItem));

        attributeUnits.put("weight", measureProvider.getMass().getUnits().stream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny().get());
        attributeUnits.put("daysLeftToShip", measureProvider.getTime().getUnits().stream().filter(u -> Objects.equals(u.getSymbol(), "d")).findAny().get());

        assertTrue(measureAdapter.isMeasured(weight));
        assertTrue(measureAdapter.isMeasured(daysLeftToShip));
        assertFalse(measureAdapter.isMeasured(quantity));
    }

    @Test
    public void testIsMeasuredDecimalArithmeticExpression() {
        final TypeName packageType = newTypeNameBuilder().withNamespace("dummy").withName("Package").build();
        final Instance packageInstance = newInstanceBuilder().withElementName(packageType).build();
        final Object pkg = new Object();

        when(modelAdapter.get(packageType)).thenReturn(Optional.of(pkg));
        attributeUnits.put("weight", measureProvider.getMass().getUnits().stream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny().get());
        attributeUnits.put("height", measureProvider.getLength().getUnits().stream().filter(u -> Objects.equals(u.getSymbol(), "cm")).findAny().get());
        attributeUnits.put("width", measureProvider.getLength().getUnits().stream().filter(u -> Objects.equals(u.getSymbol(), "cm")).findAny().get());
        attributeUnits.put("length", measureProvider.getLength().getUnits().stream().filter(u -> Objects.equals(u.getSymbol(), "cm")).findAny().get());

        final NumericExpression addition = newDecimalArithmeticExpressionBuilder()
                .withLeft(newDecimalAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("weight")
                        .build())
                .withOperator(DecimalOperator.ADD)
                .withRight(newMeasuredDecimalBuilder()
                        .withValue(BigDecimal.ONE)
                        .withUnitName("dkg")
                        .build())
                .build();

        assertTrue(measureAdapter.isMeasured(addition));
        assertThat(measureAdapter.getDimension(addition).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getMass()), 1)));

        final NumericExpression subtraction = newDecimalArithmeticExpressionBuilder()
                .withLeft(newDecimalAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("weight")
                        .build())
                .withOperator(DecimalOperator.SUBSTRACT)
                .withRight(newMeasuredDecimalBuilder()
                        .withValue(BigDecimal.ONE)
                        .withUnitName("g")
                        .build())
                .build();

        assertTrue(measureAdapter.isMeasured(subtraction));
        assertThat(measureAdapter.getDimension(subtraction).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getMass()), 1)));

        final NumericExpression multiplicationWithScalar = newDecimalArithmeticExpressionBuilder()
                .withLeft(newDecimalAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("height")
                        .build())
                .withOperator(DecimalOperator.MULTIPLY)
                .withRight(newIntegerConstantBuilder()
                        .withValue(BigInteger.TEN)
                        .build())
                .build();

        assertTrue(measureAdapter.isMeasured(multiplicationWithScalar));
        assertThat(measureAdapter.getDimension(multiplicationWithScalar).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 1)));

        final NumericExpression multiplication = newDecimalArithmeticExpressionBuilder()
                .withLeft(newDecimalAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("height")
                        .build())
                .withOperator(DecimalOperator.MULTIPLY)
                .withRight(newIntegerAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("width")
                        .build())
                .build();

        assertTrue(measureAdapter.isMeasured(multiplication));
        assertThat(measureAdapter.getDimension(multiplication).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 2)));

        final NumericExpression multiplication2 = newDecimalArithmeticExpressionBuilder()
                .withLeft(newDecimalAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("length")
                        .build())
                .withOperator(DecimalOperator.MULTIPLY)
                .withRight(newDecimalArithmeticExpressionBuilder()
                        .withLeft(newIntegerAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(packageInstance)
                                        .build())
                                .withAttributeName("height")
                                .build())
                        .withOperator(DecimalOperator.MULTIPLY)
                        .withRight(newIntegerAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(packageInstance)
                                        .build())
                                .withAttributeName("width")
                                .build())
                        .build())
                .build();

        assertTrue(measureAdapter.isMeasured(multiplication2));
        assertThat(measureAdapter.getDimension(multiplication2).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 3)));

        final NumericExpression divisionByScalar = newDecimalArithmeticExpressionBuilder()
                .withLeft(newDecimalAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("height")
                        .build())
                .withOperator(DecimalOperator.DIVIDE)
                .withRight(newIntegerConstantBuilder()
                        .withValue(BigInteger.ONE)
                        .build())
                .build();
        final NumericExpression reciprocal = newDecimalArithmeticExpressionBuilder()
                .withLeft(newIntegerConstantBuilder()
                        .withValue(BigInteger.ONE)
                        .build())
                .withOperator(DecimalOperator.DIVIDE)
                .withRight(newDecimalAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("height")
                        .build())
                .build();

        assertTrue(measureAdapter.isMeasured(divisionByScalar));
        assertThat(measureAdapter.getDimension(divisionByScalar).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 1)));
        assertTrue(measureAdapter.isMeasured(reciprocal));
        assertThat(measureAdapter.getDimension(reciprocal).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), -1)));

        final NumericExpression division1 = newDecimalArithmeticExpressionBuilder()
                .withLeft(newMeasuredDecimalBuilder()
                        .withValue(BigDecimal.valueOf(1000))
                        .withUnitName("cm²")
                        .build())
                .withOperator(DecimalOperator.DIVIDE)
                .withRight(newDecimalAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("height")
                        .build())
                .build();

        assertTrue(measureAdapter.isMeasured(division1));
        assertThat(measureAdapter.getDimension(division1).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 1)));

        final NumericExpression division2 = newDecimalArithmeticExpressionBuilder()
                .withLeft(newDecimalAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("height")
                        .build())
                .withOperator(DecimalOperator.DIVIDE)
                .withRight(newMeasuredDecimalBuilder()
                        .withValue(BigDecimal.ONE)
                        .withUnitName("cm")
                        .build())
                .build();

        assertFalse(measureAdapter.isMeasured(division2));
        assertThat(measureAdapter.getDimension(division2).get(), is(Collections.emptyMap()));
    }

    @Test
    public void testIsMeasuredDecimalOppositeExpression() {
        final NumericExpression scalarOpposite = newDecimalOppositeExpressionBuilder()
                .withExpression(newDecimalConstantBuilder()
                        .withValue(BigDecimal.ONE)
                        .build())
                .build();
        final NumericExpression measuredOpposite = newDecimalOppositeExpressionBuilder()
                .withExpression(newMeasuredDecimalBuilder()
                        .withValue(BigDecimal.TEN)
                        .withUnitName("m")
                        .build())
                .build();

        assertFalse(measureAdapter.isMeasured(scalarOpposite));
        assertThat(measureAdapter.getDimension(scalarOpposite).get(), is(Collections.emptyMap()));

        assertTrue(measureAdapter.isMeasured(measuredOpposite));
        assertThat(measureAdapter.getDimension(measuredOpposite).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 1)));
    }

    @Test
    public void testIsMeasuredIntegerAggregatedExpression() {
        final TypeName packageType = newTypeNameBuilder().withNamespace("dummy").withName("Package").build();
        final Object pkg = new Object();

        when(modelAdapter.get(packageType)).thenReturn(Optional.of(pkg));
        attributeUnits.put("weight", measureProvider.getMass().getUnits().stream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny().get());

        final Instance packageInstance = newInstanceBuilder().withElementName(packageType).build();
        final CollectionExpression allPackages = newImmutableCollectionBuilder()
                .withElementName(packageType)
                .withIteratorVariable(packageInstance)
                .build();

        final NumericExpression sum = newDecimalAggregatedExpressionBuilder()
                .withCollectionExpression(allPackages)
                .withExpression(newIntegerAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("weight")
                        .build())
                .build();

        assertTrue(measureAdapter.isMeasured(sum));
        assertThat(measureAdapter.getDimension(sum).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getMass()), 1)));
    }

    @Test
    public void testIsMeasuredDecimalAggregatedExpression() {
        final TypeName packageType = newTypeNameBuilder().withNamespace("dummy").withName("Package").build();
        final Object pkg = new Object();

        when(modelAdapter.get(packageType)).thenReturn(Optional.of(pkg));
        attributeUnits.put("weight", measureProvider.getMass().getUnits().stream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny().get());

        final Instance packageInstance = newInstanceBuilder().withElementName(packageType).build();
        final CollectionExpression allPackages = newImmutableCollectionBuilder()
                .withElementName(packageType)
                .withIteratorVariable(packageInstance)
                .build();

        final NumericExpression sum = newDecimalAggregatedExpressionBuilder()
                .withCollectionExpression(allPackages)
                .withExpression(newDecimalAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("weight")
                        .build())
                .build();

        assertTrue(measureAdapter.isMeasured(sum));
        assertThat(measureAdapter.getDimension(sum).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getMass()), 1)));
    }

    @Test
    public void testIsMeasuredDecimalSwitchExpression() {
        final TypeName packageType = newTypeNameBuilder().withNamespace("dummy").withName("Object").build();
        final Object pkg = new Object();

        when(modelAdapter.get(packageType)).thenReturn(Optional.of(pkg));
        attributeUnits.put("a", measureProvider.getLength().getUnits().stream().filter(u -> Objects.equals(u.getSymbol(), "m")).findAny().get());
        attributeUnits.put("b", measureProvider.getLength().getUnits().stream().filter(u -> Objects.equals(u.getSymbol(), "cm")).findAny().get());

        final Instance packageInstance = newInstanceBuilder().withElementName(packageType).build();

        final NumericExpression min = newDecimalSwitchExpressionBuilder()
                .withCases(newSwitchCaseBuilder()
                        .withCondition(newDecimalComparisonBuilder()
                                .withLeft(newDecimalAttributeBuilder()
                                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                                .withVariable(packageInstance)
                                                .build())
                                        .withAttributeName("a")
                                        .build())
                                .withOperator(NumericComparator.LESS_THAN)
                                .withRight(newDecimalAttributeBuilder()
                                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                                .withVariable(packageInstance)
                                                .build())
                                        .withAttributeName("b")
                                        .build())
                                .build())
                        .withExpression(newDecimalAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(packageInstance)
                                        .build())
                                .withAttributeName("a")
                                .build()))
                .withDefaultExpression(
                        newDecimalAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(packageInstance)
                                        .build())
                                .withAttributeName("b")
                                .build())
                .build();

        assertTrue(measureAdapter.isMeasured(min));
        assertThat(measureAdapter.getDimension(min).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 1)));
    }

    @Test
    public void testIsMeasuredTimestampDifferenceExpression() {
        final NumericExpression duration = newTimestampDifferenceExpressionBuilder()
                .withStartTimestamp(newTimestampConstantBuilder()
                        .withValue(OffsetDateTime.parse("2019-01-01T12:00:00+00:00").atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime())
                        .build())
                .withEndTimestamp(newTimestampConstantBuilder()
                        .withValue(OffsetDateTime.parse("2019-01-01T12:00:00+00:00").atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime())
                        .build())
                .build();

        assertTrue(measureAdapter.isMeasured(duration));
        assertThat(measureAdapter.getDimension(duration).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getTime()), 1)));
    }

    @Test
    public void testIsMeasuredNonMeasuredConstant() {
        final NumericExpression integerExpression = newIntegerConstantBuilder().withValue(BigInteger.ONE).build();
        assertFalse(measureAdapter.isMeasured(integerExpression));
        assertThat(measureAdapter.getDimension(integerExpression), is(Optional.of(Collections.emptyMap())));

        final NumericExpression decimalExpression = newDecimalConstantBuilder().withValue(BigDecimal.ONE).build();
        assertFalse(measureAdapter.isMeasured(decimalExpression));
        assertThat(measureAdapter.getDimension(decimalExpression), is(Optional.of(Collections.emptyMap())));
    }

    //TODO kieg: perc & méter as 'm', egyikkel táv-e, másikkal idő-e (measure név is) (op- a: min b: meter) //getName(..)
    @Test
    public void testGetUnit() {
        //Assert.assertThat(measureAdapter.getUnit());
        assertThat(measureAdapter.getUnit(Optional.empty(), Optional.empty(), "kg"), is(measureProvider.getMass().getUnits().stream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny()));
        assertThat(measureAdapter.getUnit(Optional.of("base"), Optional.of("Mass"), "kg"), is(measureProvider.getMass().getUnits().stream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny()));
        assertFalse(measureAdapter.getUnit(Optional.of("custom"), Optional.of("Mass"), "kg").isPresent());
        assertFalse(measureAdapter.getUnit(Optional.empty(), Optional.empty(), "μg").isPresent());
    }

    private MeasureAdapter.MeasureId measureIdFrom(final Measure measure) {
        return MeasureAdapter.MeasureId.fromMeasure(measureProvider, measure);
    }
}
