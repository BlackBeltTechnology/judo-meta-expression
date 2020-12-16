package hu.blackbelt.judo.meta.expression.adapters.esm;

import static hu.blackbelt.judo.meta.esm.measure.util.builder.MeasureBuilders.newMeasuredTypeBuilder;
import static hu.blackbelt.judo.meta.esm.namespace.util.builder.NamespaceBuilders.newModelBuilder;
import static hu.blackbelt.judo.meta.esm.structure.util.builder.StructureBuilders.newTwoWayRelationMemberBuilder;
import static hu.blackbelt.judo.meta.esm.support.EsmModelResourceSupport.esmModelResourceSupportBuilder;
import static hu.blackbelt.judo.meta.esm.type.util.builder.TypeBuilders.newStringTypeBuilder;
import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newCollectionNavigationFromObjectExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newInstanceBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newMeasuredDecimalBuilder;
import static hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator.createEnum;
import static hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator.createPackage;
import static hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator.createTestModel;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalArithmeticExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectNavigationExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newMeasureNameBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.blackbelt.judo.meta.esm.measure.DurationType;
import hu.blackbelt.judo.meta.esm.measure.Measure;
import hu.blackbelt.judo.meta.esm.measure.MeasuredType;
import hu.blackbelt.judo.meta.esm.measure.Unit;
import hu.blackbelt.judo.meta.esm.namespace.Model;
import hu.blackbelt.judo.meta.esm.namespace.Package;
import hu.blackbelt.judo.meta.esm.structure.EntityType;
import hu.blackbelt.judo.meta.esm.structure.TwoWayRelationMember;
import hu.blackbelt.judo.meta.esm.support.EsmModelResourceSupport;
import hu.blackbelt.judo.meta.esm.type.EnumerationType;
import hu.blackbelt.judo.meta.esm.type.StringType;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.collection.CollectionNavigationFromObjectExpression;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal;
import hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator;
import hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator.EntityCreator;
import hu.blackbelt.judo.meta.expression.numeric.DecimalArithmeticExpression;
import hu.blackbelt.judo.meta.expression.object.ObjectNavigationExpression;
import hu.blackbelt.judo.meta.expression.object.ObjectVariableReference;
import hu.blackbelt.judo.meta.expression.operator.DecimalOperator;
import hu.blackbelt.judo.meta.measure.support.MeasureModelResourceSupport;

public class EsmModelAdapterTest {

    private static final String ESM_RESOURCE_URI = "urn:esm.judo-meta-esm";
    private EsmModelAdapter modelAdapter;
    private EsmModelResourceSupport esmModelResourceSupport;
    private MeasureModelResourceSupport measureModelResourceSupport;
    private Map<String, Measure> measureMap = new HashMap<>();
    private Model measureModel;

    @BeforeEach
    public void setUp() {
        measureModel = createMeasureModel();
    }

    public void initResources(Model model) {
        esmModelResourceSupport = esmModelResourceSupportBuilder()
                .uri(URI.createURI(ESM_RESOURCE_URI))
                .build();
        if (model != null) {
            esmModelResourceSupport.addContent(model);
        }

        MeasureModelResourceSupport measureModelResourceSupport = loadMeasureModel();
        modelAdapter = new EsmModelAdapter(esmModelResourceSupport.getResourceSet(), measureModelResourceSupport.getResourceSet());
    }

    private Model createMeasureModel() {
        Measure mass = new EsmTestModelCreator.MeasureCreator("Mass").withUnit("mg").create();
        Measure length = new EsmTestModelCreator.MeasureCreator("Length").withUnit("m").create();
        measureMap.put("Length", length);
        Measure time = new EsmTestModelCreator.MeasureCreator("Time").withDurationUnit("s", DurationType.SECOND).create();
        measureMap.put("Time", time);
        Measure velocity = new EsmTestModelCreator.MeasureCreator("Velocity").withUnit("m/s").withTerm(length.getUnits().get(0), 1).withTerm(time.getUnits().get(0), -1).create();
        measureMap.put("Velocity", velocity);
        Package measures = createPackage("measures", mass, length, time, velocity);
        return newModelBuilder().withName("demo").withElements(Arrays.asList(measures)).build();
    }

    private MeasureModelResourceSupport loadMeasureModel() {
        MeasureModelResourceSupport measureModelResourceSupport = MeasureModelResourceSupport.measureModelResourceSupportBuilder()
                .uri(URI.createURI("urn:test.judo-meta-measure"))
                .build();
        measureModelResourceSupport.addContent(measureModel);
        return measureModelResourceSupport;
    }


    @Test
    public void testGetTypeName() {
        EntityType order = new EntityCreator("Order").create();
        EntityType category = new EntityCreator("Category").create();
        EntityType customer = new EntityCreator("Customer").create();
        Package entities = createPackage("entities", category);
        Package superpackage = createPackage("superpackage", entities, customer);
        initResources(createTestModel(superpackage, order));
        assertThat(modelAdapter.buildTypeName(order).get(), hasToString("demo::Order"));
        assertThat(modelAdapter.buildTypeName(category).get(), hasToString("demo::superpackage::entities::Category"));
        assertThat(modelAdapter.buildTypeName(entities).get(), hasToString("demo::superpackage::entities"));
        assertThat(modelAdapter.buildTypeName(customer).get(), hasToString("demo::superpackage::Customer"));
        assertThat(modelAdapter.buildTypeName(superpackage).get(), hasToString("demo::superpackage"));
    }

    @Test
    public void testGetEnumerationTypeName() {
        EnumerationType countries = createEnum("Countries", "HU", "AT");
        EnumerationType titles = createEnum("Titles", "MR", "MRS");
        initResources(createTestModel(countries, createPackage("enums", titles)));
        assertThat(modelAdapter.buildTypeName(countries).get(), hasToString("demo::Countries"));
        assertThat(modelAdapter.buildTypeName(titles).get(), hasToString("demo::enums::Titles"));
    }

    @Test
    public void testContainsEnumerationMember() {
        EnumerationType countries = createEnum("Countries", "HU", "AT");
        initResources(createTestModel(createPackage("enums", countries)));
        assertTrue(modelAdapter.contains(countries, "HU"));
        assertFalse(modelAdapter.contains(countries, "US"));
    }

    @Test
    public void testGetMeasureName() {
        initResources(null);
        assertThat(modelAdapter.buildMeasureName(measureMap.get("Length")).get(), hasToString("demo::measures::Length"));
    }

    @Test
    public void testGetMeasureOfExpression() {
        MeasuredType velocityType = newMeasuredTypeBuilder().withStoreUnit(measureMap.get("Velocity").getUnits().get(0)).withName("velocity").build();
        EntityType vehicle = new EntityCreator("Vehicle").withAttribute("speed", velocityType).create();
        initResources(createTestModel(velocityType, vehicle));
        ObjectVariableReference variableReference = createVariableReference(vehicle);
        Measure measure = modelAdapter.getMeasure(
                newDecimalArithmeticExpressionBuilder().withLeft(
                        newMeasuredDecimalBuilder()
                                .withMeasure(modelAdapter.buildMeasureName(measureMap.get("Time")).get())
                                .withUnitName("s")
                                .withValue(BigDecimal.ONE)
                                .build()).withOperator(DecimalOperator.MULTIPLY).withRight(
                        newDecimalAttributeBuilder().withAttributeName("speed").withObjectExpression(variableReference).build()).build()).get();
        assertThat(measure, is(measureMap.get("Length")));
    }

    @Test
    public void testGetUnit() {
        MeasuredType velocityType = newMeasuredTypeBuilder().withStoreUnit(measureMap.get("Velocity").getUnits().get(0)).withName("velocity").build();
        EntityType vehicle = new EntityCreator("Vehicle").withAttribute("speed", velocityType).create();
        initResources(createTestModel(velocityType, vehicle));
        ObjectVariableReference variableReference = createVariableReference(vehicle);
        Unit speedUnit = modelAdapter.getUnit(newDecimalAttributeBuilder().withAttributeName("speed").withObjectExpression(variableReference).build()).get();
        assertThat(speedUnit, is(measureMap.get("Velocity").getUnits().get(0)));
        MeasuredDecimal decimalLength = newMeasuredDecimalBuilder().withMeasure(modelAdapter.buildMeasureName(measureMap.get("Length")).get()).withUnitName("m").withValue(BigDecimal.ONE).build();
        assertThat(modelAdapter.getUnit(decimalLength).get(), is(measureMap.get("Length").getUnits().get(0)));
        DecimalArithmeticExpression arithmeticExpression = newDecimalArithmeticExpressionBuilder().withLeft(
                newMeasuredDecimalBuilder()
                        .withMeasure(modelAdapter.buildMeasureName(measureMap.get("Time")).get())
                        .withUnitName("s")
                        .withValue(BigDecimal.ONE)
                        .build()).withOperator(DecimalOperator.MULTIPLY).withRight(
                newDecimalAttributeBuilder().withAttributeName("speed").withObjectExpression(variableReference).build()).build();
        Measure measure = modelAdapter.getMeasure(arithmeticExpression).get();
        assertThat(measure, is(measureMap.get("Length")));
    }

    @Test
    public void testIsDurationSupportingAddition() {
        initResources(null);
        assertTrue(modelAdapter.isDurationSupportingAddition(measureMap.get("Time").getUnits().get(0)));
        assertFalse(modelAdapter.isDurationSupportingAddition(measureMap.get("Length").getUnits().get(0)));
    }

    @Test
    public void testGetMeasure() {
        initResources(null);
        MeasureName measureName = newMeasureNameBuilder().withNamespace("demo::measures").withName("Length").build();
        assertThat(modelAdapter.get(measureName).get(), is(measureMap.get("Length")));
    }

    @Test
    public void testGetDimension() {
        MeasuredType velocityType = newMeasuredTypeBuilder().withStoreUnit(measureMap.get("Velocity").getUnits().get(0)).withName("velocity").build();
        EntityType vehicle = new EntityCreator("Vehicle").withAttribute("speed", velocityType).create();
        initResources(createTestModel(velocityType, vehicle));
        ObjectVariableReference variableReference = createVariableReference(vehicle);
        Map<Measure, Integer> dimensionMap = modelAdapter.getDimension(newDecimalAttributeBuilder().withAttributeName("speed").withObjectExpression(variableReference).build()).get();
        assertThat(dimensionMap.size(), is(2));
        assertThat(dimensionMap, hasKey(measureMap.get("Length")));
        assertThat(dimensionMap, hasKey(measureMap.get("Time")));
    }

    @Test
    public void testGetNamespaceElement() {
        EntityType order = new EntityCreator("Order").create();
        EntityType category = new EntityCreator("Category").create();
        Package entities = createPackage("entities", category);
        Package superpackage = createPackage("superpackage", entities);
        initResources(createTestModel(superpackage, order));
        assertThat(modelAdapter.get(newTypeNameBuilder().withNamespace("demo").withName("Order").build()).get(), is(order));
        assertThat(modelAdapter.get(newTypeNameBuilder().withNamespace("demo").withName("superpackage").build()).get(), is(superpackage));
        assertThat(modelAdapter.get(newTypeNameBuilder().withNamespace("demo::superpackage").withName("entities").build()).get(), is(entities));
        assertThat(modelAdapter.get(newTypeNameBuilder().withNamespace("demo.superpackage.entities").withName("Category").build()).get(), is(category));
        assertThat(modelAdapter.get(newTypeNameBuilder().withNamespace("demo::superpackage::entities").withName("Category").build()).get(), is(category));
    }

    @Test
    public void testIsObjectType() {
        EntityType category = new EntityCreator("Category").create();
        Package entities = createPackage("entities", category);
        initResources(createTestModel(entities));
        assertTrue(modelAdapter.isObjectType(category));
        assertFalse(modelAdapter.isObjectType(entities));
    }

    @Test
    public void testGetReference() {
        EntityType grandfather = new EntityCreator("Grandfather").create();
        EntityType grandmotherFriend = new EntityCreator("GrandmotherFriend").create();
        EntityType grandmother = new EntityCreator("Grandmother").withObjectRelation("friend", grandmotherFriend).create();
        TwoWayRelationMember twr = newTwoWayRelationMemberBuilder()
                .withName("brother")
                .withUpper(1).build();
        EntityType uncle = new EntityCreator("Uncle").withGeneralization(grandfather).withGeneralization(grandmother).withTwoWayRelation(twr).create();
        EntityType father = new EntityCreator("Father").withGeneralization(grandfather).withGeneralization(grandmother).withTwoWayRelation("brother", uncle, twr, false).create();
        twr.setTarget(father);
        EntityType child = new EntityCreator("Child").withGeneralization(father).create();
        initResources(createTestModel(grandfather, grandmother, grandmotherFriend, father, uncle, child));
        assertThat(modelAdapter.getReference(grandmother, "friend").get().getTarget(), is(grandmotherFriend));
        assertThat(modelAdapter.getReference(child, "friend").get().getTarget(), is(grandmotherFriend));
        assertThat(modelAdapter.getReference(uncle, "brother").get().getTarget(), is(father));
        assertThat(modelAdapter.getReference(father, "brother").get().getTarget(), is(uncle));
    }

    @Test
    public void testIsCollection() {
        EntityType order = new EntityCreator("Order").create();
        EntityType address = new EntityCreator("Address").create();
        EntityType customer = new EntityCreator("Customer").withCollectionRelation("orders", order).withObjectRelation("address", address).create();
        EntityType person = new EntityCreator("Person").withGeneralization(customer).create();
        initResources(createTestModel(order, address, customer, person));
        CollectionNavigationFromObjectExpression orders = newCollectionNavigationFromObjectExpressionBuilder().withReferenceName("orders").withObjectExpression(createVariableReference(person)).build();
        ObjectNavigationExpression addressRelation = newObjectNavigationExpressionBuilder().withReferenceName("address").withObjectExpression(createVariableReference(person)).build();
        assertTrue(modelAdapter.isCollection(orders));
        assertFalse(modelAdapter.isCollection(addressRelation));
    }

    @Test
    public void testGetTarget() {
        EntityType order = new EntityCreator("Order").create();
        EntityType address = new EntityCreator("Address").create();
        EntityType customer = new EntityCreator("Customer").withCollectionRelation("orders", order).withObjectRelation("address", address).create();
        EntityType person = new EntityCreator("Person").withGeneralization(customer).create();
        initResources(createTestModel(order, address, customer, person));
        assertThat(modelAdapter.getTarget(customer.getRelations().stream().filter(r -> r.getName().equals("orders")).findAny().get()), is(order));
    }

    @Test
    public void testGetAttribute() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        EntityType internationalAddress = new EntityCreator("InternationalAddress").withAttribute("country", stringType).create();
        EntityType address = new EntityCreator("Address").withAttribute("zip", stringType).withGeneralization(internationalAddress).create();
        initResources(createTestModel(stringType, address, internationalAddress));
        assertThat(modelAdapter.getAttribute(address, "zip").get().getName(), is("zip"));
        assertThat(modelAdapter.getAttribute(address, "zip").get().getDataType(), is(stringType));
        assertThat(modelAdapter.getAttribute(address, "country").get().getDataType(), is(stringType));

    }

    @Test
    public void testGetAttributeType() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        EntityType address = new EntityCreator("Address").withAttribute("zip", stringType).create();
        initResources(createTestModel(stringType, address));
        assertThat(modelAdapter.getAttributeType(modelAdapter.getAttribute(address, "zip").get()).get(), is(stringType));
        assertThat(modelAdapter.getAttributeType(address, "zip").get(), is(stringType));
    }

    @Test
    public void testGetSupertypes() {
        EntityType grandfather = new EntityCreator("Grandfather").create();
        EntityType grandmotherFriend = new EntityCreator("GrandmotherFriend").create();
        EntityType grandmother = new EntityCreator("Grandmother").withObjectRelation("friend", grandmotherFriend).create();
        EntityType uncle = new EntityCreator("Uncle").withGeneralization(grandfather).withGeneralization(grandmother).create();
        EntityType father = new EntityCreator("Father").withGeneralization(grandfather).withGeneralization(grandmother).create();
        EntityType child = new EntityCreator("Child").withGeneralization(father).create();
        initResources(createTestModel(grandfather, grandmother, grandmotherFriend, father, uncle, child));
        assertThat(modelAdapter.getSuperTypes(child), containsInAnyOrder(grandfather, grandmother, father));
    }


    private ObjectVariableReference createVariableReference(EntityType entityType) {
        Instance vehicleInstance  = newInstanceBuilder().withName("self").withElementName(modelAdapter.buildTypeName(entityType).get()).build();
        return newObjectVariableReferenceBuilder().withVariable(vehicleInstance).build();
    }

}
