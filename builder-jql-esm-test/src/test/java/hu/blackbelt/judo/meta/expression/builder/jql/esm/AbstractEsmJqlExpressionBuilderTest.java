package hu.blackbelt.judo.meta.expression.builder.jql.esm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.api.ModelContext;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.esm.measure.DurationType;
import hu.blackbelt.judo.meta.esm.measure.Measure;
import hu.blackbelt.judo.meta.esm.measure.Unit;
import hu.blackbelt.judo.meta.esm.namespace.Model;
import hu.blackbelt.judo.meta.esm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.esm.namespace.Package;
import hu.blackbelt.judo.meta.esm.structure.Class;
import hu.blackbelt.judo.meta.esm.structure.PrimitiveTypedElement;
import hu.blackbelt.judo.meta.esm.structure.ReferenceTypedElement;
import hu.blackbelt.judo.meta.esm.structure.Sequence;
import hu.blackbelt.judo.meta.esm.structure.TransferObjectType;
import hu.blackbelt.judo.meta.esm.support.EsmModelResourceSupport;
import hu.blackbelt.judo.meta.esm.type.EnumerationType;
import hu.blackbelt.judo.meta.esm.type.Primitive;
import hu.blackbelt.judo.meta.expression.CollectionExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.ObjectExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.esm.EsmModelAdapter;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
import hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator;
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
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static hu.blackbelt.judo.meta.esm.namespace.util.builder.NamespaceBuilders.newModelBuilder;
import static hu.blackbelt.judo.meta.esm.support.EsmModelResourceSupport.esmModelResourceSupportBuilder;
import static hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator.createPackage;
import static hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport.SaveArguments.expressionSaveArgumentsBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class AbstractEsmJqlExpressionBuilderTest {

    private static final String TARGET_TEST_CLASSES = "target/test-classes";
    private static final String ESM_RESOURCE_URI = "urn:esm.judo-meta-esm";
    private static final String EXPRESSION_RESOURCE_URI = "urn:test.judo-meta-expression";
    private static final Log log = new Slf4jLog();
    private JqlExpressionBuilder<NamespaceElement, Primitive, EnumerationType, Class, PrimitiveTypedElement, ReferenceTypedElement, TransferObjectType, PrimitiveTypedElement, ReferenceTypedElement, Sequence, Measure, Unit> esmJqlExpressionBuilder;
    private Resource measureResource;
    protected EsmModelAdapter modelAdapter;
    private ExpressionModelResourceSupport expressionModelResourceSupport;
    private EsmModelResourceSupport esmModelResourceSupport;
    protected Map<String, Measure> measureMap = new HashMap<>();
    private Model measureModel;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        measureModel = createMeasureModel();
        testInfo.getTestMethod().ifPresent(method -> {
            if (method.getDeclaredAnnotationsByType(EsmJqlExpressionBuilderTest.WithDefaultModel.class).length > 0) {
                initResources(null);
            }
        });
    }

    protected void initResources(Model model) {
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
    }

    protected Model createMeasureModel() {
        Measure mass = new EsmTestModelCreator.MeasureCreator("Mass").withUnit("mg").create();
        Measure length = new EsmTestModelCreator.MeasureCreator("Length").withUnit("m").create();
        measureMap.put("Length", length);
        Measure time = new EsmTestModelCreator.MeasureCreator("Time").withDurationUnit("s", DurationType.SECOND).create();
        measureMap.put("Time", time);
        Measure velocity = new EsmTestModelCreator.MeasureCreator("Velocity").withUnit("m/s").withTerm(length.getUnits().get(0), 1).withTerm(time.getUnits().get(0), -1).create();
        measureMap.put("Velocity", velocity);
        Measure duration = new EsmTestModelCreator.MeasureCreator("Duration").withDurationUnit("day", DurationType.DAY).create();
        measureMap.put("Duration", duration);
        Package measures = createPackage("measures", mass, length, time, velocity, duration);
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

    protected Expression createExpression(String jqlExpressionAsString) {
        return createExpression(null, jqlExpressionAsString);
    }

    protected Expression createExpression(Class clazz, final String jqlExpressionString) {
        final Expression expression = esmJqlExpressionBuilder.createExpression(clazz, jqlExpressionString);
        assertThat(expression, notNullValue());
        return expression;
    }

    protected Expression createExpression(Class clazz, final String jqlExpressionString, TransferObjectType inputParameterType) {
        final Expression expression = esmJqlExpressionBuilder.createExpressionWithInput(clazz, jqlExpressionString, inputParameterType);
        assertThat(expression, notNullValue());
        return expression;
    }

    protected Matcher<Expression> collectionOf(String typeName) {
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
                    TypeName collectionTypeName = modelAdapter.buildTypeName((NamespaceElement) (collection).getObjectType(modelAdapter)).get();
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

    protected Matcher<Expression> objectOf(String typeName) {
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
                    TypeName objectTypeName = modelAdapter.buildTypeName((NamespaceElement) (oe).getObjectType(modelAdapter)).get();
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

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface WithDefaultModel {

    }
}
