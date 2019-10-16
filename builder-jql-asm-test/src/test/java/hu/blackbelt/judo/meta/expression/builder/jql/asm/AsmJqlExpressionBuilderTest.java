package hu.blackbelt.judo.meta.expression.builder.jql.asm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.api.ModelContext;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.asm.support.AsmModelResourceSupport;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.adapters.asm.AsmModelAdapter;
import hu.blackbelt.judo.meta.expression.binding.Binding;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
import hu.blackbelt.judo.meta.expression.constant.DateConstant;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionEvaluator;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import hu.blackbelt.judo.meta.measure.Measure;
import hu.blackbelt.judo.meta.measure.Unit;
import hu.blackbelt.judo.meta.measure.support.MeasureModelResourceSupport;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder.BindingType.ATTRIBUTE;
import static hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder.BindingType.RELATION;
import static hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport.SaveArguments.expressionSaveArgumentsBuilder;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AsmJqlExpressionBuilderTest {

    private static final String TARGET_TEST_CLASSES = "target/test-classes";
    private static final Log log = new Slf4jLog();

    private JqlExpressionBuilder<EClassifier, EDataType, EAttribute, EEnum, EClass, EReference, Measure, Unit> asmJqlExpressionBuilder;

    private Resource asmResource;
    private Resource measureResource;
    private AsmModelAdapter modelAdapter;
    private AsmUtils asmUtils;
    private ExpressionModelResourceSupport expressionModelResourceSupport;

    @BeforeEach
    void setUp() throws Exception {
        final AsmModelResourceSupport asmModelResourceSupport = AsmModelResourceSupport.loadAsm(AsmModelResourceSupport.LoadArguments.asmLoadArgumentsBuilder()
                .file(new File("src/test/model/asm.model"))
                .uri(URI.createURI("urn:test.judo-meta-asm"))
                .build());
        asmResource = asmModelResourceSupport.getResource();
        final MeasureModelResourceSupport measureModelResourceSupport = MeasureModelResourceSupport.loadMeasure(MeasureModelResourceSupport.LoadArguments.measureLoadArgumentsBuilder()
                .file(new File("src/test/model/measure.model"))
                .uri(URI.createURI("urn:test.judo-meta-measure"))
                .build());
        measureResource = measureModelResourceSupport.getResource();

        expressionModelResourceSupport = ExpressionModelResourceSupport.expressionModelResourceSupportBuilder()
                .uri(URI.createURI("urn:test.judo-meta-expression"))
                .build();

        modelAdapter = new AsmModelAdapter(asmModelResourceSupport.getResourceSet(), measureModelResourceSupport.getResourceSet());
        asmJqlExpressionBuilder = new JqlExpressionBuilder<>(modelAdapter, expressionModelResourceSupport.getResource());

        asmUtils = new AsmUtils(asmModelResourceSupport.getResourceSet());
    }

    @AfterEach
    void tearDown(final TestInfo testInfo) throws Exception {
        expressionModelResourceSupport.saveExpression(expressionSaveArgumentsBuilder()
                .file(new File(TARGET_TEST_CLASSES, testInfo.getDisplayName().replace("(", "").replace(")", "") + "-expression.model"))
                .build());

        asmResource = null;
        measureResource = null;
        modelAdapter = null;
        asmUtils = null;
        expressionModelResourceSupport = null;
    }

    private void validateExpressions(final Resource expressionResource) throws Exception {
        final List<ModelContext> modelContexts = Arrays.asList(
                wrappedEmfModelContextBuilder()
                        .log(log)
                        .name("NORTHWIND")
                        .validateModel(false)
                        .aliases(ImmutableList.of("ASM"))
                        .resource(asmResource)
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

    private Expression createExpression(final EClass clazz, final String jqlExpressionAsString) throws Exception {
        return createExpression(clazz, jqlExpressionAsString, true);
    }

    private Expression createExpression(final EClass clazz, final String jqlExpressionString, final boolean validate) throws Exception {
        final Expression expression = asmJqlExpressionBuilder.createExpression(clazz, jqlExpressionString);
        assertNotNull(expression);
        if (validate) {
            validateExpressions(expression.eResource());
        }
        return expression;
    }

    private Expression createGetterExpression(EClass clazz, String jqlExpressionString, boolean validate, String feature, JqlExpressionBuilder.BindingType bindingType) throws Exception {
        Expression expression = createExpression(clazz, jqlExpressionString, validate);
        createGetterBinding(clazz, expression, feature, bindingType);
        return expression;
    }

    @Test
    void testOrderDate() throws Exception {
        final EClass order = asmUtils.all(EClass.class).filter(c -> "Order".equals(c.getName())).findAny().get();
        final Expression expression = createExpression(order, "self.orderDate");
        assertNotNull(expression);

        createGetterBinding(order, expression, "orderDate", ATTRIBUTE);

        log.info("Order date: " + expression);
    }

    @Test
    void testOrderCategories() throws Exception {
        final EClass order = asmUtils.all(EClass.class).filter(c -> "Order".equals(c.getName())).findAny().get();
        final Expression expression = createExpression(order, "self.orderDetails.product.category");
        assertNotNull(expression);

        createGetterBinding(order, expression, "categories", RELATION);

        log.info("Order categories: " + expression);
    }

    @Test
    void testSimpleArithmeticOperation() throws Exception {
        final Expression expression = createExpression(null, "2*3");
        log.info("Simple arithmetic operation: " + expression);
        assertNotNull(expression);
    }

    @Test
    void testSimpleDaoTest() throws Exception {
        EClass order = findBase("Order");
        createGetterExpression(order, "self.orderDetails", false, "orderDetails", RELATION);
        createGetterExpression(order, "self.orderDetails.product.category", false, "categories", RELATION);
        createGetterExpression(order, "self.shipper.companyName", false, "shipperName", ATTRIBUTE);
        createGetterExpression(order, "self.shipper", false, "shipper", RELATION);
        createGetterExpression(order, "self.orderDate", false, "oderDate", ATTRIBUTE);

        EClass internationalOrder = findBase("InternationalOrder");
        createGetterExpression(internationalOrder, "self.customsDescription", false, "customsDescription", ATTRIBUTE);
        createGetterExpression(internationalOrder, "self.exciseTax", false, "exciseTax", ATTRIBUTE);

        EClass product = findBase("Product");
        createGetterExpression(product, "self.category", false, "category", RELATION);
        createGetterExpression(product, "self.productName", false, "productName", ATTRIBUTE);
        createGetterExpression(product, "self.unitPrice", false, "unitPrice", ATTRIBUTE);

        EClass shipper = findBase("Shipper");
        createGetterExpression(shipper, "self.companyName", false, "companyName", ATTRIBUTE);

        EClass category = findBase("Category");
        createGetterExpression(category, "self.products", false, "products", RELATION);
        createGetterExpression(category, "self.categoryName", false, "categoryName", ATTRIBUTE);

        EClass orderDetail = findBase("OrderDetail");
        createGetterExpression(orderDetail, "self.product", false, "product", RELATION);
        createGetterExpression(orderDetail, "self.product.category", false, "category", RELATION);
        createGetterExpression(orderDetail, "self.product.productName", false, "productName", ATTRIBUTE);
        createGetterExpression(orderDetail, "self.unitPrice", false,"unitPrice", ATTRIBUTE);
        createGetterExpression(orderDetail, "self.quantity", false, "quantity", ATTRIBUTE);
        createGetterExpression(orderDetail, "self.discount", false, "discount", ATTRIBUTE);
        createGetterExpression(orderDetail, "self.quantity * self.unitPrice * (1 - self.discount)", false, "price", ATTRIBUTE);

        validateExpressions(expressionModelResourceSupport.getResource());
    }

    private EClass findBase(String entityName) {
        return asmUtils.all(EClass.class).filter(c -> entityName.equals(c.getName())).findAny().orElseThrow(IllegalArgumentException::new);
    }

    private Binding createGetterBinding(EClass base, Expression expression, String feature, JqlExpressionBuilder.BindingType bindingType) {
        Binding binding = asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext(feature, bindingType, JqlExpressionBuilder.BindingRole.GETTER), base, expression);
        assertNotNull(binding);
        return binding;
    }

    @Test
    void testConstants() throws Exception {
        createExpression(null, "1");
        createExpression(null, "1.2");
        createExpression(null, "true");
        createExpression(null, "false");
        createExpression(null, "'a'");
        createExpression(null, "\"\"");
        createExpression(null, "\"b\"");
        DateConstant dateConstant = (DateConstant) createExpression(null, "`2019-12-31`");
        createExpression(null, "`2019-12-31T13:52:03 Europe/Budapest`");
    }

    @Test
    void testSwitchCase() throws Exception {
        createExpression(null, "true ? 1 : 2");
        createExpression(null, "true ? 1 : 2 + 3");
        createExpression(null, "true ? 1.0 : 2.0 + 3");

        EClass order = findBase("Order");
        createGetterExpression(order, "false ? self.shipper.companyName : 'b'", true, "stringSwitch", ATTRIBUTE);
    }

    @Test
    void testStringOperations() throws Exception {
        EClass category = findBase("Category");
        createExpression(category, "self.categoryName < 'c'");
        createExpression(category, "self.categoryName > 'c'");
        createExpression(category, "self.categoryName <= 'c'");
        createExpression(category, "self.categoryName >= 'c'");
        createExpression(category, "self.categoryName = 'c'");
        createExpression(category, "self.categoryName <> 'c'");

        // Concatenate
        EClass order = findBase("Order");
        createExpression(null, "'a'+'b'");
        createGetterExpression(order, "self.shipper.companyName + self.shipper.companyName", false, "shipperNameConcat", ATTRIBUTE);
        createGetterExpression(order, "'_' + self.shipper.companyName", false, "shipperNameConcat2", ATTRIBUTE);
    }

    @Test
    void testUnaryOperations() throws Exception {
        createExpression(null, "-1");
        createExpression(null, "-1.0");
        createExpression(null, "not true");
    }

    @Test
    void testEnums() throws Exception {
        EClass order = findBase("Order");
        createGetterExpression(order, "self.shipAddress.country = demo::types::Countries#AT", true, "countryCheck", ATTRIBUTE);
    }

    @Test
    void testMeasures() throws Exception {
        createExpression(null, "5[demo::measures::Time#min]");
        createExpression(null, "1.23[min]");
    }

    @Test
    void testStringFunctions() throws Exception {
        EClass order = findBase("Order");

        // LowerCase
        createGetterExpression(order, "self.shipper.companyName!lowerCase()", true, "shipperNameLower", ATTRIBUTE);

        // UpperCase
        createGetterExpression(order, "self.shipper.companyName!upperCase()", true, "shipperNameUpper", ATTRIBUTE);

        // Length
        createGetterExpression(order, "self.shipper.companyName!lowerCase()!length() > 0", true, "shipperNameLowerLength", ATTRIBUTE);

        // SubString
        createGetterExpression(order, "self.shipper.companyName!substring(1, 4)!length() > 0", true, "shipperNameSubString1", ATTRIBUTE);
        createGetterExpression(order, "self.shipper.companyName!substring(1, self.shipper.companyName!length()-1)", true, "shipperNameSubStringEnd", ATTRIBUTE);

        // Position
        createGetterExpression(order, "self.shipper.companyName!position('a') > 0", true, "shipperNamePosition", ATTRIBUTE);
        createGetterExpression(order, "self.shipper.companyName!position(self.shipper.companyName) > 0", true, "shipperNamePosition2", ATTRIBUTE);

        // Replace
        createGetterExpression(order, "self.shipper.companyName!replace('\\n', '')", true, "shipperNameReplace1", ATTRIBUTE);
        createGetterExpression(order, "self.shipper.companyName!replace('$', self.shipper.companyName)", true, "shipperNameReplace2", ATTRIBUTE);

        // Trim
        createGetterExpression(order, "self.shipper.companyName!trim()", true, "shipperTrim", ATTRIBUTE);
    }

}
