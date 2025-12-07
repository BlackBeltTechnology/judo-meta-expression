package hu.blackbelt.judo.meta.expression;

/*-
 * #%L
 * Judo :: Expression :: Model :: Test
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.constant.IntegerConstant;
import hu.blackbelt.judo.meta.expression.constant.StringConstant;
import hu.blackbelt.judo.meta.expression.constant.BooleanConstant;
import hu.blackbelt.judo.meta.expression.logical.LogicalAttribute;
import hu.blackbelt.judo.meta.expression.numeric.IntegerAttribute;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionEvaluator;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionModel;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionScriptUriProvider;
import hu.blackbelt.judo.meta.expression.string.StringAttribute;
import hu.blackbelt.judo.meta.expression.validation.ExpressionZetaValidator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.epsilon.common.util.UriUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.*;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.*;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.*;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.*;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;
import static java.util.Collections.emptyList;

/**
 * Performance benchmark test comparing EVL vs Java (Zeta) validation.
 *
 * <p>This test generates a large expression model with 10,000+ elements and measures
 * the validation time for both EVL and Java validators.</p>
 */
public class ExpressionValidationPerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(ExpressionValidationPerformanceTest.class);

    private static final String MODEL_NAME = "PerformanceTestModel";
    private static final int EXPRESSION_COUNT = 2500;
    private static final int WARMUP_ITERATIONS = 1;
    private static final int BENCHMARK_ITERATIONS = 3;

    private ExpressionModel expressionModel;
    private ModelAdapter mockModelAdapter;

    @BeforeEach
    void setUp() {
        expressionModel = ExpressionModel.buildExpressionModel()
                .name("performance-test")
                .uri(URI.createURI("urn:expression.performance-test"))
                .build();
        
        // Create a mock model adapter that returns valid types
        mockModelAdapter = Mockito.mock(ModelAdapter.class);
        Mockito.when(mockModelAdapter.get(Mockito.any(TypeName.class)))
                .thenReturn(Optional.of(new Object()));
    }

    @Test
    void benchmarkEvlVsJavaValidation() throws Exception {
        log.info("=".repeat(80));
        log.info("Expression Validation Performance Benchmark");
        log.info("=".repeat(80));
        log.info("Configuration:");
        log.info("  - Expression count: {}", EXPRESSION_COUNT);
        log.info("  - Warmup iterations: {}", WARMUP_ITERATIONS);
        log.info("  - Benchmark iterations: {}", BENCHMARK_ITERATIONS);
        log.info("-".repeat(80));

        // Generate the large model
        log.info("Generating large expression model...");
        long modelGenStart = System.currentTimeMillis();
        generateLargeModel();
        long modelGenTime = System.currentTimeMillis() - modelGenStart;
        log.info("Model generation completed in {} ms", modelGenTime);

        int elementCount = countModelElements();
        log.info("Total model elements: {}", elementCount);
        log.info("-".repeat(80));

        // Warmup phase
        log.info("Warmup phase ({} iterations)...", WARMUP_ITERATIONS);
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            runEvlValidation();
            runJavaValidation(false);
            runJavaValidation(true);
        }
        log.info("Warmup completed.");
        log.info("-".repeat(80));

        // Benchmark EVL validation
        log.info("Benchmarking EVL validation ({} iterations)...", BENCHMARK_ITERATIONS);
        List<Long> evlTimes = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long start = System.currentTimeMillis();
            runEvlValidation();
            long elapsed = System.currentTimeMillis() - start;
            evlTimes.add(elapsed);
            log.info("  EVL iteration {}: {} ms", i + 1, elapsed);
        }

        // Benchmark Java validation (sequential)
        log.info("Benchmarking Java validation - Sequential ({} iterations)...", BENCHMARK_ITERATIONS);
        List<Long> javaSeqTimes = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long start = System.currentTimeMillis();
            runJavaValidation(false);
            long elapsed = System.currentTimeMillis() - start;
            javaSeqTimes.add(elapsed);
            log.info("  Java Sequential iteration {}: {} ms", i + 1, elapsed);
        }

        // Benchmark Java validation (parallel)
        log.info("Benchmarking Java validation - Parallel ({} iterations)...", BENCHMARK_ITERATIONS);
        List<Long> javaParTimes = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long start = System.currentTimeMillis();
            runJavaValidation(true);
            long elapsed = System.currentTimeMillis() - start;
            javaParTimes.add(elapsed);
            log.info("  Java Parallel iteration {}: {} ms", i + 1, elapsed);
        }

        // Calculate statistics
        log.info("=".repeat(80));
        log.info("RESULTS");
        log.info("=".repeat(80));

        double evlAvg = calculateAverage(evlTimes);
        double evlMin = Collections.min(evlTimes);
        double evlMax = Collections.max(evlTimes);

        double javaSeqAvg = calculateAverage(javaSeqTimes);
        double javaSeqMin = Collections.min(javaSeqTimes);
        double javaSeqMax = Collections.max(javaSeqTimes);

        double javaParAvg = calculateAverage(javaParTimes);
        double javaParMin = Collections.min(javaParTimes);
        double javaParMax = Collections.max(javaParTimes);

        log.info("");
        log.info("EVL Validation:");
        log.info("  Average: {} ms", String.format("%.2f", evlAvg));
        log.info("  Min: {} ms, Max: {} ms", (long) evlMin, (long) evlMax);

        log.info("");
        log.info("Java Validation (Sequential):");
        log.info("  Average: {} ms", String.format("%.2f", javaSeqAvg));
        log.info("  Min: {} ms, Max: {} ms", (long) javaSeqMin, (long) javaSeqMax);
        if (evlAvg > 0) {
            log.info("  Speedup vs EVL: {}x", String.format("%.2f", evlAvg / javaSeqAvg));
        }

        log.info("");
        log.info("Java Validation (Parallel):");
        log.info("  Average: {} ms", String.format("%.2f", javaParAvg));
        log.info("  Min: {} ms, Max: {} ms", (long) javaParMin, (long) javaParMax);
        if (evlAvg > 0) {
            log.info("  Speedup vs EVL: {}x", String.format("%.2f", evlAvg / javaParAvg));
        }
        if (javaSeqAvg > 0) {
            log.info("  Speedup vs Java Sequential: {}x", String.format("%.2f", javaSeqAvg / javaParAvg));
        }

        log.info("");
        log.info("=".repeat(80));
        log.info("Model Statistics:");
        log.info("  Total elements validated: {}", elementCount);
        if (evlAvg > 0) {
            log.info("  Elements per millisecond (EVL): {}", String.format("%.2f", elementCount / evlAvg));
        }
        if (javaSeqAvg > 0) {
            log.info("  Elements per millisecond (Java Seq): {}", String.format("%.2f", elementCount / javaSeqAvg));
        }
        if (javaParAvg > 0) {
            log.info("  Elements per millisecond (Java Par): {}", String.format("%.2f", elementCount / javaParAvg));
        }
        log.info("=".repeat(80));
    }

    private void generateLargeModel() {
        // Create type names for various types
        List<TypeName> typeNames = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            TypeName typeName = newTypeNameBuilder()
                    .withName("Entity" + i)
                    .withNamespace("test.model")
                    .build();
            typeNames.add(typeName);
            expressionModel.addContent(typeName);
        }

        // Generate various expression types
        for (int i = 0; i < EXPRESSION_COUNT; i++) {
            int type = i % 10;
            
            switch (type) {
                case 0:
                    // Integer constants
                    IntegerConstant intConst = newIntegerConstantBuilder()
                            .withValue(BigInteger.valueOf(i))
                            .build();
                    expressionModel.addContent(intConst);
                    break;
                    
                case 1:
                    // String constants
                    StringConstant strConst = newStringConstantBuilder()
                            .withValue("String value " + i)
                            .build();
                    expressionModel.addContent(strConst);
                    break;
                    
                case 2:
                    // Boolean constants
                    BooleanConstant boolConst = newBooleanConstantBuilder()
                            .withValue(i % 2 == 0)
                            .build();
                    expressionModel.addContent(boolConst);
                    break;
                    
                case 3:
                    // Integer arithmetic expressions
                    IntegerConstant left = newIntegerConstantBuilder()
                            .withValue(BigInteger.valueOf(i))
                            .build();
                    IntegerConstant right = newIntegerConstantBuilder()
                            .withValue(BigInteger.valueOf(i + 1))
                            .build();
                    hu.blackbelt.judo.meta.expression.numeric.IntegerArithmeticExpression arith = 
                            newIntegerArithmeticExpressionBuilder()
                                    .withLeft(left)
                                    .withRight(right)
                                    .withOperator(hu.blackbelt.judo.meta.expression.operator.IntegerOperator.ADD)
                                    .build();
                    expressionModel.addContent(arith);
                    break;
                    
                case 4:
                    // Decimal constants
                    hu.blackbelt.judo.meta.expression.constant.DecimalConstant decConst = 
                            newDecimalConstantBuilder()
                                    .withValue(new java.math.BigDecimal(i + ".5"))
                                    .build();
                    expressionModel.addContent(decConst);
                    break;
                    
                case 5:
                    // Instance (object expression)
                    TypeName instanceType = typeNames.get(i % typeNames.size());
                    hu.blackbelt.judo.meta.expression.constant.Instance instance = 
                            newInstanceBuilder()
                                    .withElementName(instanceType)
                                    .withName("instance" + i)
                                    .build();
                    expressionModel.addContent(instance);
                    break;
                    
                case 6:
                    // Logical negation
                    BooleanConstant innerBool = newBooleanConstantBuilder()
                            .withValue(true)
                            .build();
                    hu.blackbelt.judo.meta.expression.logical.NegationExpression neg = 
                            newNegationExpressionBuilder()
                                    .withExpression(innerBool)
                                    .build();
                    expressionModel.addContent(neg);
                    break;
                    
                case 7:
                    // String concatenation
                    StringConstant str1 = newStringConstantBuilder()
                            .withValue("Hello")
                            .build();
                    StringConstant str2 = newStringConstantBuilder()
                            .withValue("World")
                            .build();
                    hu.blackbelt.judo.meta.expression.string.Concatenate concat = 
                            newConcatenateBuilder()
                                    .withLeft(str1)
                                    .withRight(str2)
                                    .build();
                    expressionModel.addContent(concat);
                    break;
                    
                case 8:
                    // Integer comparison
                    IntegerConstant cmpLeft = newIntegerConstantBuilder()
                            .withValue(BigInteger.valueOf(i))
                            .build();
                    IntegerConstant cmpRight = newIntegerConstantBuilder()
                            .withValue(BigInteger.valueOf(i + 1))
                            .build();
                    hu.blackbelt.judo.meta.expression.logical.IntegerComparison cmp = 
                            newIntegerComparisonBuilder()
                                    .withLeft(cmpLeft)
                                    .withRight(cmpRight)
                                    .withOperator(hu.blackbelt.judo.meta.expression.operator.NumericComparator.LESS_THAN)
                                    .build();
                    expressionModel.addContent(cmp);
                    break;
                    
                case 9:
                    // Additional type name (to increase element count)
                    TypeName additionalType = newTypeNameBuilder()
                            .withName("AdditionalType" + i)
                            .withNamespace("test.additional")
                            .build();
                    expressionModel.addContent(additionalType);
                    break;
            }
        }
    }

    private int countModelElements() {
        final int[] count = {0};
        expressionModel.getResource().getAllContents().forEachRemaining(e -> count[0]++);
        return count[0];
    }

    private void runEvlValidation() throws Exception {
        try (BufferedSlf4jLogger bufferedLogger = new BufferedSlf4jLogger(log)) {
            final Map<String, Object> injections = new HashMap<>();
            injections.put("evaluator", new ExpressionEvaluator());
            injections.put("modelAdapter", mockModelAdapter);

            ExecutionContext executionContext = executionContextBuilder()
                    .log(bufferedLogger)
                    .resourceSet(expressionModel.getResourceSet())
                    .metaModels(emptyList())
                    .modelContexts(Arrays.asList(
                            wrappedEmfModelContextBuilder()
                                    .log(bufferedLogger)
                                    .name("EXPR")
                                    .resource(expressionModel.getResource())
                                    .validateModel(false)
                                    .useCache(false)
                                    .build()
                    ))
                    .injectContexts(injections)
                    .build();

            try {
                executionContext.load();
                executionContext.executeProgram(
                        evlExecutionContextBuilder()
                                .source(UriUtil.resolve("expression.evl",
                                        ExpressionScriptUriProvider.calculateExpressionValidationScriptURI()))
                                .expectedErrors(emptyList())
                                .expectedWarnings(emptyList())
                                .parallel(false)
                                .build()
                );
            } catch (Exception e) {
                // Validation errors are expected - we're just measuring time
            } finally {
                executionContext.commit();
                try {
                    executionContext.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    private void runJavaValidation(boolean parallel) throws Exception {
        try (BufferedSlf4jLogger bufferedLogger = new BufferedSlf4jLogger(log)) {
            try {
                ExpressionZetaValidator.validateExpression(
                        bufferedLogger,
                        expressionModel,
                        mockModelAdapter,
                        emptyList(),
                        emptyList(),
                        parallel
                );
            } catch (Exception e) {
                // Validation errors are expected - we're just measuring time
            }
        }
    }

    private double calculateAverage(List<Long> times) {
        return times.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }
}
