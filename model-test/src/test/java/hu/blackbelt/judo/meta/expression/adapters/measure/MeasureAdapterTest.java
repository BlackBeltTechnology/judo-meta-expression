package hu.blackbelt.judo.meta.expression.adapters.measure;

import hu.blackbelt.judo.meta.expression.CollectionExpression;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.model.Measure;
import hu.blackbelt.judo.meta.expression.adapters.measure.model.Unit;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal;
import hu.blackbelt.judo.meta.expression.constant.MeasuredInteger;
import hu.blackbelt.judo.meta.expression.numeric.NumericAttribute;
import hu.blackbelt.judo.meta.expression.operator.DecimalOperator;
import hu.blackbelt.judo.meta.expression.operator.IntegerOperator;
import hu.blackbelt.judo.meta.expression.operator.NumericComparator;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newImmutableCollectionBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newDecimalComparisonBuilder;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newIntegerComparisonBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.*;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampDifferenceExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MeasureAdapterTest {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(MeasureAdapterTest.class);
    private DummyMeasureProvider measureProvider = new DummyMeasureProvider();
    private ModelAdapter modelAdapter;
    private MeasureAdapter<Measure, Unit, ?> measureAdapter;

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
            } else if (numericExpression instanceof MeasuredInteger) {
                final MeasuredInteger measuredInteger = (MeasuredInteger) numericExpression;
                return measureAdapter.getUnit(measuredInteger.getMeasure() != null ? Optional.ofNullable(measuredInteger.getMeasure().getNamespace()) : Optional.empty(),
                        measuredInteger.getMeasure() != null ? Optional.ofNullable(measuredInteger.getMeasure().getName()) : Optional.empty(),
                        measuredInteger.getUnitName());
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
        Assert.assertTrue(time.isPresent());
        Assert.assertThat(time.get(), is(measureProvider.getTime()));

        final Optional<Measure> length = measureAdapter.get(newMeasureNameBuilder().withNamespace("base").withName("Length").build());
        Assert.assertTrue(length.isPresent());
        Assert.assertThat(length.get(), is(measureProvider.getLength()));

        final Optional<Measure> mass = measureAdapter.get(newMeasureNameBuilder().withNamespace("base").withName("Mass").build());
        Assert.assertTrue(mass.isPresent());

        final Optional<Measure> electricCurrent = measureAdapter.get(newMeasureNameBuilder().withNamespace("base").withName("ElectricCurrent").build());
        Assert.assertFalse(electricCurrent.isPresent());

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
    public void testRefreshDimensions() {
        final Map<Map<MeasureAdapter.MeasureId, Integer>, MeasureAdapter.MeasureId> dimensions = measureAdapter.dimensions;

        Assert.assertThat(dimensions.size(), is(6));

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

        Assert.assertThat(dimensions, is(expected));
    }

    @Test
    public void testIsMeasuredMeasuredInteger() {
        final NumericExpression numericExpressionWithoutMeasure = newMeasuredIntegerBuilder().withUnitName("kg").withValue(BigInteger.ONE).build();
        Assert.assertTrue(measureAdapter.isMeasured(numericExpressionWithoutMeasure));
        Assert.assertThat(measureAdapter.getDimension(numericExpressionWithoutMeasure).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getMass()), 1)));

        final MeasureName massName = newMeasureNameBuilder().withNamespace("base").withName("Mass").build();
        final NumericExpression numericExpressionWithMeasure = newMeasuredIntegerBuilder().withMeasure(massName).withUnitName("kg").withValue(BigInteger.ONE).build();
        Assert.assertTrue(measureAdapter.isMeasured(numericExpressionWithMeasure));
        Assert.assertThat(measureAdapter.getDimension(numericExpressionWithMeasure).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getMass()), 1)));
    }

    @Test
    public void testIsMeasuredMeasuredDecimal() {
        final NumericExpression numericExpressionWithoutMeasure = newMeasuredDecimalBuilder().withUnitName("kg").withValue(BigDecimal.ONE).build();
        Assert.assertTrue(measureAdapter.isMeasured(numericExpressionWithoutMeasure));
        Assert.assertThat(measureAdapter.getDimension(numericExpressionWithoutMeasure).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getMass()), 1)));

        final MeasureName massName = newMeasureNameBuilder().withNamespace("base").withName("Mass").build();
        final NumericExpression numericExpressionWithMeasure = newMeasuredIntegerBuilder().withMeasure(massName).withUnitName("kg").withValue(BigInteger.ONE).build();
        Assert.assertTrue(measureAdapter.isMeasured(numericExpressionWithMeasure));
        Assert.assertThat(measureAdapter.getDimension(numericExpressionWithMeasure).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getMass()), 1)));
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

        attributeUnits.put("weight", measureProvider.getMass().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny().get());
        attributeUnits.put("daysLeftToShip", measureProvider.getTime().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "d")).findAny().get());

        Assert.assertTrue(measureAdapter.isMeasured(weight));
        Assert.assertTrue(measureAdapter.isMeasured(daysLeftToShip));
        Assert.assertFalse(measureAdapter.isMeasured(quantity));
    }

    @Test
    public void testIsMeasuredIntegerAritmeticExpression() {
        final TypeName packageType = newTypeNameBuilder().withNamespace("dummy").withName("Package").build();
        final Instance packageInstance = newInstanceBuilder().withElementName(packageType).build();
        final Object pkg = new Object();

        when(modelAdapter.get(packageType)).thenReturn(Optional.of(pkg));
        attributeUnits.put("weight", measureProvider.getMass().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny().get());
        attributeUnits.put("height", measureProvider.getLength().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "cm")).findAny().get());
        attributeUnits.put("width", measureProvider.getLength().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "cm")).findAny().get());
        attributeUnits.put("length", measureProvider.getLength().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "cm")).findAny().get());

        final NumericExpression addition = newIntegerAritmeticExpressionBuilder()
                .withLeft(newIntegerAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("weight")
                        .build())
                .withOperator(IntegerOperator.ADD)
                .withRight(newMeasuredIntegerBuilder()
                        .withValue(BigInteger.ONE)
                        .withUnitName("dkg")
                        .build())
                .build();

        Assert.assertTrue(measureAdapter.isMeasured(addition));
        Assert.assertThat(measureAdapter.getDimension(addition).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getMass()), 1)));

        final NumericExpression subtraction = newIntegerAritmeticExpressionBuilder()
                .withLeft(newIntegerAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("weight")
                        .build())
                .withOperator(IntegerOperator.SUBSTRACT)
                .withRight(newMeasuredIntegerBuilder()
                        .withValue(BigInteger.ONE)
                        .withUnitName("g")
                        .build())
                .build();

        Assert.assertTrue(measureAdapter.isMeasured(subtraction));
        Assert.assertThat(measureAdapter.getDimension(subtraction).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getMass()), 1)));

        final NumericExpression multiplicationWithScalar = newIntegerAritmeticExpressionBuilder()
                .withLeft(newIntegerAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("height")
                        .build())
                .withOperator(IntegerOperator.MULTIPLY)
                .withRight(newIntegerConstantBuilder()
                        .withValue(BigInteger.TEN)
                        .build())
                .build();

        Assert.assertTrue(measureAdapter.isMeasured(multiplicationWithScalar));
        Assert.assertThat(measureAdapter.getDimension(multiplicationWithScalar).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 1)));

        final NumericExpression multiplication = newIntegerAritmeticExpressionBuilder()
                .withLeft(newIntegerAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("height")
                        .build())
                .withOperator(IntegerOperator.MULTIPLY)
                .withRight(newIntegerAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("width")
                        .build())
                .build();

        Assert.assertTrue(measureAdapter.isMeasured(multiplication));
        Assert.assertThat(measureAdapter.getDimension(multiplication).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 2)));

        final NumericExpression multiplication2 = newIntegerAritmeticExpressionBuilder()
                .withLeft(newIntegerAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("length")
                        .build())
                .withOperator(IntegerOperator.MULTIPLY)
                .withRight(newIntegerAritmeticExpressionBuilder()
                        .withLeft(newIntegerAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(packageInstance)
                                        .build())
                                .withAttributeName("height")
                                .build())
                        .withOperator(IntegerOperator.MULTIPLY)
                        .withRight(newIntegerAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(packageInstance)
                                        .build())
                                .withAttributeName("width")
                                .build())
                        .build())
                .build();

        Assert.assertTrue(measureAdapter.isMeasured(multiplication2));
        Assert.assertThat(measureAdapter.getDimension(multiplication2).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 3)));

        final NumericExpression divisionByScalar = newIntegerAritmeticExpressionBuilder()
                .withLeft(newIntegerAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("height")
                        .build())
                .withOperator(IntegerOperator.DIVIDE)
                .withRight(newIntegerConstantBuilder()
                        .withValue(BigInteger.ONE)
                        .build())
                .build();
        final NumericExpression reciprocal = newIntegerAritmeticExpressionBuilder()
                .withLeft(newIntegerConstantBuilder()
                        .withValue(BigInteger.ONE)
                        .build())
                .withOperator(IntegerOperator.DIVIDE)
                .withRight(newIntegerAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("height")
                        .build())
                .build();

        Assert.assertTrue(measureAdapter.isMeasured(divisionByScalar));
        Assert.assertThat(measureAdapter.getDimension(divisionByScalar).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 1)));
        Assert.assertTrue(measureAdapter.isMeasured(reciprocal));
        Assert.assertThat(measureAdapter.getDimension(reciprocal).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), -1)));

        final NumericExpression modulo = newIntegerAritmeticExpressionBuilder()
                .withLeft(newIntegerAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("height")
                        .build())
                .withOperator(IntegerOperator.MODULO)
                .withRight(newMeasuredIntegerBuilder()
                        .withValue(BigInteger.TEN)
                        .withUnitName("m")
                        .build())
                .build();

        Assert.assertTrue(measureAdapter.isMeasured(modulo));
        Assert.assertThat(measureAdapter.getDimension(modulo).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 1)));

        final NumericExpression division1 = newIntegerAritmeticExpressionBuilder()
                .withLeft(newMeasuredIntegerBuilder()
                        .withValue(BigInteger.valueOf(1000))
                        .withUnitName("cm²")
                        .build())
                .withOperator(IntegerOperator.DIVIDE)
                .withRight(newIntegerAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("height")
                        .build())
                .build();

        Assert.assertTrue(measureAdapter.isMeasured(division1));
        Assert.assertThat(measureAdapter.getDimension(division1).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 1)));

        final NumericExpression division2 = newIntegerAritmeticExpressionBuilder()
                .withLeft(newIntegerAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("height")
                        .build())
                .withOperator(IntegerOperator.DIVIDE)
                .withRight(newMeasuredIntegerBuilder()
                        .withValue(BigInteger.ONE)
                        .withUnitName("cm")
                        .build())
                .build();

        Assert.assertFalse(measureAdapter.isMeasured(division2));
        Assert.assertThat(measureAdapter.getDimension(division2).get(), is(Collections.emptyMap()));
    }

    @Test
    public void testIsMeasuredDecimalAritmeticExpression() {
        final TypeName packageType = newTypeNameBuilder().withNamespace("dummy").withName("Package").build();
        final Instance packageInstance = newInstanceBuilder().withElementName(packageType).build();
        final Object pkg = new Object();

        when(modelAdapter.get(packageType)).thenReturn(Optional.of(pkg));
        attributeUnits.put("weight", measureProvider.getMass().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny().get());
        attributeUnits.put("height", measureProvider.getLength().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "cm")).findAny().get());
        attributeUnits.put("width", measureProvider.getLength().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "cm")).findAny().get());
        attributeUnits.put("length", measureProvider.getLength().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "cm")).findAny().get());

        final NumericExpression addition = newDecimalAritmeticExpressionBuilder()
                .withLeft(newDecimalAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("weight")
                        .build())
                .withOperator(DecimalOperator.ADD)
                .withRight(newMeasuredIntegerBuilder()
                        .withValue(BigInteger.ONE)
                        .withUnitName("dkg")
                        .build())
                .build();

        Assert.assertTrue(measureAdapter.isMeasured(addition));
        Assert.assertThat(measureAdapter.getDimension(addition).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getMass()), 1)));

        final NumericExpression subtraction = newDecimalAritmeticExpressionBuilder()
                .withLeft(newDecimalAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("weight")
                        .build())
                .withOperator(DecimalOperator.SUBSTRACT)
                .withRight(newMeasuredIntegerBuilder()
                        .withValue(BigInteger.ONE)
                        .withUnitName("g")
                        .build())
                .build();

        Assert.assertTrue(measureAdapter.isMeasured(subtraction));
        Assert.assertThat(measureAdapter.getDimension(subtraction).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getMass()), 1)));

        final NumericExpression multiplicationWithScalar = newDecimalAritmeticExpressionBuilder()
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

        Assert.assertTrue(measureAdapter.isMeasured(multiplicationWithScalar));
        Assert.assertThat(measureAdapter.getDimension(multiplicationWithScalar).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 1)));

        final NumericExpression multiplication = newDecimalAritmeticExpressionBuilder()
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

        Assert.assertTrue(measureAdapter.isMeasured(multiplication));
        Assert.assertThat(measureAdapter.getDimension(multiplication).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 2)));

        final NumericExpression multiplication2 = newDecimalAritmeticExpressionBuilder()
                .withLeft(newDecimalAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("length")
                        .build())
                .withOperator(DecimalOperator.MULTIPLY)
                .withRight(newDecimalAritmeticExpressionBuilder()
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

        Assert.assertTrue(measureAdapter.isMeasured(multiplication2));
        Assert.assertThat(measureAdapter.getDimension(multiplication2).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 3)));

        final NumericExpression divisionByScalar = newDecimalAritmeticExpressionBuilder()
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
        final NumericExpression reciprocal = newDecimalAritmeticExpressionBuilder()
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

        Assert.assertTrue(measureAdapter.isMeasured(divisionByScalar));
        Assert.assertThat(measureAdapter.getDimension(divisionByScalar).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 1)));
        Assert.assertTrue(measureAdapter.isMeasured(reciprocal));
        Assert.assertThat(measureAdapter.getDimension(reciprocal).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), -1)));

        final NumericExpression division1 = newDecimalAritmeticExpressionBuilder()
                .withLeft(newMeasuredIntegerBuilder()
                        .withValue(BigInteger.valueOf(1000))
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

        Assert.assertTrue(measureAdapter.isMeasured(division1));
        Assert.assertThat(measureAdapter.getDimension(division1).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 1)));

        final NumericExpression division2 = newDecimalAritmeticExpressionBuilder()
                .withLeft(newDecimalAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("height")
                        .build())
                .withOperator(DecimalOperator.DIVIDE)
                .withRight(newMeasuredIntegerBuilder()
                        .withValue(BigInteger.ONE)
                        .withUnitName("cm")
                        .build())
                .build();

        Assert.assertFalse(measureAdapter.isMeasured(division2));
        Assert.assertThat(measureAdapter.getDimension(division2).get(), is(Collections.emptyMap()));
    }

    @Test
    public void testIsMeasuredIntegerOppositeExpression() {
        final NumericExpression scalarOpposite = newIntegerOppositeExpressionBuilder()
                .withExpression(newIntegerConstantBuilder()
                        .withValue(BigInteger.ONE)
                        .build())
                .build();
        final NumericExpression measuredOpposite = newIntegerOppositeExpressionBuilder()
                .withExpression(newMeasuredIntegerBuilder()
                        .withValue(BigInteger.TEN)
                        .withUnitName("m")
                        .build())
                .build();

        Assert.assertFalse(measureAdapter.isMeasured(scalarOpposite));
        Assert.assertThat(measureAdapter.getDimension(scalarOpposite).get(), is(Collections.emptyMap()));

        Assert.assertTrue(measureAdapter.isMeasured(measuredOpposite));
        Assert.assertThat(measureAdapter.getDimension(measuredOpposite).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 1)));
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

        Assert.assertFalse(measureAdapter.isMeasured(scalarOpposite));
        Assert.assertThat(measureAdapter.getDimension(scalarOpposite).get(), is(Collections.emptyMap()));

        Assert.assertTrue(measureAdapter.isMeasured(measuredOpposite));
        Assert.assertThat(measureAdapter.getDimension(measuredOpposite).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 1)));
    }

    @Test
    public void testIsMeasuredIntegerAggregatedExpression() {
        final TypeName packageType = newTypeNameBuilder().withNamespace("dummy").withName("Package").build();
        final Object pkg = new Object();

        when(modelAdapter.get(packageType)).thenReturn(Optional.of(pkg));
        attributeUnits.put("weight", measureProvider.getMass().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny().get());

        final Instance packageInstance = newInstanceBuilder().withElementName(packageType).build();
        final CollectionExpression allPackages = newImmutableCollectionBuilder()
                .withElementName(packageType)
                .withIteratorVariable(packageInstance)
                .build();

        final NumericExpression sum = newIntegerAggregatedExpressionBuilder()
                .withCollectionExpression(allPackages)
                .withExpression(newIntegerAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .build())
                        .withAttributeName("weight")
                        .build())
                .build();

        Assert.assertTrue(measureAdapter.isMeasured(sum));
        Assert.assertThat(measureAdapter.getDimension(sum).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getMass()), 1)));
    }

    @Test
    public void testIsMeasuredDecimalAggregatedExpression() {
        final TypeName packageType = newTypeNameBuilder().withNamespace("dummy").withName("Package").build();
        final Object pkg = new Object();

        when(modelAdapter.get(packageType)).thenReturn(Optional.of(pkg));
        attributeUnits.put("weight", measureProvider.getMass().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny().get());

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

        Assert.assertTrue(measureAdapter.isMeasured(sum));
        Assert.assertThat(measureAdapter.getDimension(sum).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getMass()), 1)));
    }

    @Test
    public void testIsMeasuredRoundExpression() {
        final NumericExpression scalar = newRoundExpressionBuilder()
                .withExpression(newDecimalConstantBuilder()
                        .withValue(BigDecimal.ONE)
                        .build())
                .build();

        final NumericExpression measured = newRoundExpressionBuilder()
                .withExpression(newMeasuredDecimalBuilder()
                        .withValue(BigDecimal.ONE)
                        .withUnitName("m")
                        .build())
                .build();

        Assert.assertFalse(measureAdapter.isMeasured(scalar));
        Assert.assertThat(measureAdapter.getDimension(scalar).get(), is(Collections.emptyMap()));

        Assert.assertTrue(measureAdapter.isMeasured(measured));
        Assert.assertThat(measureAdapter.getDimension(measured).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 1)));
    }

    @Test
    public void testIsMeasuredIntegerSwitchExpression() {
        final TypeName packageType = newTypeNameBuilder().withNamespace("dummy").withName("Object").build();
        final Object pkg = new Object();

        when(modelAdapter.get(packageType)).thenReturn(Optional.of(pkg));
        attributeUnits.put("a", measureProvider.getLength().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "m")).findAny().get());
        attributeUnits.put("b", measureProvider.getLength().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "cm")).findAny().get());

        final Instance packageInstance = newInstanceBuilder().withElementName(packageType).build();

        final NumericExpression min = newIntegerSwitchExpressionBuilder()
                .withCases(newSwitchCaseBuilder()
                        .withCondition(newIntegerComparisonBuilder()
                                .withLeft(newIntegerAttributeBuilder()
                                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                                .withVariable(packageInstance)
                                                .build())
                                        .withAttributeName("a")
                                        .build())
                                .withOperator(NumericComparator.LESS_THAN)
                                .withRight(newIntegerAttributeBuilder()
                                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                                .withVariable(packageInstance)
                                                .build())
                                        .withAttributeName("b")
                                        .build())
                                .build())
                        .withExpression(newIntegerAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(packageInstance)
                                        .build())
                                .withAttributeName("a")
                                .build()))
                .withDefaultExpression(
                        newIntegerAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(packageInstance)
                                        .build())
                                .withAttributeName("b")
                                .build())
                .build();

        Assert.assertTrue(measureAdapter.isMeasured(min));
        Assert.assertThat(measureAdapter.getDimension(min).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 1)));
    }

    @Test
    public void testIsMeasuredDecimalSwitchExpression() {
        final TypeName packageType = newTypeNameBuilder().withNamespace("dummy").withName("Object").build();
        final Object pkg = new Object();

        when(modelAdapter.get(packageType)).thenReturn(Optional.of(pkg));
        attributeUnits.put("a", measureProvider.getLength().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "m")).findAny().get());
        attributeUnits.put("b", measureProvider.getLength().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "cm")).findAny().get());

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

        Assert.assertTrue(measureAdapter.isMeasured(min));
        Assert.assertThat(measureAdapter.getDimension(min).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getLength()), 1)));
    }

    @Test
    public void testIsMeasuredTimestampDifferenceExpression() {
        final NumericExpression duration = newTimestampDifferenceExpressionBuilder()
                .withStartTimestamp(newTimestampConstantBuilder()
                        .withValue("2019-01-01 12:00:00Z")
                        .build())
                .withEndTimestamp(newTimestampConstantBuilder()
                        .withValue("2019-01-01 13:00:00Z")
                        .build())
                .build();

        Assert.assertTrue(measureAdapter.isMeasured(duration));
        Assert.assertThat(measureAdapter.getDimension(duration).get(), is(Collections.singletonMap(measureIdFrom(measureProvider.getTime()), 1)));
    }

    @Test
    public void testIsMeasuredNonMeasuredConstant() {
        final NumericExpression integerExpression = newIntegerConstantBuilder().withValue(BigInteger.ONE).build();
        Assert.assertFalse(measureAdapter.isMeasured(integerExpression));
        Assert.assertThat(measureAdapter.getDimension(integerExpression), is(Optional.of(Collections.emptyMap())));

        final NumericExpression decimalExpression = newDecimalConstantBuilder().withValue(BigDecimal.ONE).build();
        Assert.assertFalse(measureAdapter.isMeasured(decimalExpression));
        Assert.assertThat(measureAdapter.getDimension(decimalExpression), is(Optional.of(Collections.emptyMap())));
    }

    //TODO kieg: perc & méter as 'm', egyikkel táv-e, másikkal idő-e (measure név is) (op- a: min b: meter) //getName(..)
    @Test
    public void testGetUnit() {
        //Assert.assertThat(measureAdapter.getUnit());
        Assert.assertThat(measureAdapter.getUnit(Optional.empty(), Optional.empty(), "kg"), is(measureProvider.getMass().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny()));
        Assert.assertThat(measureAdapter.getUnit(Optional.of("base"), Optional.of("Mass"), "kg"), is(measureProvider.getMass().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny()));
        Assert.assertFalse(measureAdapter.getUnit(Optional.of("custom"), Optional.of("Mass"), "kg").isPresent());
        Assert.assertFalse(measureAdapter.getUnit(Optional.empty(), Optional.empty(), "μg").isPresent());
    }

    private MeasureAdapter.MeasureId measureIdFrom(final Measure measure) {
        return MeasureAdapter.MeasureId.fromMeasure(measureProvider, measure);
    }
}
