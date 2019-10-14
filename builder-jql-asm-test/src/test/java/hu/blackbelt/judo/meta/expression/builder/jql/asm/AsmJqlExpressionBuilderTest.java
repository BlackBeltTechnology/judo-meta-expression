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
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionEvaluator;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import hu.blackbelt.judo.meta.measure.Measure;
import hu.blackbelt.judo.meta.measure.Unit;
import hu.blackbelt.judo.meta.measure.support.MeasureModelResourceSupport;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;
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

        // Execution context
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

    private Expression createExpression(final EClass clazz, final String jqlExpressionAsString, final boolean validate) throws Exception {
        final Expression expression = asmJqlExpressionBuilder.createExpression(clazz, jqlExpressionAsString);

        if (validate && expression != null) {
            validateExpressions(expression.eResource());
        }

        return expression;
    }

    @Test
    void testOrderDate() throws Exception {
        final EClass order = asmUtils.all(EClass.class).filter(c -> "Order".equals(c.getName())).findAny().get();
        final Expression expression = createExpression(order, "self.orderDate");
        assertNotNull(expression);

        asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("orderDate", JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.GETTER), order, expression);

        log.info("Order date: " + expression);
    }

    @Test
    void testOrderCategories() throws Exception {
        final EClass order = asmUtils.all(EClass.class).filter(c -> "Order".equals(c.getName())).findAny().get();
        final Expression expression = createExpression(order, "self.orderDetails.product.category");
        assertNotNull(expression);

        asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("categories", JqlExpressionBuilder.BindingType.RELATION, JqlExpressionBuilder.BindingRole.GETTER), order, expression);

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
        final EClass order = asmUtils.all(EClass.class).filter(c -> "Order".equals(c.getName())).findAny().get();
        final EClass internationalOrder = asmUtils.all(EClass.class).filter(c -> "InternationalOrder".equals(c.getName())).findAny().get();
        final EClass orderDetail = asmUtils.all(EClass.class).filter(c -> "OrderDetail".equals(c.getName())).findAny().get();
        final EClass product = asmUtils.all(EClass.class).filter(c -> "Product".equals(c.getName())).findAny().get();
        final EClass category = asmUtils.all(EClass.class).filter(c -> "Category".equals(c.getName())).findAny().get();
        final EClass company = asmUtils.all(EClass.class).filter(c -> "Company".equals(c.getName())).findAny().get();
        final EClass shipper = asmUtils.all(EClass.class).filter(c -> "Shipper".equals(c.getName())).findAny().get();

        assertNotNull(order);
        assertNotNull(internationalOrder);
        assertNotNull(orderDetail);
        assertNotNull(product);
        assertNotNull(category);
        assertNotNull(company);
        assertNotNull(shipper);

        final Expression orderOrderDetails = createExpression(order, "self.orderDetails", false);
        final Expression orderCategories = createExpression(order, "self.orderDetails.product.category", false);
        final Expression orderShipperName = createExpression(order, "self.shipper.companyName", false);
        final Expression orderShipper = createExpression(order, "self.shipper", false);
        final Expression orderOrderDate = createExpression(order, "self.orderDate", false);
        assertNotNull(orderOrderDetails);
        assertNotNull(orderCategories);
        assertNotNull(orderShipperName);
        assertNotNull(orderShipper);
        assertNotNull(orderOrderDate);

        final Expression internationalOrderCustomsDescription = createExpression(internationalOrder, "self.customsDescription", false);
        final Expression internationalOrderExciseTax = createExpression(internationalOrder, "self.exciseTax", false);
        assertNotNull(internationalOrderCustomsDescription);
        assertNotNull(internationalOrderExciseTax);

        final Expression productCategory = createExpression(product, "self.category", false);
        final Expression productProductName = createExpression(product, "self.productName", false);
        final Expression productUnitPrice = createExpression(product, "self.unitPrice", false);
        assertNotNull(productCategory);
        assertNotNull(productProductName);
        assertNotNull(productUnitPrice);

        final Expression companyCompanyName = createExpression(shipper, "self.companyName", false);
        assertNotNull(companyCompanyName);

        final Expression categoryProducts = createExpression(category, "self.products", false);
        final Expression categoryCategoryName = createExpression(category, "self.categoryName", false);
        assertNotNull(categoryProducts);
        assertNotNull(categoryCategoryName);

        final Expression orderDetailProduct = createExpression(orderDetail, "self.product", false);
        final Expression orderDetailCategory = createExpression(orderDetail, "self.product.category", false);
        final Expression orderDetailProductName = createExpression(orderDetail, "self.product.productName", false);
        final Expression orderDetailUnitPrice = createExpression(orderDetail, "self.unitPrice", false);
        final Expression orderDetailQuantity = createExpression(orderDetail, "self.quantity", false);
        final Expression orderDetailDiscount = createExpression(orderDetail, "self.discount", false);
        final Expression orderDetailPrice = createExpression(orderDetail, "self.quantity * self.unitPrice * (1 - self.discount)", false);
        assertNotNull(orderDetailProduct);
        assertNotNull(orderDetailCategory);
        assertNotNull(orderDetailProductName);
        assertNotNull(orderDetailUnitPrice);
        assertNotNull(orderDetailQuantity);
        assertNotNull(orderDetailDiscount);
        assertNotNull(orderDetailPrice);

        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("orderDetails", JqlExpressionBuilder.BindingType.RELATION, JqlExpressionBuilder.BindingRole.GETTER), order, orderOrderDetails));
        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("categories", JqlExpressionBuilder.BindingType.RELATION, JqlExpressionBuilder.BindingRole.GETTER), order, orderCategories));
        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("shipperName", JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.GETTER), order, orderShipperName));
        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("shipper", JqlExpressionBuilder.BindingType.RELATION, JqlExpressionBuilder.BindingRole.GETTER), order, orderShipper));
        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("orderDate", JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.GETTER), order, orderOrderDate));

        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("customsDescription", JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.GETTER), internationalOrder, internationalOrderCustomsDescription));
        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("exciseTax", JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.GETTER), internationalOrder, internationalOrderExciseTax));

        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("category", JqlExpressionBuilder.BindingType.RELATION, JqlExpressionBuilder.BindingRole.GETTER), product, productCategory));
        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("productName", JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.GETTER), product, productProductName));
        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("unitPrice", JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.GETTER), product, productUnitPrice));

        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("companyName", JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.GETTER), company, companyCompanyName));

        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("products", JqlExpressionBuilder.BindingType.RELATION, JqlExpressionBuilder.BindingRole.GETTER), category, categoryProducts));
        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("categoryName", JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.GETTER), category, categoryCategoryName));

        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("product", JqlExpressionBuilder.BindingType.RELATION, JqlExpressionBuilder.BindingRole.GETTER), orderDetail, orderDetailProduct));
        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("category", JqlExpressionBuilder.BindingType.RELATION, JqlExpressionBuilder.BindingRole.GETTER), orderDetail, orderDetailCategory));
        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("productName", JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.GETTER), orderDetail, orderDetailProductName));
        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("unitPrice", JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.GETTER), orderDetail, orderDetailUnitPrice));
        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("quantity", JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.GETTER), orderDetail, orderDetailQuantity));
        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("discount", JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.GETTER), orderDetail, orderDetailDiscount));
        assertNotNull(asmJqlExpressionBuilder.createBinding(new JqlExpressionBuilder.BindingContext("price", JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.GETTER), orderDetail, orderDetailPrice));

        validateExpressions(expressionModelResourceSupport.getResource());
    }
}
