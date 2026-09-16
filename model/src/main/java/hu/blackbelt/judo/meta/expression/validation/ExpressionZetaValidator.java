package hu.blackbelt.judo.meta.expression.validation;

/*-
 * #%L
 * Judo :: Expression :: Model
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

import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionEvaluator;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionModel;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionValidationException;
import hu.blackbelt.judo.zeta.validation.core.Severity;
import hu.blackbelt.judo.zeta.validation.core.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationExecutor;
import hu.blackbelt.judo.zeta.validation.core.ValidationRegistry;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Entry point for Zeta-based Java Expression model validation.
 *
 * <p>This validator provides a native Java alternative to EVL (Epsilon Validation Language)
 * validation with better IDE integration, debugging support, and performance.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * ExpressionZetaValidator.validateExpression(log, expressionModel, modelAdapter);
 * }</pre>
 */
public class ExpressionZetaValidator {

    private static final List<Class<?>> VALIDATOR_CLASSES = List.of(
            // Expression core validations
            hu.blackbelt.judo.meta.expression.validation.rules.expression.TypeNameValidations.class,
            hu.blackbelt.judo.meta.expression.validation.rules.expression.ExpressionValidations.class,
            
            // Numeric validations
            hu.blackbelt.judo.meta.expression.validation.rules.numeric.NumericExpressionValidations.class,
            
            // Object validations
            hu.blackbelt.judo.meta.expression.validation.rules.object.ObjectExpressionValidations.class,
            
            // Collection validations
            hu.blackbelt.judo.meta.expression.validation.rules.collection.CollectionExpressionValidations.class,
            
            // Logical validations
            hu.blackbelt.judo.meta.expression.validation.rules.logical.LogicalExpressionValidations.class,
            
            // String validations
            hu.blackbelt.judo.meta.expression.validation.rules.string.StringExpressionValidations.class,
            
            // Attribute validations
            hu.blackbelt.judo.meta.expression.validation.rules.attribute.AttributeValidations.class,
            
            // Temporal validations
            hu.blackbelt.judo.meta.expression.validation.rules.temporal.TemporalExpressionValidations.class,
            
            // Measured validations
            hu.blackbelt.judo.meta.expression.validation.rules.measured.MeasuredExpressionValidations.class,
            
            // Enumeration validations
            hu.blackbelt.judo.meta.expression.validation.rules.enumeration.EnumerationExpressionValidations.class,
            
            // Constant validations
            hu.blackbelt.judo.meta.expression.validation.rules.constant.ConstantExpressionValidations.class,
            
            // Custom validations
            hu.blackbelt.judo.meta.expression.validation.rules.custom.CustomExpressionValidations.class,
            
            // Binding validations
            hu.blackbelt.judo.meta.expression.validation.rules.binding.AttributeBindingValidations.class,
            hu.blackbelt.judo.meta.expression.validation.rules.binding.ReferenceBindingValidations.class,
            hu.blackbelt.judo.meta.expression.validation.rules.binding.FilterBindingValidations.class
    );

    /**
     * Validate Expression model using Zeta validation rules.
     *
     * @param log the logger
     * @param expressionModel the model to validate
     * @param modelAdapter the model adapter for type resolution
     * @throws ExpressionValidationException if validation fails
     */
    public static void validateExpression(
            Logger log,
            ExpressionModel expressionModel,
            ModelAdapter modelAdapter
    ) throws ExpressionValidationException {
        validateExpression(log, expressionModel, modelAdapter, null, null, false);
    }

    /**
     * Validate Expression model with expected errors and warnings (for testing).
     *
     * @param log the logger
     * @param expressionModel the model to validate
     * @param modelAdapter the model adapter for type resolution
     * @param expectedErrors expected error constraint names
     * @param expectedWarnings expected warning constraint names
     * @throws ExpressionValidationException if validation fails
     */
    public static void validateExpression(
            Logger log,
            ExpressionModel expressionModel,
            ModelAdapter modelAdapter,
            Collection<String> expectedErrors,
            Collection<String> expectedWarnings
    ) throws ExpressionValidationException {
        validateExpression(log, expressionModel, modelAdapter, expectedErrors, expectedWarnings, false);
    }

    /**
     * Validate Expression model with all options.
     *
     * @param log the logger
     * @param expressionModel the model to validate
     * @param modelAdapter the model adapter for type resolution
     * @param expectedErrors expected error constraint names
     * @param expectedWarnings expected warning constraint names
     * @param parallel use parallel execution
     * @throws ExpressionValidationException if validation fails
     */
    public static void validateExpression(
            Logger log,
            ExpressionModel expressionModel,
            ModelAdapter modelAdapter,
            Collection<String> expectedErrors,
            Collection<String> expectedWarnings,
            boolean parallel
    ) throws ExpressionValidationException {
        log.info("Starting Zeta-based Expression validation...");

        // Create validation infrastructure
        ValidationRegistry registry = new ValidationRegistry();
        ExpressionEvaluator evaluator = new ExpressionEvaluator();

        // Create validation context with model adapter and evaluator
        ValidationContext context = new ExpressionValidationContext(
                expressionModel,
                modelAdapter,
                evaluator
        );

        // Register validation rule classes
        for (Class<?> validatorClass : VALIDATOR_CLASSES) {
            try {
                registry.register(validatorClass);
                log.debug("Registered validation rules from: {}", validatorClass.getSimpleName());
            } catch (Exception e) {
                log.warn("Failed to register validator class: {} - {}", 
                        validatorClass.getName(), e.getMessage());
            }
        }

        // Collect all model elements
        List<EObject> allElements = collectAllElements(expressionModel);
        log.debug("Validating {} elements", allElements.size());

        // Execute validation
        ValidationExecutor executor = new ValidationExecutor(registry, context, parallel);
        List<ValidationResult> results;
        try {
            results = executor.validate(allElements);
        } finally {
            executor.shutdown();
        }

        // Separate errors and warnings
        List<String> errors = results.stream()
                .filter(r -> r.getSeverity() == Severity.ERROR)
                .map(ValidationResult::getConstraintName)
                .collect(Collectors.toList());

        List<String> warnings = results.stream()
                .filter(r -> r.getSeverity() == Severity.WARNING)
                .map(ValidationResult::getConstraintName)
                .collect(Collectors.toList());

        log.info("Zeta validation completed: {} errors, {} warnings", errors.size(), warnings.size());

        // Validate against expectations
        validateResults(log, expectedErrors, expectedWarnings, errors, warnings);
    }

    /**
     * Collect all model elements for validation.
     */
    private static List<EObject> collectAllElements(ExpressionModel expressionModel) {
        List<EObject> elements = new ArrayList<>();
        expressionModel.getResource().getAllContents().forEachRemaining(elements::add);
        return elements;
    }

    /**
     * Validate results against expected errors and warnings.
     */
    private static void validateResults(
            Logger log,
            Collection<String> expectedErrors,
            Collection<String> expectedWarnings,
            List<String> actualErrors,
            List<String> actualWarnings
    ) throws ExpressionValidationException {
        Set<String> expectedErrorSet = expectedErrors != null
                ? new HashSet<>(expectedErrors)
                : Collections.emptySet();

        Set<String> expectedWarningSet = expectedWarnings != null
                ? new HashSet<>(expectedWarnings)
                : null; // null means "don't care about warnings"

        Set<String> actualErrorSet = new HashSet<>(actualErrors);
        Set<String> actualWarningSet = new HashSet<>(actualWarnings);

        // Check errors
        Set<String> unexpectedErrors = new HashSet<>(actualErrorSet);
        unexpectedErrors.removeAll(expectedErrorSet);

        Set<String> missingErrors = new HashSet<>(expectedErrorSet);
        missingErrors.removeAll(actualErrorSet);

        // Check warnings only if expectedWarnings was explicitly provided
        Set<String> unexpectedWarnings = Collections.emptySet();
        Set<String> missingWarnings = Collections.emptySet();
        if (expectedWarningSet != null) {
            unexpectedWarnings = new HashSet<>(actualWarningSet);
            unexpectedWarnings.removeAll(expectedWarningSet);
            missingWarnings = new HashSet<>(expectedWarningSet);
            missingWarnings.removeAll(actualWarningSet);
        }

        boolean hasIssues = !unexpectedErrors.isEmpty()
                || !missingErrors.isEmpty()
                || !unexpectedWarnings.isEmpty()
                || !missingWarnings.isEmpty();

        if (hasIssues) {
            StringBuilder sb = new StringBuilder("Zeta validation result mismatch:\n");
            sb.append("  Actual errors: ").append(actualErrorSet).append("\n");
            sb.append("  Expected errors: ").append(expectedErrorSet).append("\n");
            sb.append("  Actual warnings: ").append(actualWarningSet).append("\n");
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

        log.info("Zeta validation passed: {} errors, {} warnings",
                actualErrorSet.size(), actualWarningSet.size());
    }
}
