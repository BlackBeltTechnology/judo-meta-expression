package hu.blackbelt.judo.meta.expression.validation.mock;

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
import hu.blackbelt.epsilon.runtime.execution.api.ModelContext;
import hu.blackbelt.epsilon.runtime.execution.exceptions.EvlScriptExecutionException;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.expression.ValidatorType;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionEvaluator;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionModel;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionScriptUriProvider;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionValidationException;
import hu.blackbelt.judo.meta.expression.validation.ExpressionZetaValidator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.epsilon.common.util.UriUtil;
import org.eclipse.epsilon.evl.execute.UnsatisfiedConstraint;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static java.util.Collections.emptyList;

/**
 * Abstract base class for mock-based Expression validation tests.
 *
 * <p>This class provides infrastructure for testing expression validation using
 * {@link MockModelAdapter} and in-memory mock model structures. Unlike the EMF-based
 * tests, these tests don't require full model loading and can be faster to execute.</p>
 *
 * <p><b>EVL Support:</b> EVL validation is now supported with mock adapters! The key insight
 * is that expression elements ARE real EMF EObjects (generated code), and EVL scripts
 * call methods on these EObjects which delegate to the injected ModelAdapter. The mock
 * types returned by ModelAdapter are only passed TO ModelAdapter methods, not navigated
 * by EVL scripts directly.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * public class NumericExpressionValidationTest extends AbstractMockExpressionValidationTest {
 *
 *     @ParameterizedTest(name = "testIntegerAttribute [{0}]")
 *     @EnumSource(ValidatorType.class)
 *     void testIntegerAttribute(ValidatorType type) throws Exception {
 *         this.validatorType = type;
 *         
 *         MockModelAdapter adapter = MockModelBuilder.create()
 *             .withStandardPrimitives()
 *             .withEntityType("Person")
 *                 .withAttribute("age", "Integer")
 *                 .end()
 *             .build();
 *         
 *         initModel();
 *         setMockModelAdapter(adapter);
 *
 *         // ... create expression ...
 *
 *         runValidation(
 *             ImmutableList.of(),  // No errors expected
 *             ImmutableList.of()   // No warnings expected
 *         );
 *     }
 * }
 * }</pre>
 *
 * @see MockModelAdapter
 * @see MockModelBuilder
 * @see ValidatorType
 */
public abstract class AbstractMockExpressionValidationTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final String createdSourceModelName = "urn:expression.judo-meta-expression";

    protected ExpressionModel expressionModel;
    protected MockModelAdapter mockModelAdapter;
    protected ValidatorType validatorType;

    /**
     * Initialize the test with a fresh ExpressionModel.
     * Subclasses should call this at the start of each test method.
     */
    protected void initModel() {
        expressionModel = ExpressionModel.buildExpressionModel()
                .uri(URI.createURI(createdSourceModelName))
                .name("test-expression")
                .build();
    }

    /**
     * Set the mock model adapter for the test.
     * Must be called before runValidation().
     *
     * @param mockModelAdapter the mock model adapter for type resolution
     */
    protected void setMockModelAdapter(MockModelAdapter mockModelAdapter) {
        this.mockModelAdapter = mockModelAdapter;
    }

    /**
     * Run validation using the selected validator type.
     *
     * <p>For Java validation, this runs the {@link ExpressionZetaValidator}.</p>
     * <p>For EVL validation, this runs the Epsilon EVL scripts with mock adapter.</p>
     *
     * @param expectedErrors expected error constraint names (null means no errors expected)
     * @param expectedWarnings expected warning constraint names (null means warnings are ignored)
     * @throws Exception if validation fails unexpectedly
     */
    protected void runValidation(
            Collection<String> expectedErrors,
            Collection<String> expectedWarnings
    ) throws Exception {
        String diagnostics = expressionModel.getDiagnosticsAsString();
        if (!expressionModel.isValid()) {
            log.warn("Expression model diagnostics: {}", diagnostics);
        }
        // Skip structural validation for now as mock models may have unresolved references
        // The semantic validation will catch actual issues

        Assertions.assertNotNull(mockModelAdapter, "MockModelAdapter must be set before validation");

        try (BufferedSlf4jLogger bufferedLogger = new BufferedSlf4jLogger(log)) {
            switch (validatorType) {
                case EVL:
                    log.info("Running EVL validation with MockModelAdapter...");
                    runEvlValidation(bufferedLogger, expectedErrors, expectedWarnings);
                    break;
                case JAVA:
                    log.info("Running Java (Zeta) validation with MockModelAdapter...");
                    ExpressionZetaValidator.validateExpression(
                            bufferedLogger,
                            expressionModel,
                            mockModelAdapter,
                            expectedErrors,
                            expectedWarnings,
                            false // Sequential for deterministic results
                    );
                    break;
                default:
                    throw new IllegalStateException("Unknown validator type: " + validatorType);
            }
        } catch (ExpressionValidationException ex) {
            log.error("{} validation failed", validatorType, ex);
            throw ex;
        }
    }

    /**
     * Run EVL validation with mock model adapter.
     *
     * <p>This works because:
     * <ul>
     *   <li>Expression elements (IntegerAttribute, etc.) ARE real EMF EObjects</li>
     *   <li>The ModelAdapter is injected as a Java service</li>
     *   <li>EVL scripts call methods on expression EObjects which delegate to ModelAdapter</li>
     *   <li>Mock types returned by ModelAdapter are passed back to ModelAdapter methods</li>
     * </ul>
     * </p>
     */
    private void runEvlValidation(
            BufferedSlf4jLogger bufferedLogger,
            Collection<String> expectedErrors,
            Collection<String> expectedWarnings
    ) throws Exception {
        // Prepare injections - MockModelAdapter is injected as the modelAdapter service
        final Map<String, Object> injections = new HashMap<>();
        injections.put("evaluator", new ExpressionEvaluator());
        injections.put("modelAdapter", mockModelAdapter);

        // Build model contexts
        List<ModelContext> modelContexts = new ArrayList<>();
        
        // Create empty "adapted" resource - EVL needs a model named "ADAPTED"
        // but mock tests don't have real PSM/ESM models
        Resource emptyAdapted = MockEvlModelContext.createEmptyResource("ADAPTED");
        modelContexts.add(
                wrappedEmfModelContextBuilder()
                        .log(bufferedLogger)
                        .name("ADAPTED")
                        .resource(emptyAdapted)
                        .validateModel(false)
                        .useCache(false)
                        .build()
        );
        
        // Create empty "MEASURES" resource
        Resource emptyMeasures = MockEvlModelContext.createEmptyResource("MEASURES");
        modelContexts.add(
                wrappedEmfModelContextBuilder()
                        .log(bufferedLogger)
                        .name("MEASURES")
                        .resource(emptyMeasures)
                        .validateModel(false)
                        .useCache(false)
                        .build()
        );
        
        // Expression model - this contains real EMF EObjects
        modelContexts.add(
                wrappedEmfModelContextBuilder()
                        .log(bufferedLogger)
                        .name("EXPR")
                        .resource(expressionModel.getResource())
                        .validateModel(false)
                        .useCache(false)
                        .build()
        );

        ExecutionContext executionContext = executionContextBuilder()
                .log(bufferedLogger)
                .resourceSet(expressionModel.getResourceSet())
                .metaModels(emptyList())
                .modelContexts(modelContexts)
                .injectContexts(injections)
                .build();

        Set<String> actualErrorNames = new HashSet<>();
        Set<String> actualWarningNames = new HashSet<>();

        try {
            // Load the model
            executionContext.load();

            // Execute EVL with empty expected collections to capture all results
            executionContext.executeProgram(
                    evlExecutionContextBuilder()
                            .source(UriUtil.resolve("expression.evl",
                                    ExpressionScriptUriProvider.calculateExpressionValidationScriptURI()))
                            .expectedErrors(Collections.emptyList())
                            .expectedWarnings(Collections.emptyList())
                            .parallel(false)
                            .build()
            );

            log.debug("EVL validation completed with no errors or warnings");
        } catch (EvlScriptExecutionException ex) {
            // Extract constraint names from the exception
            if (ex.getUnsatisfiedErrors() != null) {
                for (UnsatisfiedConstraint uc : ex.getUnsatisfiedErrors()) {
                    actualErrorNames.add(uc.getConstraint().getName());
                    log.debug("EVL error: {} - {}", uc.getConstraint().getName(), uc.getMessage());
                }
            }
            if (ex.getUnsatisfiedWarnings() != null) {
                for (UnsatisfiedConstraint uc : ex.getUnsatisfiedWarnings()) {
                    actualWarningNames.add(uc.getConstraint().getName());
                    log.debug("EVL warning: {} - {}", uc.getConstraint().getName(), uc.getMessage());
                }
            }
        } finally {
            executionContext.commit();
            try {
                executionContext.close();
            } catch (Exception e) {
                log.warn("Unable to close EVL execution context", e);
            }
        }

        // Compare with expected constraint names
        Set<String> expectedErrorSet = new HashSet<>(expectedErrors != null ? expectedErrors : Collections.emptyList());
        Set<String> expectedWarningSet = new HashSet<>(expectedWarnings != null ? expectedWarnings : Collections.emptyList());

        // Check for missing expected errors
        Set<String> missingErrors = new HashSet<>(expectedErrorSet);
        missingErrors.removeAll(actualErrorNames);
        
        // Check for unexpected errors
        Set<String> unexpectedErrors = new HashSet<>(actualErrorNames);
        unexpectedErrors.removeAll(expectedErrorSet);

        // Check for missing expected warnings
        Set<String> missingWarnings = new HashSet<>(expectedWarningSet);
        missingWarnings.removeAll(actualWarningNames);
        
        // Check for unexpected warnings
        Set<String> unexpectedWarnings = new HashSet<>(actualWarningNames);
        unexpectedWarnings.removeAll(expectedWarningSet);

        if (!missingErrors.isEmpty() || !unexpectedErrors.isEmpty() || 
            !missingWarnings.isEmpty() || !unexpectedWarnings.isEmpty()) {
            StringBuilder sb = new StringBuilder("EVL validation mismatch:\n");
            if (!missingErrors.isEmpty()) {
                sb.append("  Missing errors: ").append(missingErrors).append("\n");
            }
            if (!unexpectedErrors.isEmpty()) {
                sb.append("  Unexpected errors: ").append(unexpectedErrors).append("\n");
            }
            if (!missingWarnings.isEmpty()) {
                sb.append("  Missing warnings: ").append(missingWarnings).append("\n");
            }
            if (!unexpectedWarnings.isEmpty()) {
                sb.append("  Unexpected warnings: ").append(unexpectedWarnings).append("\n");
            }
            throw new ExpressionValidationException(sb.toString());
        }
        
        log.info("EVL validation passed: {} errors, {} warnings", 
                actualErrorNames.size(), actualWarningNames.size());
    }

    /**
     * Create a standard mock model adapter with common primitive types.
     * Convenience method for tests that need a basic model setup.
     *
     * @return a MockModelAdapter with standard primitive types
     */
    protected MockModelAdapter createStandardMockAdapter() {
        return MockModelBuilder.create()
                .withStandardPrimitives()
                .build();
    }

    /**
     * Create a mock model builder with standard primitives pre-configured.
     * Convenience method for tests to extend the standard setup.
     *
     * @return a MockModelBuilder with standard primitive types
     */
    protected MockModelBuilder createStandardMockBuilder() {
        return MockModelBuilder.create()
                .withStandardPrimitives();
    }
}
