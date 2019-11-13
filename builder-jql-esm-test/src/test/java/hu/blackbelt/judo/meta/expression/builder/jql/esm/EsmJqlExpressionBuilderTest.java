package hu.blackbelt.judo.meta.expression.builder.jql.esm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.api.ModelContext;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.esm.measure.DurationType;
import hu.blackbelt.judo.meta.esm.measure.Measure;
import hu.blackbelt.judo.meta.esm.measure.MeasuredType;
import hu.blackbelt.judo.meta.esm.measure.Unit;
import hu.blackbelt.judo.meta.esm.namespace.Model;
import hu.blackbelt.judo.meta.esm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.esm.namespace.Package;
import hu.blackbelt.judo.meta.esm.runtime.EsmUtils;
import hu.blackbelt.judo.meta.esm.structure.Class;
import hu.blackbelt.judo.meta.esm.structure.Sequence;
import hu.blackbelt.judo.meta.esm.structure.*;
import hu.blackbelt.judo.meta.esm.support.EsmModelResourceSupport;
import hu.blackbelt.judo.meta.esm.type.EnumerationType;
import hu.blackbelt.judo.meta.esm.type.Primitive;
import hu.blackbelt.judo.meta.esm.type.StringType;
import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.adapters.esm.EsmModelAdapter;
import hu.blackbelt.judo.meta.expression.binding.Binding;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
import hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator.*;
import hu.blackbelt.judo.meta.expression.numeric.SequenceExpression;
import hu.blackbelt.judo.meta.expression.operator.SequenceOperator;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionEvaluator;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import hu.blackbelt.judo.meta.measure.support.MeasureModelResourceSupport;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static hu.blackbelt.judo.meta.esm.expression.util.builder.ExpressionBuilders.newReferenceExpressionTypeBuilder;
import static hu.blackbelt.judo.meta.esm.measure.util.builder.MeasureBuilders.newMeasuredTypeBuilder;
import static hu.blackbelt.judo.meta.esm.namespace.util.builder.NamespaceBuilders.newModelBuilder;
import static hu.blackbelt.judo.meta.esm.structure.util.builder.StructureBuilders.newNamespaceSequenceBuilder;
import static hu.blackbelt.judo.meta.esm.structure.util.builder.StructureBuilders.newTwoWayRelationMemberBuilder;
import static hu.blackbelt.judo.meta.esm.support.EsmModelResourceSupport.esmModelResourceSupportBuilder;
import static hu.blackbelt.judo.meta.esm.type.util.builder.TypeBuilders.newStringTypeBuilder;
import static hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator.*;
import static hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport.SaveArguments.expressionSaveArgumentsBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class EsmJqlExpressionBuilderTest {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface WithDefaultModel {

    }

    private static final String TARGET_TEST_CLASSES = "target/test-classes";
    private static final String ESM_RESOURCE_URI = "urn:esm.judo-meta-esm";
    private static final String EXPRESSION_RESOURCE_URI = "urn:test.judo-meta-expression";
    private static final Log log = new Slf4jLog();

    private JqlExpressionBuilder<NamespaceElement, Primitive, PrimitiveTypedElement, EnumerationType, hu.blackbelt.judo.meta.esm.structure.Class, ReferenceTypedElement, Sequence, Measure, Unit> esmJqlExpressionBuilder;

    private Resource measureResource;
    private EsmModelAdapter modelAdapter;
    private ExpressionModelResourceSupport expressionModelResourceSupport;
    private EsmModelResourceSupport esmModelResourceSupport;
    private EsmUtils esmUtils;

    private Map<String, Measure> measureMap = new HashMap<>();
    private Model measureModel;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        measureModel = createMeasureModel();
        testInfo.getTestMethod().ifPresent(method -> {
            if (method.getDeclaredAnnotationsByType(WithDefaultModel.class).length > 0) {
                initResources(null);
            }
        });
    }

    private void initResources(Model model) {
        MeasureModelResourceSupport measureModelResourceSupport = loadMeasureModel();
        measureResource = measureModelResourceSupport.getResource();

        expressionModelResourceSupport = ExpressionModelResourceSupport.expressionModelResourceSupportBuilder()
                .uri(URI.createURI(EXPRESSION_RESOURCE_URI))
                .build();
        esmModelResourceSupport = esmModelResourceSupportBuilder()
                .uri(URI.createURI(ESM_RESOURCE_URI))
                .build();

        if (model != null) {
            esmModelResourceSupport.addContent(model);
        }
        modelAdapter = new EsmModelAdapter(esmModelResourceSupport.getResourceSet(), measureModelResourceSupport.getResourceSet());
        esmJqlExpressionBuilder = new JqlExpressionBuilder<>(modelAdapter, expressionModelResourceSupport.getResource());
        esmUtils = new EsmUtils(esmModelResourceSupport.getResourceSet(), false);
    }

    private Model createMeasureModel() {
        Measure mass = new MeasureCreator("Mass").withUnit("mg").create();
        Measure length = new MeasureCreator("Length").withUnit("m").create();
        measureMap.put("Length", length);
        Measure time = new MeasureCreator("Time").withDurationUnit("s", DurationType.SECOND).create();
        measureMap.put("Time", time);
        Measure velocity = new MeasureCreator("Velocity").withUnit("m/s").withTerm(length.getUnits().get(0), 1).withTerm(time.getUnits().get(0), -1).create();
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

    @AfterEach
    void tearDown(final TestInfo testInfo) throws Exception {
        validateExpressions(expressionModelResourceSupport.getResource());
        expressionModelResourceSupport.saveExpression(expressionSaveArgumentsBuilder()
                .file(new File(TARGET_TEST_CLASSES, testInfo.getDisplayName().replace("(", "").replace(")", "") + "-expression.model"))
                .build());
        esmModelResourceSupport.saveEsm(EsmModelResourceSupport.SaveArguments.esmSaveArgumentsBuilder()
                .file(new File(TARGET_TEST_CLASSES, testInfo.getDisplayName().replace("(", "").replace(")", "") + "-esm.model"))
                .build());

        measureResource = null;
        modelAdapter = null;
        expressionModelResourceSupport = null;
        esmModelResourceSupport = null;
        esmUtils = null;
    }

    private void validateExpressions(final Resource expressionResource) throws Exception {
        final List<ModelContext> modelContexts = Arrays.asList(
                wrappedEmfModelContextBuilder()
                        .log(log)
                        .name("ESM")
                        .validateModel(false)
                        .resource(esmModelResourceSupport.getResource())
                        .build(),
                wrappedEmfModelContextBuilder()
                        .log(log)
                        .name("ESM_MEASURES")
                        .validateModel(false)
                        .aliases(ImmutableList.of("MEASURES"))
                        .resource(measureResource)
                        .build(),
                wrappedEmfModelContextBuilder()
                        .log(log)
                        .name("TEST")
                        .validateModel(false)
                        .aliases(ImmutableList.of("EXPR"))
                        .resource(expressionResource)
                        .build()
        );

        ExecutionContext executionContext = executionContextBuilder()
                .log(log)
                .resourceSet(expressionResource.getResourceSet())
                .metaModels(Collections.emptyList())
                .modelContexts(modelContexts)
                .injectContexts(ImmutableMap.of(
                        "evaluator", new ExpressionEvaluator(),
                        "modelAdapter", modelAdapter))
                .build();

        // run the model / metadata loading
        executionContext.load();

        // Validation script
        executionContext.executeProgram(
                evlExecutionContextBuilder()
                        .source(new File("../model/src/main/epsilon/validations/expression.evl").toURI())
                        .expectedErrors(Collections.emptyList())
                        .expectedWarnings(Collections.emptyList())
                        .build());

        executionContext.commit();
        executionContext.close();
    }

    private Expression createExpression(String jqlExpressionAsString) {
        return createExpression(null, jqlExpressionAsString);
    }

    private Expression createExpression(hu.blackbelt.judo.meta.esm.structure.Class clazz, final String jqlExpressionString) {
        final Expression expression = esmJqlExpressionBuilder.createExpression(clazz, jqlExpressionString);
        assertThat(expression, notNullValue());
        return expression;
    }

    private Expression createGetterExpression(hu.blackbelt.judo.meta.esm.structure.Class clazz, String jqlExpressionString, String feature, JqlExpressionBuilder.BindingType bindingType) {
        Expression expression = createExpression(clazz, jqlExpressionString);
        createGetterBinding(clazz, expression, feature, bindingType);
        return expression;
    }

    private Matcher<Expression> collectionOf(String typeName) {
        return new DiagnosingMatcher<Expression>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("A collection of type with name: ").appendText(typeName);
            }

            @Override
            protected boolean matches(Object item, Description mismatchDescription) {
                boolean result = false;
                if (item instanceof CollectionExpression) {
                    CollectionExpression collection = (CollectionExpression) item;
                    TypeName collectionTypeName = modelAdapter.getTypeName((NamespaceElement) (collection).getObjectType(modelAdapter)).get();
                    if (collectionTypeName.getName().equals(typeName)) {
                        result = true;
                    } else {
                        mismatchDescription.appendValue(collectionTypeName.getName());
                    }
                } else {
                    mismatchDescription.appendText("Not a collection: ").appendValue(item);
                }
                return result;
            }

        };
    }

    private Matcher<Expression> objectOf(String typeName) {
        return new DiagnosingMatcher<Expression>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("An object of type with name: ").appendText(typeName);
            }

            @Override
            protected boolean matches(Object item, Description mismatchDescription) {
                boolean result = false;
                if (item instanceof ObjectExpression) {
                    ObjectExpression oe = (ObjectExpression) item;
                    TypeName objectTypeName = modelAdapter.getTypeName((NamespaceElement) (oe).getObjectType(modelAdapter)).get();
                    if (objectTypeName.getName().equals(typeName)) {
                        result = true;
                    } else {
                        mismatchDescription.appendValue(objectTypeName.getName());
                    }
                } else {
                    mismatchDescription.appendText("Not an object: ").appendValue(item);
                }
                return result;
            }

        };
    }

    private Binding createGetterBinding(hu.blackbelt.judo.meta.esm.structure.Class base, Expression expression, String feature, JqlExpressionBuilder.BindingType bindingType) {
        Binding binding = esmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext(feature, bindingType, JqlExpressionBuilder.BindingRole.GETTER), base, expression);
        assertNotNull(binding);
        return binding;
    }

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
    void testEnums() {
        EnumerationType countryEnum = createEnum("Countries", "HU", "US", "AT");
        EnumerationType titleEnum = createEnum("Titles", "MR", "MS");
        Package enumPackage = createPackage("enums", countryEnum, titleEnum);
        initResources(createTestModel(enumPackage));
        Expression expression = createExpression("1 < 2 ? demo::enums::Countries#AT : demo::enums::Countries#RO");
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
        assertThat(modelAdapter.getMeasureName(modelAdapter.getMeasure(velocityExpression).get()).get().getName(), is("Length"));
    }

    @Test
    void testCast() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        TwoWayRelationMember twr = newTwoWayRelationMemberBuilder()
                .withName("customer")
                .withUpper(1)
                .withDefaultExpression(newReferenceExpressionTypeBuilder().withExpression(""))
                .withRangeExpression(newReferenceExpressionTypeBuilder().withExpression("")).build();
        Class order = new EntityCreator("Order").withTwoWayRelation(twr).create();
        Class internationalOrder = new EntityCreator("InternationalOrder").withGeneralization(order).create();
        Class address = new EntityCreator("Address").create();
        Class internationalAddress = new EntityCreator("InternationalAddress")
                .withAttribute("country", stringType)
                .withGeneralization(address)
                .create();
        Class customer = new EntityCreator("Customer").withTwoWayRelation("orders", order, twr, true).withCollectionRelation("addresses", address).create();
        twr.setTarget(customer);
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

}
