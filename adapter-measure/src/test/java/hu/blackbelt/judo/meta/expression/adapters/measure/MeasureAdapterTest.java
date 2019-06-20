package hu.blackbelt.judo.meta.expression.adapters.measure;

import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.model.Measure;
import hu.blackbelt.judo.meta.expression.adapters.measure.model.Unit;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.operator.DecimalOperator;
import hu.blackbelt.judo.meta.expression.operator.IntegerOperator;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.*;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.*;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.*;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public class MeasureAdapterTest {

    private DummyMeasureProvider measureProvider = new DummyMeasureProvider();
    private MeasureSupport<Object, Unit> measureSupport;
    private ModelAdapter modelAdapter;
    private MeasureAdapter<Measure, Unit, ?> measureAdapter;

    @BeforeEach
    public void setUp() {
        modelAdapter = mock(ModelAdapter.class);
        measureSupport = mock(MeasureSupport.class);

        measureAdapter = new MeasureAdapter(measureProvider, measureSupport, modelAdapter);
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
        expected.put(Collections.singletonMap(measureIdFrom(measureProvider.getMass()), 1),measureIdFrom(measureProvider.getMass()));
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
                        .withReferenceName("self")
                        .build())
                .withAttributeName("weight")
                .build();
        final NumericExpression daysLeftToShip = newIntegerAttributeBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(orderItemInstance)
                        .withReferenceName("self")
                        .build())
                .withAttributeName("daysLeftToShip")
                .build();
        final NumericExpression quantity = newIntegerAttributeBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(orderItemInstance)
                        .withReferenceName("self")
                        .build())
                .withAttributeName("quantity")
                .build();

        final Object person = new Object();
        final Object orderItem = new Object();

        when(modelAdapter.get(personType)).thenReturn(Optional.of(person));
        when(modelAdapter.get(orderItemType)).thenReturn(Optional.of(orderItem));

        when(measureSupport.getUnit(person, "weight"))
                .thenReturn(measureProvider.getMass().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny());
        when(measureSupport.getUnit(orderItem, "daysLeftToShip"))
                .thenReturn(measureProvider.getTime().getUnits().parallelStream().filter(u -> Objects.equals(u.getName(), "day")).findAny());
        when(measureSupport.getUnit(orderItem, "quantity"))
                .thenReturn(Optional.empty());

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
        when(measureSupport.getUnit(pkg, "weight"))
                .thenReturn(measureProvider.getMass().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny());
        when(measureSupport.getUnit(pkg, "height"))
                .thenReturn(measureProvider.getLength().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "cm")).findAny());
        when(measureSupport.getUnit(pkg, "width"))
                .thenReturn(measureProvider.getLength().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "cm")).findAny());
        when(measureSupport.getUnit(pkg, "length"))
                .thenReturn(measureProvider.getLength().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "cm")).findAny());

        final NumericExpression addition = newIntegerAritmeticExpressionBuilder()
                .withLeft(newIntegerAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .withReferenceName("self")
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
                                .withReferenceName("self")
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
                                .withReferenceName("self")
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
                                .withReferenceName("self")
                                .build())
                        .withAttributeName("height")
                        .build())
                .withOperator(IntegerOperator.MULTIPLY)
                .withRight(newIntegerAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .withReferenceName("self")
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
                                .withReferenceName("self")
                                .build())
                        .withAttributeName("length")
                        .build())
                .withOperator(IntegerOperator.MULTIPLY)
                .withRight(newIntegerAritmeticExpressionBuilder()
                        .withLeft(newIntegerAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(packageInstance)
                                        .withReferenceName("self")
                                        .build())
                                .withAttributeName("height")
                                .build())
                        .withOperator(IntegerOperator.MULTIPLY)
                        .withRight(newIntegerAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(packageInstance)
                                        .withReferenceName("self")
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
                                .withReferenceName("self")
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
                                .withReferenceName("self")
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
                                .withReferenceName("self")
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
                                .withReferenceName("self")
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
                                .withReferenceName("self")
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
        when(measureSupport.getUnit(pkg, "weight"))
                .thenReturn(measureProvider.getMass().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny());
        when(measureSupport.getUnit(pkg, "height"))
                .thenReturn(measureProvider.getLength().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "cm")).findAny());
        when(measureSupport.getUnit(pkg, "width"))
                .thenReturn(measureProvider.getLength().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "cm")).findAny());
        when(measureSupport.getUnit(pkg, "length"))
                .thenReturn(measureProvider.getLength().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "cm")).findAny());

        final NumericExpression addition = newDecimalAritmeticExpressionBuilder()
                .withLeft(newDecimalAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .withReferenceName("self")
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
                                .withReferenceName("self")
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
                                .withReferenceName("self")
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
                                .withReferenceName("self")
                                .build())
                        .withAttributeName("height")
                        .build())
                .withOperator(DecimalOperator.MULTIPLY)
                .withRight(newIntegerAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(packageInstance)
                                .withReferenceName("self")
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
                                .withReferenceName("self")
                                .build())
                        .withAttributeName("length")
                        .build())
                .withOperator(DecimalOperator.MULTIPLY)
                .withRight(newDecimalAritmeticExpressionBuilder()
                        .withLeft(newIntegerAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(packageInstance)
                                        .withReferenceName("self")
                                        .build())
                                .withAttributeName("height")
                                .build())
                        .withOperator(DecimalOperator.MULTIPLY)
                        .withRight(newIntegerAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(packageInstance)
                                        .withReferenceName("self")
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
                                .withReferenceName("self")
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
                                .withReferenceName("self")
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
                                .withReferenceName("self")
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
                                .withReferenceName("self")
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

    public void testIsMeasuredIntegerAggregatedExpression() {
        // TODO
    }

    public void testIsMeasuredDecimalAggregatedExpression() {
        // TODO
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

    public void testIsMeasuredIntegerSwitchExpression() {
        // TODO
    }

    public void testIsMeasuredDecimalSwitchExpression() {
        // TODO
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

    @Test
    public void testGetUnitOfNumericAttribute() {
        final TypeName personType = newTypeNameBuilder().withNamespace("dummy").withName("Person").build();

        final Instance personInstance = newInstanceBuilder().withElementName(personType).build();

        final NumericExpression weight = newDecimalAttributeBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(personInstance)
                        .withReferenceName("self")
                        .build())
                .withAttributeName("weight")
                .build();

        final Object person = new Object();

        when(modelAdapter.get(personType)).thenReturn(Optional.of(person));

        when(measureSupport.getUnit(person, "weight"))
                .thenReturn(measureProvider.getMass().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny());

        final Optional<Unit> unit = measureAdapter.getUnit(weight);
        Assert.assertThat(unit, is(measureProvider.getMass().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny()));
    }

    @Test
    public void testGetUnitOfMeasuredInteger() {
        final NumericExpression numericExpressionWithoutMeasure = newMeasuredIntegerBuilder().withUnitName("kg").withValue(BigInteger.ONE).build();
        Assert.assertThat(measureAdapter.getUnit(numericExpressionWithoutMeasure), is(measureProvider.getMass().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny()));

        final MeasureName massName = newMeasureNameBuilder().withNamespace("base").withName("Mass").build();
        final NumericExpression numericExpressionWithMeasure = newMeasuredIntegerBuilder().withMeasure(massName).withUnitName("kg").withValue(BigInteger.ONE).build();
        Assert.assertThat(measureAdapter.getUnit(numericExpressionWithMeasure), is(measureProvider.getMass().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny()));

        final MeasureName invalidMassName = newMeasureNameBuilder().withNamespace("custom").withName("Mass").build();
        final NumericExpression numericExpressionWithInvalidMeasure = newMeasuredIntegerBuilder().withMeasure(invalidMassName).withUnitName("kg").withValue(BigInteger.ONE).build();
        Assert.assertFalse(measureAdapter.getUnit(numericExpressionWithInvalidMeasure).isPresent());

        final NumericExpression numericExpressionWithInvalidUnit = newMeasuredIntegerBuilder().withUnitName("μg").withValue(BigInteger.ONE).build();
        Assert.assertFalse(measureAdapter.getUnit(numericExpressionWithInvalidUnit).isPresent());
    }

    @Test
    public void testGetUnitOfMeasuredDecimal() {
        final NumericExpression numericExpressionWithoutMeasure = newMeasuredDecimalBuilder().withUnitName("kg").withValue(BigDecimal.ONE).build();
        Assert.assertThat(measureAdapter.getUnit(numericExpressionWithoutMeasure), is(measureProvider.getMass().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny()));

        final MeasureName massName = newMeasureNameBuilder().withNamespace("base").withName("Mass").build();
        final NumericExpression numericExpressionWithMeasure = newMeasuredDecimalBuilder().withMeasure(massName).withUnitName("kg").withValue(BigDecimal.ONE).build();
        Assert.assertThat(measureAdapter.getUnit(numericExpressionWithMeasure), is(measureProvider.getMass().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny()));

        final MeasureName invalidMassName = newMeasureNameBuilder().withNamespace("custom").withName("Mass").build();
        final NumericExpression numericExpressionWithInvalidMeasure = newMeasuredDecimalBuilder().withMeasure(invalidMassName).withUnitName("kg").withValue(BigDecimal.ONE).build();
        Assert.assertFalse(measureAdapter.getUnit(numericExpressionWithInvalidMeasure).isPresent());

        final NumericExpression numericExpressionWithInvalidUnit = newMeasuredDecimalBuilder().withUnitName("μg").withValue(BigDecimal.ONE).build();
        Assert.assertFalse(measureAdapter.getUnit(numericExpressionWithInvalidUnit).isPresent());
    }

    @Test
    public void testGetUnitOfNonMeasured() {
        final NumericExpression numericExpressionWithoutMeasure = newIntegerConstantBuilder().withValue(BigInteger.ONE.ONE).build();
        Assert.assertFalse(measureAdapter.getUnit(numericExpressionWithoutMeasure).isPresent());
    }

    private MeasureAdapter.MeasureId measureIdFrom(final Measure measure) {
        return MeasureAdapter.MeasureId.fromMeasure(measureProvider, measure);
    }
}
