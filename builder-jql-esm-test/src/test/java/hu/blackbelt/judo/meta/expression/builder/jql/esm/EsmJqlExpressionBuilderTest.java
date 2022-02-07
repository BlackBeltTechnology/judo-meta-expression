package hu.blackbelt.judo.meta.expression.builder.jql.esm;

import hu.blackbelt.judo.meta.esm.measure.MeasuredType;
import hu.blackbelt.judo.meta.esm.namespace.Package;
import hu.blackbelt.judo.meta.esm.structure.Class;
import hu.blackbelt.judo.meta.esm.structure.EntityType;
import hu.blackbelt.judo.meta.esm.structure.NamespaceSequence;
import hu.blackbelt.judo.meta.esm.structure.TwoWayRelationMember;
import hu.blackbelt.judo.meta.esm.type.DateType;
import hu.blackbelt.judo.meta.esm.type.EnumerationType;
import hu.blackbelt.judo.meta.esm.type.NumericType;
import hu.blackbelt.judo.meta.esm.type.StringType;
import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuildException;
import hu.blackbelt.judo.meta.expression.numeric.SequenceExpression;
import hu.blackbelt.judo.meta.expression.operator.SequenceOperator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static hu.blackbelt.judo.meta.esm.measure.util.builder.MeasureBuilders.newMeasuredTypeBuilder;
import static hu.blackbelt.judo.meta.esm.structure.util.builder.StructureBuilders.newNamespaceSequenceBuilder;
import static hu.blackbelt.judo.meta.esm.structure.util.builder.StructureBuilders.newTwoWayRelationMemberBuilder;
import static hu.blackbelt.judo.meta.esm.type.util.builder.TypeBuilders.newDateTypeBuilder;
import static hu.blackbelt.judo.meta.esm.type.util.builder.TypeBuilders.newNumericTypeBuilder;
import static hu.blackbelt.judo.meta.esm.type.util.builder.TypeBuilders.newStringTypeBuilder;
import static hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator.EntityCreator;
import static hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator.createEnum;
import static hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator.createPackage;
import static hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator.createTestModel;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void testPrimitiveQueryWithInput() {
        NumericType integer = newNumericTypeBuilder().withName("integer").withScale(0).withPrecision(7).build();
        EntityType qInputTarget = new EntityCreator("qInputTarget").create();
        EntityType qInput = new EntityCreator("QInput")
                .withAttribute("attr", integer)
                .withObjectRelation("relation", qInputTarget)
                .create();
        EntityType tester = new EntityCreator("Tester").create();
        initResources(createTestModel(createPackage("p", integer, qInputTarget, qInput, tester)));

        Expression e = assertDoesNotThrow(() -> createExpression(tester, "input.attr", qInput));
        assertThat(e, instanceOf(IntegerExpression.class));

        Exception exception = assertThrows(JqlExpressionBuildException.class,
                                           () -> createExpression(tester, "input.relation.attr", qInput));
        assertThat(exception.getMessage(),
                   equalTo("Errors during building expression: Transfer attribute relation of demo::p::QInput not found"));
    }

    @Test
    public void testComplexQueryWithInput() {
        NumericType integer = newNumericTypeBuilder().withName("integer").withScale(0).withPrecision(7).build();
        EntityType qInputTarget = new EntityCreator("qInputTarget").create();
        EntityType qInput = new EntityCreator("QInput")
                .withAttribute("attr", integer)
                .withCollectionRelation("relation", qInputTarget)
                .create();
        EntityType tester = new EntityCreator("Tester")
                .withAttribute("attr", integer)
                .create();
        initResources(createTestModel(createPackage("p", integer, qInputTarget, qInput, tester)));

        Expression e = assertDoesNotThrow(() -> createExpression(tester, "demo::p::Tester!filter(t | t.attr == input.attr)", qInput));
        assertThat(e, instanceOf(CollectionExpression.class));

        Exception exception = assertThrows(JqlExpressionBuildException.class,
                                           () -> createExpression(tester, "input.relation", qInput));
        assertThat(exception.getMessage(),
                   equalTo("Errors during building expression: Transfer attribute relation of demo::p::QInput not found"));
    }

}
