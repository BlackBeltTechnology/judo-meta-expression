package hu.blackbelt.judo.meta.expression.builder.jql.esm;

import hu.blackbelt.judo.meta.esm.measure.MeasuredType;
import hu.blackbelt.judo.meta.esm.namespace.Package;
import hu.blackbelt.judo.meta.esm.structure.Class;
import hu.blackbelt.judo.meta.esm.structure.*;
import hu.blackbelt.judo.meta.esm.type.*;
import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuildException;
import hu.blackbelt.judo.meta.expression.numeric.SequenceExpression;
import hu.blackbelt.judo.meta.expression.operator.SequenceOperator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static hu.blackbelt.judo.meta.esm.measure.util.builder.MeasureBuilders.newMeasuredTypeBuilder;
import static hu.blackbelt.judo.meta.esm.structure.util.builder.StructureBuilders.newNamespaceSequenceBuilder;
import static hu.blackbelt.judo.meta.esm.structure.util.builder.StructureBuilders.newTwoWayRelationMemberBuilder;
import static hu.blackbelt.judo.meta.esm.type.util.builder.TypeBuilders.*;
import static hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class EsmJqlExpressionBuilderTest extends AbstractEsmJqlExpressionBuilderTest {

    @Test
    @WithDefaultModel
    void testConstants() {
        createExpression("1");
    }

    @Test
    @WithDefaultModel
    void testUnaryOperations() {
        createExpression("-1");
        createExpression("-1.0");
        createExpression("not true");
    }

    @Test
    void testStringOperations() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        EntityType category = new EntityCreator("Category").withAttribute("categoryName", stringType).create();
        initResources(createTestModel(stringType, category));
        createExpression(category, "self.categoryName < 'c'");
        // Concatenate
        createExpression("'a'+'b'");
    }

    @Test
    void testStringOperationsRelation() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        EntityType company = new EntityCreator("Company").withAttribute("companyName", stringType).create();
        EntityType order = new EntityCreator("Order").withObjectRelation("shipper", company).create();
        initResources(createTestModel(stringType, company, order));
        createExpression(order, "self.shipper.companyName + self.shipper.companyName");
        createExpression(order, "'_' + self.shipper.companyName");
    }

    @Test
    void testDateAddition() {
        DateType dateType = newDateTypeBuilder().withName("date").build();
        EntityType company = new EntityCreator("Company").withAttribute("producedOn", dateType).create();
        initResources(createTestModel(dateType, company));
        createExpression(company, "self.producedOn");
        Expression expression = createExpression(company, "self.producedOn + 1[day]");
    }

    @Test
    void testEnums() {
        EnumerationType countryEnum = createEnum("Countries", "HU", "US", "AT");
        EnumerationType titleEnum = createEnum("Titles", "MR", "MS");
        Package enumPackage = createPackage("enums", countryEnum, titleEnum);
        initResources(createTestModel(enumPackage));
        Expression expression = createExpression("1 < 2 ? demo::enums::Countries#AT : demo::enums::Countries#HU");
        assertThrows(IllegalArgumentException.class, () -> createExpression("true ? demo::enums::Countries#AT : demo::enums::Titles#MR"));
    }

    @Test
    void testMeasures() {
        MeasuredType velocityType = newMeasuredTypeBuilder().withName("Velocity").withStoreUnit(measureMap.get("Velocity").getUnits().get(0)).build();
        EntityType vehicle = new EntityCreator("Vehicle").withAttribute("maxSpeed", velocityType).create();
        initResources(createTestModel(createPackage("entities", vehicle), velocityType));

        createExpression("5[demo::measures::Mass#mg]");
        NumericExpression velocityExpression = (NumericExpression) createExpression(vehicle, "self.maxSpeed * 5[demo::measures::Time#s]");
        assertTrue(modelAdapter.isMeasured(velocityExpression));
        assertThat(modelAdapter.buildMeasureName(modelAdapter.getMeasure(velocityExpression).get()).get().getName(), is("Length"));
    }

    @Test
    void testCast() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        TwoWayRelationMember twr = newTwoWayRelationMemberBuilder()
                .withName("customer")
                .withUpper(1).build();
        Class order = new EntityCreator("Order").withTwoWayRelation(twr).create();
        Class address = new EntityCreator("Address").create();
        Class customer = new EntityCreator("Customer").withTwoWayRelation("orders", order, twr, true).withCollectionRelation("addresses", address).create();
        twr.setTarget(customer);

        Class internationalOrder = new EntityCreator("InternationalOrder").withGeneralization(order).create();
        Class internationalAddress = new EntityCreator("InternationalAddress")
                .withAttribute("country", stringType)
                .withGeneralization(address)
                .create();
        Class company = new EntityCreator("Company").withGeneralization(customer).create();

        initResources(createTestModel(createPackage("entities", order, internationalOrder, customer, address, internationalAddress, company), stringType));
        Expression expression = createExpression(customer, "self=>orders!asCollection(demo::entities::InternationalOrder)");
        Expression expression1 = createExpression(customer, "self=>addresses!sort()!head()!asType(demo::entities::InternationalAddress).country");

        createExpression(order, "self=>customer!asType(demo::entities::Company)");

    }

    @Test
    void testStaticExpressions() {
        EntityType product = new EntityCreator("Product").create();
        EntityType order = new EntityCreator("Order").create();
        EntityType employee = new EntityCreator("Employee").withCollectionRelation("orders", order).create();
        Package entities = createPackage("entities", product, order, employee);
        initResources(createTestModel(entities));
        Expression allProducts = createExpression("demo::entities::Product");
        assertThat(allProducts, instanceOf(CollectionExpression.class));
        Expression allOrdersCount = createExpression("demo::entities::Order!count()");
        assertThat(allOrdersCount, instanceOf(IntegerExpression.class));
        Expression allEmployeeOrders = createExpression("demo::entities::Employee=>orders");
        assertThat(allEmployeeOrders, instanceOf(CollectionExpression.class));
        assertThat(allEmployeeOrders, collectionOf("Order"));
        Expression allProductsSorted = createExpression("demo::entities::Product!sort()");
        assertThat(allProductsSorted, collectionOf("Product"));
    }

    @Test
    void testSequence() {
        EntityType product = new EntityCreator("Product").withSequence("ProductSequence").create();
        EntityType order = new EntityCreator("Order").withObjectRelation("product", product).create();
        NamespaceSequence globalSequence = newNamespaceSequenceBuilder().withName("GlobalSequence").build();
        Package entities = createPackage("entities", product, order, globalSequence);
        initResources(createTestModel(entities));

        Expression staticSequenceExpression = createExpression("demo::entities::GlobalSequence!next()");
        assertThat(staticSequenceExpression, instanceOf(SequenceExpression.class));
        assertThat(((SequenceExpression) staticSequenceExpression).getSequence(), instanceOf(StaticSequence.class));
        assertThat(((SequenceExpression) staticSequenceExpression).getOperator(), is(SequenceOperator.NEXT));

        Expression entitySequenceExpression = createExpression("demo::entities::Product!sort()!head().ProductSequence!current()");
        assertThat(entitySequenceExpression, instanceOf(SequenceExpression.class));
        assertThat(((SequenceExpression) entitySequenceExpression).getSequence(), instanceOf(ObjectSequence.class));
        assertThat(((SequenceExpression) entitySequenceExpression).getOperator(), is(SequenceOperator.CURRENT));

        Expression objectSequenceExpression = createExpression(order, "self->product.ProductSequence!current()");
        assertThat(objectSequenceExpression, instanceOf(SequenceExpression.class));
        assertThat(((SequenceExpression) objectSequenceExpression).getSequence(), instanceOf(ObjectSequence.class));
        assertThat(((SequenceExpression) objectSequenceExpression).getOperator(), is(SequenceOperator.CURRENT));
    }

    private static Stream<String> testValidEqualsWithBooleanExpressionsSource() {
        List<String> booleanExpressions = List.of("true", "(1 == 1)", "self!isDefined()");
        List<String> operators = List.of("==", "!=");
        List<String> expressions = new ArrayList<>();
        for (String left : booleanExpressions) {
            for (String right : booleanExpressions) {
                for (String operator : operators) {
                    expressions.add(String.format("%s %s %s", left, operator, right));
                }
            }
        }
        return expressions.stream();
    }

    @ParameterizedTest
    @MethodSource("testValidEqualsWithBooleanExpressionsSource")
    public void testValidEqualsWithBooleanExpressions(final String expression) {
        BooleanType bool = newBooleanTypeBuilder().withName("boolean").build();
        EntityType tester = new EntityCreator("Tester")
                .withAttribute("b", bool)
                .create();
        initResources(createTestModel(createPackage("entities", bool, tester)));
        assertDoesNotThrow(() -> createExpression(tester, expression));
    }

    private static Stream<String> testInvalidEqualsWithBooleanExpressionsSource() {
        List<String> operators = List.of("==", "!=");
        List<String> expressions = new ArrayList<>();
        for (String expression : List.of("1", "'apple'", "demo::entities::Tester!count()")) {
            for (String operator : operators) {
                expressions.add(String.format("true %s %s", operator, expression));
                expressions.add(String.format("%s %s true", expression, operator));
            }
        }
        return expressions.stream();
    }

    @ParameterizedTest
    @MethodSource("testInvalidEqualsWithBooleanExpressionsSource")
    public void testInvalidEqualsWithBooleanExpressions(final String expression) {
        EntityType tester = new EntityCreator("Tester").create();
        initResources(createTestModel(createPackage("entities", tester)));
        assertThrows(UnsupportedOperationException.class, () -> createExpression(expression));
    }

    private static Stream<String> testUsingNonFQNamesSource() {
        return Stream.of(
                "Tester",
                "Tester!any()",
                "Timestamp!now()",
                "AncientTester!any()!asType(Tester)",
                "AncientTester!any()!kindOf(Tester)",
                "AncientTester!any()!typeOf(Tester)",
                "AncientTester!asCollection(Tester)",
                "String!getVariable('SYSTEM', 'variable')",
                "Tester!filter(e | Tester!any().attr == e.attr)",
                "Tester!filter(Tester | demo::entities::Tester!any().attr == Tester.attr)"
                // TODO: !container(...) => TODO add option for containment test
        );
    }

    @ParameterizedTest
    @MethodSource("testUsingNonFQNamesSource")
    public void testUsingNonFQNames(final String script) {
        StringType string = newStringTypeBuilder().withName("String").withMaxLength(255).build();
        TimestampType timestamp = newTimestampTypeBuilder().withName("Timestamp").build();
        EntityType ancientTester = new EntityCreator("AncientTester").create();
        EntityType tester = new EntityCreator("Tester")
                .withAttribute("attr", string)
                .withGeneralization(ancientTester)
                .create();
        initResources(createTestModel(createPackage("entities", ancientTester, tester, string, timestamp)));

        assertDoesNotThrow(() -> createExpression(tester, script));
    }

    private static Stream<Entry<String, String>> testInvalidUsingNonFQNamesSource() {
        return Stream.of(
                Map.entry("tester", "Unknown symbol: tester"),
                Map.entry("tester!any()", "Unknown symbol: tester"),
                Map.entry("Tester!filter(e | tester!any().attr == e.attr)", "Unknown symbol: tester"),
                Map.entry("AncientTester!any()!asType(tester)", "Type not found: demo::entities::tester"),
                Map.entry("AncientTester!any()!kindOf(tester)", "Type not found: demo::entities::tester"),
                Map.entry("AncientTester!any()!typeOf(tester)", "Type not found: demo::entities::tester"),
                Map.entry("AncientTester!asCollection(tester)", "Type not found: demo::entities::tester"),
                Map.entry("Tester!filter(e | demo::entities::tester!any().attr == e.attr)", "Type not found: demo::entities::tester")
                // TODO: !container(...) => TODO add option for containment test
        );
    }

    @ParameterizedTest
    @MethodSource("testInvalidUsingNonFQNamesSource")
    public void testInvalidUsingNonFQNames(final Entry<String, String> scriptEntry) {
        StringType string = newStringTypeBuilder().withName("String").withMaxLength(255).build();
        TimestampType timestamp = newTimestampTypeBuilder().withName("Timestamp").build();
        EntityType ancientTester = new EntityCreator("AncientTester").create();
        EntityType tester = new EntityCreator("Tester")
                .withAttribute("attr", string)
                .withGeneralization(ancientTester)
                .create();
        initResources(createTestModel(createPackage("entities", ancientTester, tester, string, timestamp)));

        JqlExpressionBuildException exception = assertThrows(
                JqlExpressionBuildException.class, () -> createExpression(tester, scriptEntry.getKey()));
        assertThat(exception.getMessage(), endsWith(scriptEntry.getValue()));
    }

    @Test
    public void testUsingNonFQNamesInDifferentPackage() {
        StringType string = newStringTypeBuilder().withName("String").withMaxLength(255).build();
        EntityType tester = new EntityCreator("Tester")
                .withAttribute("attr", string)
                .create();
        EntityType tester1 = new EntityCreator("Tester1")
                .withAttribute("attr", string)
                .create();
        Package types = createPackage("types", string);
        Package entities = createPackage("entities", tester);
        Package entities1 = createPackage("entities1", tester1);
        initResources(createTestModel(entities, entities1, types));

        assertDoesNotThrow(() -> createExpression(tester, "Tester"));
        assertDoesNotThrow(() -> createExpression(tester1, "Tester1"));

        JqlExpressionBuildException exception =
                assertThrows(JqlExpressionBuildException.class, () -> createExpression(tester, "Tester1"));
        assertThat(exception.getMessage(), containsString("Unknown symbol: Tester1"));
        JqlExpressionBuildException exception1 =
                assertThrows(JqlExpressionBuildException.class, () -> createExpression(tester1, "Tester"));
        assertThat(exception1.getMessage(), containsString("Unknown symbol: Tester"));
    }

}
