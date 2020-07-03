package hu.blackbelt.judo.meta.expression.builder.jql.asm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.api.ModelContext;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.expression.CollectionExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.ObjectExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.asm.AsmModelAdapter;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionEvaluator;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import hu.blackbelt.judo.meta.measure.Measure;
import hu.blackbelt.judo.meta.measure.Unit;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder.BindingType.ATTRIBUTE;
import static hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder.BindingType.RELATION;
import static hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport.SaveArguments.expressionSaveArgumentsBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class AsmJqlExpressionBindingTest {

    private static final String TARGET_TEST_CLASSES = "target/test-classes";
    private static final Log log = new Slf4jLog();

    private JqlExpressionBuilder<EClassifier, EDataType, EAttribute, EEnum, EClass, EClass, EReference, EClassifier, Measure, Unit> expressionBuilder;

    private AsmModelAdapter modelAdapter;
    private AsmUtils asmUtils;
    private ExpressionModelResourceSupport expressionModelResourceSupport;

    private ModelLoader modelLoader;

    @BeforeEach
    void setUp() {
        modelLoader = new ModelLoader();

        expressionModelResourceSupport = ExpressionModelResourceSupport.expressionModelResourceSupportBuilder()
                .uri(URI.createURI("urn:test.judo-meta-expression"))
                .build();

        modelAdapter = new AsmModelAdapter(modelLoader.getAsmModel().getResourceSet(), modelLoader.getMeasureModel().getResourceSet());
        expressionBuilder = new JqlExpressionBuilder<>(modelAdapter, expressionModelResourceSupport.getResource());
        asmUtils = new AsmUtils(modelLoader.getAsmModel().getResourceSet());
    }

    private void validateExpressions(final Resource expressionResource, Collection<String> expectedErrors, Collection<String> expectedWarnings) throws Exception {
        final List<ModelContext> modelContexts = Arrays.asList(
                wrappedEmfModelContextBuilder()
                        .log(log)
                        .name("NORTHWIND")
                        .validateModel(false)
                        .aliases(ImmutableList.of("ASM"))
                        .resource(modelLoader.getAsmModel().getResource())
                        .build(),
                wrappedEmfModelContextBuilder()
                        .log(log)
                        .name("NORTHWIND_MEASURES")
                        .validateModel(false)
                        .aliases(ImmutableList.of("MEASURES"))
                        .resource(modelLoader.getMeasureModel().getResource())
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
                        .expectedErrors(expectedErrors)
                        .expectedWarnings(expectedWarnings)
                        .build());

        executionContext.commit();
        executionContext.close();
    }

    private Expression createExpression(String jqlExpressionAsString) {
        return createExpression(null, jqlExpressionAsString);
    }

    private Expression createExpression(final EClass clazz, final String jqlExpressionString) {
        final Expression expression = expressionBuilder.createExpression(clazz, jqlExpressionString);
        assertThat(expression, notNullValue());
        return expression;
    }

    private Expression createGetterExpression(EClass clazz, String jqlExpressionString, String feature, JqlExpressionBuilder.BindingType bindingType) {
        Expression expression = createExpression(clazz, jqlExpressionString);
        expressionBuilder.createGetterBinding(clazz, expression, feature, bindingType);
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
                    TypeName collectionTypeName = modelAdapter.buildTypeName((EClassifier) (collection).getObjectType(modelAdapter)).get();
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
                    TypeName objectTypeName = modelAdapter.buildTypeName((EClassifier) (oe).getObjectType(modelAdapter)).get();
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

    private EClass findBase(String entityName) {
        return asmUtils.all(EClass.class).filter(c -> entityName.equals(c.getName())).findAny().orElseThrow(IllegalArgumentException::new);
    }

    @Test
    void testAttributeBinding() throws Exception {

        EClass order = findBase("Order");

        createGetterExpression(order, "self.shipper.companyName", "orderDate", ATTRIBUTE); //string expr to time stamp
        createGetterExpression(order, "self.orderDate", "freight", ATTRIBUTE); //time stamp expr to string 

        EClass internationalOrder = findBase("InternationalOrder");
        createGetterExpression(internationalOrder, "self.exciseTax", "customsDescription", ATTRIBUTE); // decimal expr to string

        EClass product = findBase("Product");
        createGetterExpression(product, "`2019-12-31`", "productName", ATTRIBUTE); //date expr to string
        createGetterExpression(product, "demo::types::Countries#AT", "unitPrice", ATTRIBUTE); //enum expr to decimal
        createGetterExpression(product, "self.discounted", "weight", ATTRIBUTE); // boolean expr to measured

        EClass orderDetail = findBase("OrderDetail");
        createGetterExpression(orderDetail, "self.quantity", "price", ATTRIBUTE); // integer expr to double

        expressionModelResourceSupport.saveExpression(expressionSaveArgumentsBuilder()
                .file(new File(TARGET_TEST_CLASSES, "testAttributeBinding-expression.model"))
                .validateModel(false)
                .build());
        validateExpressions(expressionModelResourceSupport.getResource(),
                ImmutableList.of(
                        "StringExpressionMatchesBinding|Attribute named orderDate must be string type, because the assigned expression evaluates to a string.",
                        "TimestampExpressionMatchesBinding|Attribute named freight must be timestamp type, because the assigned expression evaluates to a timestamp.",
                        "DecimalExpressionMatchesBinding|Attribute named customsDescription must be decimal type, because the assigned expression evaluates to a decimal.",
                        "DateExpressionMatchesBinding|Attribute named productName must be date type, because the assigned expression evaluates to a date.",
                        "EnumerationExpressionMatchesBinding|Attribute named unitPrice must be enumeration type, because the assigned expression evaluates to an enumeration.",
                        "BooleanExpressionMatchesBinding|Attribute named weight must be boolean type, because the assigned expression evaluates to a boolean.",
                        "IntegerExpressionMatchesBinding|Attribute named price of object type OrderDetail must be integer type, because the assigned expression evaluates to an integer."),
                Collections.emptyList());
    }

    @Test
    @Disabled
    void testReferenceBinding() throws Exception {

        EClass order = findBase("Order");
        createExpression(order, "self.orderDetails");
        createExpression(order, "self.orderDetails.product.category");
        createExpression(order, "self.shipper");

        EClass category = findBase("Category");
        createExpression(category, "self.products");

        expressionModelResourceSupport.saveExpression(expressionSaveArgumentsBuilder()
                .file(new File(TARGET_TEST_CLASSES, "testReferenceBinding-expression.model"))
                .validateModel(false)
                .build());
        validateExpressions(expressionModelResourceSupport.getResource(), ImmutableList.of(
                "CollectionExpressionMatchesBinding|Reference named category refers to an object but the assigned expression evaluates to a collection.",
                "ReferenceExpressionMatchesBinding|Reference named otherShipper does not match the type of the assigned expression",
                "ObjectExpressionMatchesBinding|Reference named products refers to a collection but the assigned expression evaluates to an object."),
                Collections.emptyList());
    }
}
