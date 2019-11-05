package hu.blackbelt.judo.meta.expression.adapters.esm;

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
import hu.blackbelt.judo.meta.expression.constant.MeasuredInteger;
import hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator;
import hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator.*;
import hu.blackbelt.judo.meta.expression.object.ObjectNavigationExpression;
import hu.blackbelt.judo.meta.expression.object.ObjectVariableReference;
import hu.blackbelt.judo.meta.expression.operator.DecimalOperator;
import hu.blackbelt.judo.meta.measure.support.MeasureModelResourceSupport;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static hu.blackbelt.judo.meta.esm.expression.util.builder.ExpressionBuilders.newReferenceExpressionTypeBuilder;
import static hu.blackbelt.judo.meta.esm.measure.util.builder.MeasureBuilders.newMeasuredTypeBuilder;
import static hu.blackbelt.judo.meta.esm.namespace.util.builder.NamespaceBuilders.newModelBuilder;
import static hu.blackbelt.judo.meta.esm.structure.util.builder.StructureBuilders.newTwoWayRelationMemberBuilder;
import static hu.blackbelt.judo.meta.esm.support.EsmModelResourceSupport.esmModelResourceSupportBuilder;
import static hu.blackbelt.judo.meta.esm.type.util.builder.TypeBuilders.newStringTypeBuilder;
import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newCollectionNavigationFromObjectExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator.*;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalAritmeticExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectNavigationExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newMeasureNameBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertThat(modelAdapter.getTypeName(order).get(), hasToString("demo::Order"));
        assertThat(modelAdapter.getTypeName(category).get(), hasToString("demo::superpackage::entities::Category"));
        assertThat(modelAdapter.getTypeName(entities).get(), hasToString("demo::superpackage::entities"));
        assertThat(modelAdapter.getTypeName(customer).get(), hasToString("demo::superpackage::Customer"));
        assertThat(modelAdapter.getTypeName(superpackage).get(), hasToString("demo::superpackage"));
    }

    @Test
    public void testGetEnumerationTypeName() {
        EnumerationType countries = createEnum("Countries", "HU", "AT");
        EnumerationType titles = createEnum("Titles", "MR", "MRS");
        initResources(createTestModel(countries, createPackage("enums", titles)));
        assertThat(modelAdapter.getEnumerationTypeName(countries).get(), hasToString("demo::Countries"));
        assertThat(modelAdapter.getEnumerationTypeName(titles).get(), hasToString("demo::enums::Titles"));
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
        assertThat(modelAdapter.getMeasureName(measureMap.get("Length")).get(), hasToString("demo::measures::Length"));
    }

    @Test
    public void testGetMeasureOfExpression() {
        MeasuredType velocityType = newMeasuredTypeBuilder().withStoreUnit(measureMap.get("Velocity").getUnits().get(0)).withName("velocity").build();
        EntityType vehicle = new EntityCreator("Vehicle").withAttribute("speed", velocityType).create();
        initResources(createTestModel(velocityType, vehicle));
        ObjectVariableReference variableReference = createVariableReference(vehicle);
        Measure measure = modelAdapter.getMeasure(
                newDecimalAritmeticExpressionBuilder().withLeft(
                        newMeasuredDecimalBuilder()
                                .withMeasure(modelAdapter.getMeasureName(measureMap.get("Time")).get())
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
        MeasuredDecimal decimalLength = newMeasuredDecimalBuilder().withMeasure(modelAdapter.getMeasureName(measureMap.get("Length")).get()).withUnitName("m").withValue(BigDecimal.ONE).build();
        assertThat(modelAdapter.getUnit(decimalLength).get(), is(measureMap.get("Length").getUnits().get(0)));
        MeasuredInteger integerTime = newMeasuredIntegerBuilder().withMeasure(modelAdapter.getMeasureName(measureMap.get("Time")).get()).withUnitName("s").withValue(BigInteger.ONE).build();
        assertThat(modelAdapter.getUnit(integerTime).get(), is(measureMap.get("Time").getUnits().get(0)));
        // TODO Is the following valid?
//        DecimalAritmeticExpression aritmeticExpression = newDecimalAritmeticExpressionBuilder().withLeft(
//                newMeasuredDecimalBuilder()
//                        .withMeasure(modelAdapter.getMeasureName(measureMap.get("Time")).get())
//                        .withUnitName("s")
//                        .withValue(BigDecimal.ONE)
//                        .build()).withOperator(DecimalOperator.MULTIPLY).withRight(
//                newDecimalAttributeBuilder().withAttributeName("speed").withObjectExpression(variableReference).build()).build();
//        Unit derivedLengthUnit = modelAdapter.getUnit(aritmeticExpression).get();
//        assertThat(derivedLengthUnit, is(measureMap.get("Length").getUnits().get(0)));
    }

    private ObjectVariableReference createVariableReference(EntityType entityType) {
        Instance vehicleInstance  = newInstanceBuilder().withName("self").withElementName(modelAdapter.getTypeName(entityType).get()).build();
        return newObjectVariableReferenceBuilder().withVariable(vehicleInstance).build();
    }

    @Test
    public void testIsDurationSupportingAddition() {
        initResources(null);
        assertTrue(modelAdapter.isDurationSupportingAddition(measureMap.get("Time").getUnits().get(0)));
        assertFalse(modelAdapter.isDurationSupportingAddition(measureMap.get("Length").getUnits().get(0)));
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
    public void testGetMeasure() {
        initResources(null);
        MeasureName measureName = newMeasureNameBuilder().withNamespace("demo::measures").withName("Length").build();
        assertThat(modelAdapter.get(measureName).get(), is(measureMap.get("Length")));
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
                .withUpper(1)
                .withDefaultExpression(newReferenceExpressionTypeBuilder().withExpression(""))
                .withRangeExpression(newReferenceExpressionTypeBuilder().withExpression("")).build();
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
        EntityType address = new EntityCreator("Address").withAttribute("zip", stringType).create();
        initResources(createTestModel(stringType, address));
        assertThat(modelAdapter.getAttribute(address, "zip").get().getName(), is("zip"));
        assertThat(modelAdapter.getAttribute(address, "zip").get().getDataType(), is(stringType));
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


}
