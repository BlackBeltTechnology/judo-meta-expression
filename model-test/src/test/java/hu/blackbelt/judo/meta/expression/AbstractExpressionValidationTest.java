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
import hu.blackbelt.epsilon.runtime.execution.api.ModelContext;
import hu.blackbelt.epsilon.runtime.execution.exceptions.EvlScriptExecutionException;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
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
 * Abstract base class for Expression validation tests.
 *
 * <p>Provides common infrastructure for testing both EVL and Java (Zeta) validators
 * with the same test cases using JUnit 5 parameterized tests.</p>
 *
 * <p>The key challenge is that EVL and Java validators use different expected error formats:
 * <ul>
 *   <li>EVL uses: {@code "ConstraintName|Full error message with context"}</li>
 *   <li>Java uses: {@code "ConstraintName"} (just the constraint name)</li>
 * </ul>
 *
 * <p>This base class handles this difference by running EVL validation with empty
 * expected collections to force it to throw an exception with all results, then
 * extracting constraint names for comparison.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * public class ExpressionValidationTypeNameTest extends AbstractExpressionValidationTest {
 *
 *     @ParameterizedTest(name = "testObjectTypeIsValid [{0}]")
 *     @EnumSource(ValidatorType.class)
 *     void testObjectTypeIsValid(ValidatorType type) throws Exception {
 *         this.validatorType = type;
 *         initModel();
 *
 *         // ... build model ...
 *
 *         runValidation(
 *             ImmutableList.of("ObjectTypeIsValid"),
 *             ImmutableList.of()
 *         );
 *     }
 * }
 * }</pre>
 *
 * @see ValidatorType
 * @see ExpressionZetaValidator
 */
public abstract class AbstractExpressionValidationTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final String createdSourceModelName = "urn:expression.judo-meta-expression";

    protected ExpressionModel expressionModel;
    protected ModelAdapter modelAdapter;
    protected Resource adaptedResource;
    protected Resource measureResource;
    protected String adaptedName = "ADAPTED";
    protected String measureName = "MEASURES";
    protected ValidatorType validatorType;

    /**
     * Initialize the test with a fresh ExpressionModel.
     * Subclasses should call this at the start of each parameterized test method.
     */
    protected void initModel() {
        expressionModel = ExpressionModel.buildExpressionModel()
                .uri(URI.createURI(createdSourceModelName))
                .build();
    }

    /**
     * Set the model adapter for the test.
     * Must be called before runValidation().
     *
     * @param modelAdapter the model adapter for type resolution
     */
    protected void setModelAdapter(ModelAdapter modelAdapter) {
        this.modelAdapter = modelAdapter;
    }

    /**
     * Set the adapted resource (e.g., ASM, PSM model).
     *
     * @param adaptedResource the adapted resource
     * @param adaptedName the name for the adapted model in EVL
     */
    protected void setAdaptedResource(Resource adaptedResource, String adaptedName) {
        this.adaptedResource = adaptedResource;
        this.adaptedName = adaptedName;
    }

    /**
     * Set the measure resource.
     *
     * @param measureResource the measure resource
     * @param measureName the name for the measure model in EVL
     */
    protected void setMeasureResource(Resource measureResource, String measureName) {
        this.measureResource = measureResource;
        this.measureName = measureName;
    }

    /**
     * Run validation using the selected validator type.
     *
     * <p>For EVL validation, this method handles the format difference by:
     * <ol>
     *   <li>Running EVL with empty expected collections to force exception</li>
     *   <li>Extracting constraint names from the exception</li>
     *   <li>Comparing against expected constraint names</li>
     * </ol>
     *
     * @param expectedErrors expected error constraint names (null means no errors expected)
     * @param expectedWarnings expected warning constraint names (null means warnings are ignored)
     * @throws Exception if validation fails unexpectedly
     */
    protected void runValidation(
            Collection<String> expectedErrors,
            Collection<String> expectedWarnings
    ) throws Exception {
        log.debug("Expression diagnostics: {}", expressionModel.getDiagnosticsAsString());
        Assertions.assertTrue(
                expressionModel.isValid(),
                "Model should be structurally valid"
        );

        Assertions.assertNotNull(modelAdapter, "ModelAdapter must be set before validation");

        try (BufferedSlf4jLogger bufferedLogger = new BufferedSlf4jLogger(log)) {
            switch (validatorType) {
                case EVL:
                    log.info("Running EVL validation...");
                    runEvlValidation(bufferedLogger, expectedErrors, expectedWarnings);
                    break;
                case JAVA:
                    log.info("Running Java (Zeta) validation...");
                    ExpressionZetaValidator.validateExpression(
                            bufferedLogger,
                            expressionModel,
                            modelAdapter,
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
     * Run EVL validation with constraint name matching (not full key matching).
     *
     * <p>This method runs EVL validation with empty expected collections to force
     * it to throw an exception containing the actual results. We then extract
     * constraint names from the unsatisfied constraints for comparison.</p>
     */
    private void runEvlValidation(
            BufferedSlf4jLogger bufferedLogger,
            Collection<String> expectedErrors,
            Collection<String> expectedWarnings
    ) throws Exception {
        // Prepare injections
        final Map<String, Object> injections = new HashMap<>();
        injections.put("evaluator", new ExpressionEvaluator());
        injections.put("modelAdapter", modelAdapter);

        // Build model contexts
        List<ModelContext> modelContexts = new ArrayList<>();
        
        if (adaptedResource != null) {
            modelContexts.add(
                    wrappedEmfModelContextBuilder()
                            .log(bufferedLogger)
                            .name(adaptedName)
                            .resource(adaptedResource)
                            .validateModel(false)
                            .useCache(true)
                            .build()
            );
        }
        
        if (measureResource != null) {
            modelContexts.add(
                    wrappedEmfModelContextBuilder()
                            .log(bufferedLogger)
                            .name(measureName)
                            .resource(measureResource)
                            .validateModel(false)
                            .useCache(true)
                            .build()
            );
        }
        
        modelContexts.add(
                wrappedEmfModelContextBuilder()
                        .log(bufferedLogger)
                        .name("EXPR")
                        .resource(expressionModel.getResource())
                        .validateModel(false)
                        .useCache(true)
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

            // Execute EVL with empty expected collections
            // This forces EVL to throw exception if there are any errors/warnings
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
        validateResults(expectedErrors, expectedWarnings, actualErrorNames, actualWarningNames);
    }

    /**
     * Validate results against expected errors and warnings.
     */
    private void validateResults(
            Collection<String> expectedErrors,
            Collection<String> expectedWarnings,
            Set<String> actualErrorNames,
            Set<String> actualWarningNames
    ) throws ExpressionValidationException {
        Set<String> expectedErrorSet = expectedErrors != null
                ? new HashSet<>(expectedErrors)
                : Collections.emptySet();

        Set<String> expectedWarningSet = expectedWarnings != null
                ? new HashSet<>(expectedWarnings)
                : null; // null means "don't care about warnings"

        // Check errors
        Set<String> unexpectedErrors = new HashSet<>(actualErrorNames);
        unexpectedErrors.removeAll(expectedErrorSet);

        Set<String> missingErrors = new HashSet<>(expectedErrorSet);
        missingErrors.removeAll(actualErrorNames);

        // Check warnings only if expectedWarnings was explicitly provided
        Set<String> unexpectedWarnings = Collections.emptySet();
        Set<String> missingWarnings = Collections.emptySet();
        if (expectedWarningSet != null) {
            unexpectedWarnings = new HashSet<>(actualWarningNames);
            unexpectedWarnings.removeAll(expectedWarningSet);
            missingWarnings = new HashSet<>(expectedWarningSet);
            missingWarnings.removeAll(actualWarningNames);
        }

        boolean hasIssues = !unexpectedErrors.isEmpty()
                || !missingErrors.isEmpty()
                || !unexpectedWarnings.isEmpty()
                || !missingWarnings.isEmpty();

        if (hasIssues) {
            StringBuilder sb = new StringBuilder("EVL validation result mismatch:\n");
            sb.append("  Actual errors: ").append(actualErrorNames).append("\n");
            sb.append("  Expected errors: ").append(expectedErrorSet).append("\n");
            sb.append("  Actual warnings: ").append(actualWarningNames).append("\n");
            sb.append("  Expected warnings: ").append(expectedWarningSet).append("\n");
            if (!unexpectedErrors.isEmpty()) {
                sb.append("  Unexpected errors: ").append(unexpectedErrors).append("\n");
            }
            if (!missingErrors.isEmpty()) {
                sb.append("  Missing errors: ").append(missingErrors).append("\n");
            }
            if (!unexpectedWarnings.isEmpty()) {
                sb.append("  Unexpected warnings: ").append(unexpectedWarnings).append("\n");
            }
            if (!missingWarnings.isEmpty()) {
                sb.append("  Missing warnings: ").append(missingWarnings).append("\n");
            }
            log.error(sb.toString());
            throw new ExpressionValidationException(sb.toString());
        }

        log.info("EVL validation passed: {} errors, {} warnings",
                actualErrorNames.size(), actualWarningNames.size());
    }
}
