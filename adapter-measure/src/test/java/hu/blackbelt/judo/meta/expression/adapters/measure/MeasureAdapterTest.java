package hu.blackbelt.judo.meta.expression.adapters.measure;

import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.model.Measure;
import hu.blackbelt.judo.meta.expression.adapters.measure.model.Unit;
import hu.blackbelt.judo.meta.expression.constant.Instance;
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
    private MeasureAdapter measureAdapter;

    @BeforeEach
    public void setUp() {
        modelAdapter = mock(ModelAdapter.class);
        measureSupport = mock(MeasureSupport.class);

        measureAdapter = new MeasureAdapter(modelAdapter, measureProvider, measureSupport);
        measureAdapter.refreshDimensions();
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
    public void testRefreshDimensions() {
        final Map<Map<Measure, Integer>, Measure> dimensions = measureAdapter.dimensions;

        Assert.assertThat(dimensions.size(), is(4));

        final Map<Map<Measure, Integer>, Measure> expected = new HashMap<>();
        expected.put(Collections.singletonMap(measureProvider.getTime(), 1), measureProvider.getTime());
        expected.put(Collections.singletonMap(measureProvider.getLength(), 1), measureProvider.getLength());
        expected.put(Collections.singletonMap(measureProvider.getMass(), 1), measureProvider.getMass());

        final Map<Measure, Integer> velocityBase = new HashMap<>();
        velocityBase.put(measureProvider.getLength(), 1);
        velocityBase.put(measureProvider.getTime(), -1);
        expected.put(velocityBase, measureProvider.getVelocity());

        Assert.assertThat(dimensions, is(expected));
    }

    @Test
    public void testIsMeasuredMeasuredInteger() {
        final NumericExpression numericExpressionWithoutMeasure = newMeasuredIntegerBuilder().withUnitName("kg").withValue(BigInteger.ONE).build();
        Assert.assertTrue(measureAdapter.isMeasured(numericExpressionWithoutMeasure));

        final MeasureName massName = newMeasureNameBuilder().withNamespace("base").withName("Mass").build();
        final NumericExpression numericExpressionWithMeasure = newMeasuredIntegerBuilder().withMeasure(massName).withUnitName("kg").withValue(BigInteger.ONE).build();
        Assert.assertTrue(measureAdapter.isMeasured(numericExpressionWithMeasure));
    }

    @Test
    public void testIsMeasuredMeasuredDecimal() {
        final NumericExpression numericExpressionWithoutMeasure = newMeasuredDecimalBuilder().withUnitName("kg").withValue(BigDecimal.ONE).build();
        Assert.assertTrue(measureAdapter.isMeasured(numericExpressionWithoutMeasure));

        final MeasureName massName = newMeasureNameBuilder().withNamespace("base").withName("Mass").build();
        final NumericExpression numericExpressionWithMeasure = newMeasuredIntegerBuilder().withMeasure(massName).withUnitName("kg").withValue(BigInteger.ONE).build();
        Assert.assertTrue(measureAdapter.isMeasured(numericExpressionWithMeasure));
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

    public void testIsMeasuredIntegerAritmeticExpression() {
        // TODO
    }

    public void testIsMeasuredDecimalAritmeticExpression() {
        // TODO
    }

    public void testIsMeasuredIntegerOppositeExpression() {
        // TODO
    }

    public void testIsMeasuredDecimalOppositeExpression() {
        // TODO
    }

    public void testIsMeasuredIntegerAggregatedExpression() {
        // TODO
    }

    public void testIsMeasuredDecimalAggregatedExpression() {
        // TODO
    }

    public void testIsMeasuredRoundExpression() {
        // TODO
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
    }

    @Test
    public void testIsMeasuredNonMeasuredConstant() {
        final NumericExpression integerExpression = newIntegerConstantBuilder().withValue(BigInteger.ONE).build();
        Assert.assertFalse(measureAdapter.isMeasured(integerExpression));

        final NumericExpression decimalExpression = newDecimalConstantBuilder().withValue(BigDecimal.ONE).build();
        Assert.assertFalse(measureAdapter.isMeasured(decimalExpression));
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
    }

    @Test
    public void testGetUnitOfMeasuredDecimal() {
        final NumericExpression numericExpressionWithoutMeasure = newMeasuredDecimalBuilder().withUnitName("kg").withValue(BigDecimal.ONE).build();
        Assert.assertThat(measureAdapter.getUnit(numericExpressionWithoutMeasure), is(measureProvider.getMass().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny()));

        final MeasureName massName = newMeasureNameBuilder().withNamespace("base").withName("Mass").build();
        final NumericExpression numericExpressionWithMeasure = newMeasuredDecimalBuilder().withMeasure(massName).withUnitName("kg").withValue(BigDecimal.ONE).build();
        Assert.assertThat(measureAdapter.getUnit(numericExpressionWithMeasure), is(measureProvider.getMass().getUnits().parallelStream().filter(u -> Objects.equals(u.getSymbol(), "kg")).findAny()));
    }

    public void testGetDimensionOfNumericAttribute() {
        // TODO
    }

    @Test
    public void testGetDimensionOfMeasuredDecimal() {
        final NumericExpression numericExpressionWithoutMeasure = newMeasuredDecimalBuilder().withUnitName("kg").withValue(BigDecimal.ONE).build();

        Assert.assertThat(measureAdapter.getDimension(numericExpressionWithoutMeasure), is(Optional.of(Collections.singletonMap(measureProvider.getMass(), 1))));

        final MeasureName massName = newMeasureNameBuilder().withNamespace("base").withName("Mass").build();
        final NumericExpression numericExpressionWithMeasure = newMeasuredDecimalBuilder().withMeasure(massName).withUnitName("kg").withValue(BigDecimal.ONE).build();
        Assert.assertThat(measureAdapter.getDimension(numericExpressionWithMeasure), is(Optional.of(Collections.singletonMap(measureProvider.getMass(), 1))));
    }

    @Test
    public void testGetDimensionOfMeasuredInteger() {
        final NumericExpression numericExpressionWithoutMeasure = newMeasuredIntegerBuilder().withUnitName("kg").withValue(BigInteger.ONE).build();

        Assert.assertThat(measureAdapter.getDimension(numericExpressionWithoutMeasure), is(Optional.of(Collections.singletonMap(measureProvider.getMass(), 1))));

        final MeasureName massName = newMeasureNameBuilder().withNamespace("base").withName("Mass").build();
        final NumericExpression numericExpressionWithMeasure = newMeasuredIntegerBuilder().withMeasure(massName).withUnitName("kg").withValue(BigInteger.ONE).build();
        Assert.assertThat(measureAdapter.getDimension(numericExpressionWithMeasure), is(Optional.of(Collections.singletonMap(measureProvider.getMass(), 1))));
    }

    public void testGetDimensionOfDecimalAritmeticExpression() {
        // TODO
    }

    public void testGetDimensionOfIntegerAritmeticExpression() {
        // TODO
    }

    public void testGetDimensionOfIntegerOppositeExpression() {
        // TODO
    }

    public void testGetDimensionOfDecimalOppositeExpression() {
        // TODO
    }

    public void testGetDimensionOfIntegerAggregatedExpression() {
        // TODO
    }

    public void testGetDimensionOfDecimalAggregatedExpression() {
        // TODO
    }

    public void testGetDimensionOfRoundExpression() {
        // TODO
    }

    public void testGetDimensionOfIntegerSwitchExpression() {
        // TODO
    }

    public void testGetDimensionOfDecimalSwitchExpression() {
        // TODO
    }

    @Test
    public void testGetDimensionOfTimestampDifferenceExpression() {
        final NumericExpression duration = newTimestampDifferenceExpressionBuilder()
                .withStartTimestamp(newTimestampConstantBuilder()
                        .withValue("2019-01-01 12:00:00Z")
                        .build())
                .withEndTimestamp(newTimestampConstantBuilder()
                        .withValue("2019-01-01 13:00:00Z")
                        .build())
                .build();

        Assert.assertThat(measureAdapter.getDimension(duration), is(Optional.of(Collections.singletonMap(measureProvider.getTime(), 1))));
    }

    @Test
    public void testGetDimensionOfNonMeasuredConstant() {
        final NumericExpression integerExpression = newIntegerConstantBuilder().withValue(BigInteger.ONE).build();
        Assert.assertThat(measureAdapter.getDimension(integerExpression), is(Optional.of(Collections.emptyMap())));

        final NumericExpression decimalExpression = newDecimalConstantBuilder().withValue(BigDecimal.ONE).build();
        Assert.assertThat(measureAdapter.getDimension(decimalExpression), is(Optional.of(Collections.emptyMap())));
    }
}
