package hu.blackbelt.judo.meta.expression.validation.rules.numeric;

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

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.numeric.*;
import hu.blackbelt.judo.meta.expression.operator.DecimalOperator;
import hu.blackbelt.judo.meta.expression.validation.ExpressionValidationContext;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.Critique;
import hu.blackbelt.judo.zeta.annotation.Guard;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;
import org.eclipse.emf.ecore.EObject;

/**
 * Validation rules for numeric expression elements.
 *
 * <p>Corresponds to constraints in numeric.evl</p>
 */
@ValidationContext(NumericExpression.class)
public class NumericExpressionValidations {

    /**
     * Generic resolved check for numeric expressions - always passes for supported types.
     */
    @Constraint(
            name = ValidationConstants.RESOLVED,
            message = "Unsupported expression"
    )
    public ValidationRule resolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
            return ValidationResult.pass();
        };
    }

    // =========================================================================
    // IntegerAttribute
    // =========================================================================

    /**
     * Guard: Check if element is IntegerAttribute.
     */
    public boolean isIntegerAttributeAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof IntegerAttribute)) {
            return false;
        }
        // Check resolution inline rather than depending on resolved() having run first,
        // since Zeta validation does not guarantee rule execution order
        IntegerAttribute self = (IntegerAttribute) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        ModelAdapter modelAdapter = exprCtx.getModelAdapter();
        Object attributeTypeObj = self.getAttributeType(modelAdapter);
        if (attributeTypeObj instanceof java.util.Optional) {
            return ((java.util.Optional<?>) attributeTypeObj).isPresent();
        }
        return attributeTypeObj != null;
    }

    /**
     * Validates that IntegerAttribute's attribute type is numeric.
     */
    @Constraint(
            name = ValidationConstants.ATTRIBUTE_TYPE_IS_INTEGER,
            message = "Attribute type is not numeric"
    )
    @Guard(method = "isIntegerAttributeAndResolved")
    public ValidationRule attributeTypeIsInteger() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            IntegerAttribute self = (IntegerAttribute) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            // getAttributeType returns Optional<P> wrapped in Object
            Object attributeTypeObj = self.getAttributeType(modelAdapter);
            if (attributeTypeObj instanceof java.util.Optional) {
                java.util.Optional<?> optionalType = (java.util.Optional<?>) attributeTypeObj;
                if (optionalType.isPresent() && modelAdapter.isNumeric(optionalType.get())) {
                    return ValidationResult.pass();
                }
            } else if (attributeTypeObj != null && modelAdapter.isNumeric(attributeTypeObj)) {
                return ValidationResult.pass();
            }

            String objectTypeName = self.getObjectExpression() != null
                    ? String.valueOf(self.getObjectExpression().getObjectType(modelAdapter))
                    : "unknown";
            return ValidationResult.fail(
                    "Attribute type of " + self.getAttributeName() +
                    " of object type " + objectTypeName + " is not numeric"
            );
        };
    }

    // =========================================================================
    // DecimalAttribute
    // =========================================================================

    /**
     * Guard: Check if element is DecimalAttribute.
     */
    public boolean isDecimalAttributeAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof DecimalAttribute)) {
            return false;
        }
        // Check resolution inline rather than depending on resolved() having run first,
        // since Zeta validation does not guarantee rule execution order
        DecimalAttribute self = (DecimalAttribute) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        ModelAdapter modelAdapter = exprCtx.getModelAdapter();
        Object attributeTypeObj = self.getAttributeType(modelAdapter);
        if (attributeTypeObj instanceof java.util.Optional) {
            return ((java.util.Optional<?>) attributeTypeObj).isPresent();
        }
        return attributeTypeObj != null;
    }

    /**
     * Validates that DecimalAttribute's attribute type is numeric.
     */
    @Constraint(
            name = ValidationConstants.ATTRIBUTE_TYPE_IS_DECIMAL,
            message = "Attribute type is not numeric"
    )
    @Guard(method = "isDecimalAttributeAndResolved")
    public ValidationRule attributeTypeIsDecimal() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            DecimalAttribute self = (DecimalAttribute) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            // getAttributeType returns Optional<P> wrapped in Object
            Object attributeTypeObj = self.getAttributeType(modelAdapter);
            if (attributeTypeObj instanceof java.util.Optional) {
                java.util.Optional<?> optionalType = (java.util.Optional<?>) attributeTypeObj;
                if (optionalType.isPresent() && modelAdapter.isNumeric(optionalType.get())) {
                    return ValidationResult.pass();
                }
            } else if (attributeTypeObj != null && modelAdapter.isNumeric(attributeTypeObj)) {
                return ValidationResult.pass();
            }

            String objectTypeName = self.getObjectExpression() != null
                    ? String.valueOf(self.getObjectExpression().getObjectType(modelAdapter))
                    : "unknown";
            return ValidationResult.fail(
                    "Attribute type of " + self.getAttributeName() +
                    " of object type " + objectTypeName + " is not numeric"
            );
        };
    }

    // =========================================================================
    // DecimalArithmeticExpression Critique
    // =========================================================================

    /**
     * Guard: Check if element is DecimalArithmeticExpression and resolved.
     */
    public boolean isDecimalArithmeticExpressionAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        // No need to check satisfies(RESOLVED) since resolved() always passes for NumericExpression
        return element instanceof DecimalArithmeticExpression;
    }

    /**
     * Critique: Recommends using integer arithmetic when both operands are integers.
     */
    @Critique(
            name = ValidationConstants.INTEGER_ARITHMETIC_EXPRESSION_IS_RECOMMENDED,
            message = "Both arguments are integer so integer arithmetic expression is recommended"
    )
    @Guard(method = "isDecimalArithmeticExpressionAndResolved")
    public ValidationRule integerArithmeticExpressionIsRecommended() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            DecimalArithmeticExpression self = (DecimalArithmeticExpression) element;

            boolean bothInteger = self.getLeft() instanceof IntegerExpression 
                    && self.getRight() instanceof IntegerExpression;
            boolean isAddSubMul = self.getOperator() == DecimalOperator.ADD 
                    || self.getOperator() == DecimalOperator.SUBSTRACT 
                    || self.getOperator() == DecimalOperator.MULTIPLY;

            if (bothInteger && isAddSubMul) {
                return ValidationResult.fail(
                        "Both arguments are integer so integer arithmetic expression is recommended in: " + self
                );
            }
            return ValidationResult.pass();
        };
    }

    // =========================================================================
    // IntegerAggregatedExpression
    // =========================================================================

    /**
     * Guard: Check if element is IntegerAggregatedExpression.
     */
    public boolean isIntegerAggregatedExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof IntegerAggregatedExpression;
    }

    /**
     * Validates IntegerAggregatedExpression is resolved.
     */
    @Constraint(
            name = "IntegerAggregatedExpressionResolved",
            message = "Attribute not found"
    )
    @Guard(method = "isIntegerAggregatedExpression")
    public ValidationRule integerAggregatedExpressionResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            IntegerAggregatedExpression self = (IntegerAggregatedExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (self.getCollectionExpression() == null || 
                !exprCtx.satisfies(self.getCollectionExpression(), ValidationConstants.RESOLVED)) {
                return ValidationResult.pass(); // Skip if collection not resolved
            }

            if (self.getObjectType(modelAdapter) != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            String objectTypeName = self.getCollectionExpression().getObjectType(modelAdapter) != null
                    ? String.valueOf(self.getCollectionExpression().getObjectType(modelAdapter))
                    : "unknown";
            return ValidationResult.fail(
                    "Attribute named " + self.getVariableName() + 
                    " of object type " + objectTypeName + " not found"
            );
        };
    }

    // =========================================================================
    // DecimalAggregatedExpression
    // =========================================================================

    /**
     * Guard: Check if element is DecimalAggregatedExpression.
     */
    public boolean isDecimalAggregatedExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof DecimalAggregatedExpression;
    }

    /**
     * Validates DecimalAggregatedExpression is resolved.
     */
    @Constraint(
            name = "DecimalAggregatedExpressionResolved",
            message = "Attribute not found"
    )
    @Guard(method = "isDecimalAggregatedExpression")
    public ValidationRule decimalAggregatedExpressionResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            DecimalAggregatedExpression self = (DecimalAggregatedExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (self.getCollectionExpression() == null || 
                !exprCtx.satisfies(self.getCollectionExpression(), ValidationConstants.RESOLVED)) {
                return ValidationResult.pass(); // Skip if collection not resolved
            }

            if (self.getObjectType(modelAdapter) != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            String objectTypeName = self.getCollectionExpression().getObjectType(modelAdapter) != null
                    ? String.valueOf(self.getCollectionExpression().getObjectType(modelAdapter))
                    : "unknown";
            return ValidationResult.fail(
                    "Attribute named " + self.getVariableName() + 
                    " of object type " + objectTypeName + " not found"
            );
        };
    }

    // =========================================================================
    // Switch Expression Default Case Validations
    // =========================================================================

    /**
     * Guard: Check if element is IntegerSwitchExpression with default.
     */
    public boolean isIntegerSwitchWithDefault(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof IntegerSwitchExpression)) {
            return false;
        }
        IntegerSwitchExpression self = (IntegerSwitchExpression) element;
        return self.getDefaultExpression() != null;
    }

    /**
     * Guard: Check if element is DecimalSwitchExpression with default.
     */
    public boolean isDecimalSwitchWithDefault(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof DecimalSwitchExpression)) {
            return false;
        }
        DecimalSwitchExpression self = (DecimalSwitchExpression) element;
        return self.getDefaultExpression() != null;
    }

    /**
     * Validates that IntegerSwitchExpression default case is numeric.
     */
    @Constraint(
            name = ValidationConstants.TYPE_OF_DEFAULT_CASE_IS_NUMERIC,
            message = "Type of default case expression is not numeric"
    )
    @Guard(method = "isIntegerSwitchWithDefault")
    public ValidationRule integerSwitchDefaultIsNumeric() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            IntegerSwitchExpression self = (IntegerSwitchExpression) element;
            if (self.getDefaultExpression() instanceof NumericExpression) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail(
                    "Type of default case expression is not numeric: " + self.getDefaultExpression()
            );
        };
    }

    /**
     * Validates that DecimalSwitchExpression default case is numeric.
     */
    @Constraint(
            name = ValidationConstants.TYPE_OF_DEFAULT_CASE_IS_NUMERIC,
            message = "Type of default case expression is not numeric"
    )
    @Guard(method = "isDecimalSwitchWithDefault")
    public ValidationRule decimalSwitchDefaultIsNumeric() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            DecimalSwitchExpression self = (DecimalSwitchExpression) element;
            if (self.getDefaultExpression() instanceof NumericExpression) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail(
                    "Type of default case expression is not numeric: " + self.getDefaultExpression()
            );
        };
    }

    // =========================================================================
    // Switch Case Validations (validated from parent switch expression context)
    // =========================================================================

    /**
     * Guard: Check if element is IntegerSwitchExpression with cases.
     */
    public boolean isIntegerSwitchWithCases(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof IntegerSwitchExpression)) {
            return false;
        }
        IntegerSwitchExpression self = (IntegerSwitchExpression) element;
        return self.getCases() != null && !self.getCases().isEmpty();
    }

    /**
     * Guard: Check if element is DecimalSwitchExpression with cases.
     */
    public boolean isDecimalSwitchWithCases(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof DecimalSwitchExpression)) {
            return false;
        }
        DecimalSwitchExpression self = (DecimalSwitchExpression) element;
        return self.getCases() != null && !self.getCases().isEmpty();
    }

    /**
     * Validates that all switch case expressions in IntegerSwitchExpression are numeric.
     */
    @Constraint(
            name = ValidationConstants.TYPE_OF_SWITCH_CASE_IS_NUMERIC,
            message = "Type of switch case expression is not numeric"
    )
    @Guard(method = "isIntegerSwitchWithCases")
    public ValidationRule integerSwitchCasesAreNumeric() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            IntegerSwitchExpression self = (IntegerSwitchExpression) element;
            for (SwitchCase switchCase : self.getCases()) {
                if (!(switchCase.getExpression() instanceof NumericExpression)) {
                    return ValidationResult.fail(
                            "Type of switch case expression is not numeric: " + switchCase.getExpression()
                    );
                }
            }
            return ValidationResult.pass();
        };
    }

    /**
     * Validates that all switch case expressions in DecimalSwitchExpression are numeric.
     */
    @Constraint(
            name = ValidationConstants.TYPE_OF_SWITCH_CASE_IS_NUMERIC,
            message = "Type of switch case expression is not numeric"
    )
    @Guard(method = "isDecimalSwitchWithCases")
    public ValidationRule decimalSwitchCasesAreNumeric() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            DecimalSwitchExpression self = (DecimalSwitchExpression) element;
            for (SwitchCase switchCase : self.getCases()) {
                if (!(switchCase.getExpression() instanceof NumericExpression)) {
                    return ValidationResult.fail(
                            "Type of switch case expression is not numeric: " + switchCase.getExpression()
                    );
                }
            }
            return ValidationResult.pass();
        };
    }
}
