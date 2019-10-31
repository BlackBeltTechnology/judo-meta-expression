package hu.blackbelt.judo.meta.expression.builder.jql.esm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.api.ModelContext;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.esm.measure.Measure;
import hu.blackbelt.judo.meta.esm.measure.Unit;
import hu.blackbelt.judo.meta.esm.namespace.Model;
import hu.blackbelt.judo.meta.esm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.esm.runtime.EsmUtils;
import hu.blackbelt.judo.meta.esm.structure.PrimitiveTypedElement;
import hu.blackbelt.judo.meta.esm.structure.ReferenceTypedElement;
import hu.blackbelt.judo.meta.esm.support.EsmModelResourceSupport;
import hu.blackbelt.judo.meta.esm.type.EnumerationMember;
import hu.blackbelt.judo.meta.esm.type.EnumerationType;
import hu.blackbelt.judo.meta.esm.type.Primitive;
import hu.blackbelt.judo.meta.expression.CollectionExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.ObjectExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.esm.EsmModelAdapter;
import hu.blackbelt.judo.meta.expression.binding.Binding;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static hu.blackbelt.judo.meta.esm.namespace.util.builder.NamespaceBuilders.newModelBuilder;
import static hu.blackbelt.judo.meta.esm.support.EsmModelResourceSupport.esmModelResourceSupportBuilder;
import static hu.blackbelt.judo.meta.esm.type.util.builder.TypeBuilders.newEnumerationMemberBuilder;
import static hu.blackbelt.judo.meta.esm.type.util.builder.TypeBuilders.newEnumerationTypeBuilder;
import static hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport.SaveArguments.expressionSaveArgumentsBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EsmJqlExpressionBuilderTest {

    private static final String TARGET_TEST_CLASSES = "target/test-classes";
    private static final String SOURCE_MODEL_NAME = "urn:esm.judo-meta-esm";
    private static final Log log = new Slf4jLog();

    private JqlExpressionBuilder<NamespaceElement, Primitive, PrimitiveTypedElement, EnumerationType, hu.blackbelt.judo.meta.esm.structure.Class, ReferenceTypedElement, Measure, Unit> esmJqlExpressionBuilder;

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

    private hu.blackbelt.judo.meta.esm.structure.Class findBase(String entityName) {
//        return asmUtils.all(EClass.class).filter(c -> entityName.equals(c.getName())).findAny().orElseThrow(IllegalArgumentException::new);
        return null;
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
    void testEnums() {
        EnumerationType countryEnum = createEnum("Countries", "HU", "US", "AT");
        EnumerationType titleEnum = createEnum("Titles", "MR", "MS");
        addModelElems(countryEnum, titleEnum);
        Expression expression = createExpression("1 < 2 ? demo::Countries#AT : demo::Countries#RO");
        assertThrows(IllegalArgumentException.class, () -> createExpression("true ? demo::Countries#AT : demo::Titles#MR"));
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

    private void addModelElems(NamespaceElement... elems) {
        Model model = newModelBuilder().withName("demo").withElements(Arrays.asList(elems)).build();
        esmModelResourceSupport.addContent(model);
        esmJqlExpressionBuilder = new JqlExpressionBuilder<>(modelAdapter, expressionModelResourceSupport.getResource());
    }

}
