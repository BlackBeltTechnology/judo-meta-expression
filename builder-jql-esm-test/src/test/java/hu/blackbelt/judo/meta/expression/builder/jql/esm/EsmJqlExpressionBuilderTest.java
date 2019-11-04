package hu.blackbelt.judo.meta.expression.builder.jql.esm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.api.ModelContext;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.esm.expression.ExpressionDialect;
import hu.blackbelt.judo.meta.esm.measure.Measure;
import hu.blackbelt.judo.meta.esm.measure.Unit;
import hu.blackbelt.judo.meta.esm.namespace.Model;
import hu.blackbelt.judo.meta.esm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.esm.namespace.Package;
import hu.blackbelt.judo.meta.esm.namespace.util.builder.PackageBuilder;
import hu.blackbelt.judo.meta.esm.runtime.EsmUtils;
import hu.blackbelt.judo.meta.esm.structure.Class;
import hu.blackbelt.judo.meta.esm.structure.Sequence;
import hu.blackbelt.judo.meta.esm.structure.*;
import hu.blackbelt.judo.meta.esm.structure.util.builder.DataMemberBuilder;
import hu.blackbelt.judo.meta.esm.structure.util.builder.EntityTypeBuilder;
import hu.blackbelt.judo.meta.esm.structure.util.builder.OneWayRelationMemberBuilder;
import hu.blackbelt.judo.meta.esm.support.EsmModelResourceSupport;
import hu.blackbelt.judo.meta.esm.type.EnumerationMember;
import hu.blackbelt.judo.meta.esm.type.EnumerationType;
import hu.blackbelt.judo.meta.esm.type.Primitive;
import hu.blackbelt.judo.meta.esm.type.StringType;
import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.adapters.esm.EsmModelAdapter;
import hu.blackbelt.judo.meta.expression.binding.Binding;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
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
import java.util.*;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static hu.blackbelt.judo.meta.esm.expression.util.builder.ExpressionBuilders.*;
import static hu.blackbelt.judo.meta.esm.namespace.util.builder.NamespaceBuilders.newModelBuilder;
import static hu.blackbelt.judo.meta.esm.namespace.util.builder.NamespaceBuilders.newPackageBuilder;
import static hu.blackbelt.judo.meta.esm.structure.util.builder.StructureBuilders.*;
import static hu.blackbelt.judo.meta.esm.support.EsmModelResourceSupport.esmModelResourceSupportBuilder;
import static hu.blackbelt.judo.meta.esm.type.util.builder.TypeBuilders.*;
import static hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport.SaveArguments.expressionSaveArgumentsBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EsmJqlExpressionBuilderTest {

    private static final String TARGET_TEST_CLASSES = "target/test-classes";
    private static final String SOURCE_MODEL_NAME = "urn:esm.judo-meta-esm";
    private static final Log log = new Slf4jLog();

    private JqlExpressionBuilder<NamespaceElement, Primitive, PrimitiveTypedElement, EnumerationType, hu.blackbelt.judo.meta.esm.structure.Class, ReferenceTypedElement, Sequence, Measure, Unit> esmJqlExpressionBuilder;

    private Resource measureResource;
    private EsmModelAdapter modelAdapter;
    private ExpressionModelResourceSupport expressionModelResourceSupport;
    private EsmModelResourceSupport esmModelResourceSupport;
    private EsmUtils esmUtils;

    @BeforeEach
    void setUp() throws Exception {
        final MeasureModelResourceSupport measureModelResourceSupport = MeasureModelResourceSupport.loadMeasure(MeasureModelResourceSupport.LoadArguments.measureLoadArgumentsBuilder()
                .file(new File("src/test/model/measure.model"))
                .uri(URI.createURI("urn:test.judo-meta-measure"))
                .build());
        measureResource = measureModelResourceSupport.getResource();

        expressionModelResourceSupport = ExpressionModelResourceSupport.expressionModelResourceSupportBuilder()
                .uri(URI.createURI("urn:test.judo-meta-expression"))
                .build();

        esmModelResourceSupport = esmModelResourceSupportBuilder()
                .uri(URI.createURI(SOURCE_MODEL_NAME))
                .build();

        modelAdapter = new EsmModelAdapter(esmModelResourceSupport.getResourceSet(), measureModelResourceSupport.getResourceSet());
        esmJqlExpressionBuilder = new JqlExpressionBuilder<>(modelAdapter, expressionModelResourceSupport.getResource());
        esmUtils = new EsmUtils(esmModelResourceSupport.getResourceSet(), false);

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
                        .name("NORTHWIND_MEASURES")
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
    void testConstants() {
        createExpression("1");
    }

    @Test
    void testUnaryOperations() {
        createExpression("-1");
        createExpression("-1.0");
        createExpression("not true");
    }

    @Test
    void testStringOperations() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        EntityType category = new EntityCreator("Category").withAttribute("categoryName", stringType).create();
        createTestModel(stringType, category);
        createExpression(category, "self.categoryName < 'c'");
        // Concatenate
        createExpression("'a'+'b'");
    }

    @Test
    void testStringOperationsRelation() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        EntityType company = new EntityCreator("Company").withAttribute("companyName", stringType).create();
        EntityType order = new EntityCreator("Order").withObjectRelation("shipper", company).create();
        createTestModel(stringType, company, order);
        createExpression(order, "self.shipper.companyName + self.shipper.companyName");
        createExpression(order, "'_' + self.shipper.companyName");
    }

    @Test
    void testEnums() {
        EnumerationType countryEnum = createEnum("Countries", "HU", "US", "AT");
        EnumerationType titleEnum = createEnum("Titles", "MR", "MS");
        Package enumPackage = createPackage("enums", countryEnum, titleEnum);
        createTestModel(enumPackage);
        Expression expression = createExpression("1 < 2 ? demo::enums::Countries#AT : demo::enums::Countries#RO");
        assertThrows(IllegalArgumentException.class, () -> createExpression("true ? demo::enums::Countries#AT : demo::enums::Titles#MR"));
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

        createTestModel(createPackage("entities", order, internationalOrder, customer, address, internationalAddress, company), stringType);
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
        createTestModel(entities);
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
        createTestModel(entities);

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

    private Package createPackage(String name, NamespaceElement... children) {
        PackageBuilder packageBuilder = newPackageBuilder().withName(name);
        if (children.length > 0) {
            packageBuilder = packageBuilder.withElements(Arrays.asList(children));
        }
        return packageBuilder.build();
    }

    private RelationFeature createRelation(String name, Class target, int upperBound) {
        OneWayRelationMemberBuilder builder = newOneWayRelationMemberBuilder().withName(name).withTarget(target).withUpper(upperBound);
        builder.withDefaultExpression(newReferenceExpressionTypeBuilder().withDialect(ExpressionDialect.JQL).withExpression(""));
        builder.withGetterExpression(newReferenceExpressionTypeBuilder().withDialect(ExpressionDialect.JQL).withExpression(""));
        builder.withSetterExpression(newReferenceSelectorTypeBuilder().withDialect(ExpressionDialect.JQL).withExpression(""));
        builder.withRangeExpression(newReferenceExpressionTypeBuilder().withDialect(ExpressionDialect.JQL).withExpression(""));
        return builder.build();
    }

    private DataFeature createAttribute(String name, Primitive datatype) {
        DataMemberBuilder builder = newDataMemberBuilder().withName(name).withDataType(datatype);
        builder.withGetterExpression(newDataExpressionTypeBuilder().withDialect(ExpressionDialect.JQL).withExpression(""));
        builder.withDefaultExpression(newDataExpressionTypeBuilder().withDialect(ExpressionDialect.JQL).withExpression(""));
        builder.withSetterExpression(newAttributeSelectorTypeBuilder().withDialect(ExpressionDialect.JQL).withExpression(""));
        return builder.build();
    }

    private EnumerationType createEnum(String name, String... members) {
        List<EnumerationMember> enumerationMembers = new LinkedList<>();
        for (String member : members) {
            enumerationMembers.add(newEnumerationMemberBuilder().withName(member).withOrdinal(enumerationMembers.size()).build());
        }
        return newEnumerationTypeBuilder()
                .withName(name)
                .withMembers(enumerationMembers)
                .build();
    }

    private void createTestModel(NamespaceElement... elems) {
        Model model = newModelBuilder().withName("demo").withElements(Arrays.asList(elems)).build();
        esmModelResourceSupport.addContent(model);

        esmJqlExpressionBuilder = new JqlExpressionBuilder<>(modelAdapter, expressionModelResourceSupport.getResource());
    }

    private class EntityCreator {
        private String name;
        private Collection<DataFeature> attributes = new LinkedList<>();
        private Collection<RelationFeature> relations = new LinkedList<>();
        private Collection<Generalization> generalizations = new LinkedList<>();
        private Collection<EntitySequence> sequences = new LinkedList<>();

        public EntityCreator(String name) {
            this.name = name;
        }

        public EntityCreator withAttribute(String name, Primitive datatype) {
            attributes.add(createAttribute(name, datatype));
            return this;
        }

        public EntityCreator withObjectRelation(String name, Class target) {
            relations.add(createRelation(name, target, 1));
            return this;
        }

        public EntityCreator withCollectionRelation(String name, Class target) {
            relations.add(createRelation(name, target, -1));
            return this;
        }

        public EntityCreator withGeneralization(Class target) {
            generalizations.add(newGeneralizationBuilder().withTarget(target).build());
            return this;
        }

        public EntityCreator withSequence(String name) {
            sequences.add(newEntitySequenceBuilder().withName(name).build());
            return this;
        }

        public EntityType create() {
            EntityTypeBuilder builder = newEntityTypeBuilder().withName(name);
            if (!attributes.isEmpty()) {
                builder.withAttributes(attributes);
            }
            if (!relations.isEmpty()) {
                builder.withRelations(relations);
            }
            if (!generalizations.isEmpty()) {
                builder.withGeneralizations(generalizations);
            }
            if (!sequences.isEmpty()) {
                builder.withSequences(sequences);
            }
            return builder.build();
        }

        public EntityCreator withTwoWayRelation(TwoWayRelationMember partner) {
            relations.add(partner);
            return this;
        }

        public EntityCreator withTwoWayRelation(String name, Class target, TwoWayRelationMember partner, boolean multi) {
            TwoWayRelationMember relationMember = newTwoWayRelationMemberBuilder()
                    .withName(name)
                    .withPartner(partner)
                    .withTarget(target)
                    .withUpper(multi ? -1 : 1)
                    .withDefaultExpression(newReferenceExpressionTypeBuilder().withExpression(""))
                    .withRangeExpression(newReferenceExpressionTypeBuilder().withExpression(""))
                    .build();
            partner.setPartner(relationMember);
            relations.add(relationMember);
            return this;
        }
    }

}
