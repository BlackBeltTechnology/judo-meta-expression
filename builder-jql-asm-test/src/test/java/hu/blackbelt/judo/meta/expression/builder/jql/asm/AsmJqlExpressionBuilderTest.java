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

    private Expression createExpression(final EClass clazz, final String jqlExpressionAsString) throws Exception {
        final Expression expression = asmJqlExpressionBuilder.createExpression(clazz, jqlExpressionAsString);

        if (expression != null) {
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
                            .resource(expression.eResource())
                            .build()
            );

            // Execution context
            ExecutionContext executionContext = executionContextBuilder()
                    .log(log)
                    .resourceSet(expression.eResource().getResourceSet())
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

        return expression;
    }

    @Test
    void testOrderDate() throws Exception {
        final EClass order = asmUtils.all(EClass.class).filter(c -> "Order".equals(c.getName())).findAny().get();
        final Expression expression = createExpression(order, "self.orderDate");
        assertNotNull(expression);

        final JqlExpressionBuilder.BindingContext bindingContext = new JqlExpressionBuilder.BindingContext("orderDate", JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.GETTER);
        asmJqlExpressionBuilder.createBinding(bindingContext, order, expression);

        log.info("Order date: " + expression);
    }

    @Test
    void testOrderCategories() throws Exception {
        final EClass order = asmUtils.all(EClass.class).filter(c -> "Order".equals(c.getName())).findAny().get();
        final Expression expression = createExpression(order, "self.orderDetails.product.category");
        assertNotNull(expression);

        final JqlExpressionBuilder.BindingContext bindingContext = new JqlExpressionBuilder.BindingContext("categories", JqlExpressionBuilder.BindingType.RELATION, JqlExpressionBuilder.BindingRole.GETTER);
        asmJqlExpressionBuilder.createBinding(bindingContext, order, expression);

        log.info("Order categories: " + expression);
    }

    @Test
    void testSimpleArithmeticOperation() throws Exception {
        final Expression expression = createExpression(null, "2*3");
        log.info("Simple arithmetic operation: " + expression);
        assertNotNull(expression);
    }
}
