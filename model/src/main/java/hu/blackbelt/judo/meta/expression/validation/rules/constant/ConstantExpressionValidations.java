package hu.blackbelt.judo.meta.expression.validation.rules.constant;

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

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.constant.*;
import hu.blackbelt.judo.meta.expression.variable.*;
import hu.blackbelt.judo.meta.expression.validation.ExpressionValidationContext;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.Guard;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;
import org.eclipse.emf.ecore.EObject;

/**
 * Validation rules for constant expression elements.
 *
 * <p>Corresponds to constraints in constant.evl</p>
 */
@ValidationContext(Expression.class)
public class ConstantExpressionValidations {

    // =========================================================================
    // IntegerConstant
    // =========================================================================

    /**
     * Guard: Check if element is IntegerConstant.
     */
    public boolean isIntegerConstant(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof IntegerConstant;
    }

    /**
     * Validates IntegerConstant value is defined.
     */
    @Constraint(
            name = "IntegerConstantResolved",
            message = "Value of integer constant is not defined"
    )
    @Guard(method = "isIntegerConstant")
    public ValidationRule integerConstantResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            IntegerConstant self = (IntegerConstant) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            if (self.getValue() != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Value of integer constant is not defined: " + self);
        };
    }

    // =========================================================================
    // DecimalConstant
    // =========================================================================

    /**
     * Guard: Check if element is DecimalConstant.
     */
    public boolean isDecimalConstant(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof DecimalConstant;
    }

    /**
     * Validates DecimalConstant value is defined.
     */
    @Constraint(
            name = "DecimalConstantResolved",
            message = "Value of decimal constant is not defined"
    )
    @Guard(method = "isDecimalConstant")
    public ValidationRule decimalConstantResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            DecimalConstant self = (DecimalConstant) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            if (self.getValue() != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Value of decimal constant is not defined: " + self);
        };
    }

    // =========================================================================
    // BooleanConstant
    // =========================================================================

    /**
     * Guard: Check if element is BooleanConstant.
     */
    public boolean isBooleanConstant(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof BooleanConstant;
    }

    /**
     * Validates BooleanConstant value is defined.
     */
    @Constraint(
            name = "BooleanConstantResolved",
            message = "Value of boolean constant is not defined"
    )
    @Guard(method = "isBooleanConstant")
    public ValidationRule booleanConstantResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            BooleanConstant self = (BooleanConstant) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            // Boolean value is always defined (true/false)
            exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
            return ValidationResult.pass();
        };
    }

    // =========================================================================
    // StringConstant
    // =========================================================================

    /**
     * Guard: Check if element is StringConstant.
     */
    public boolean isStringConstant(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof StringConstant;
    }

    /**
     * Validates StringConstant value is defined.
     */
    @Constraint(
            name = "StringConstantResolved",
            message = "Value of string constant is not defined"
    )
    @Guard(method = "isStringConstant")
    public ValidationRule stringConstantResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            StringConstant self = (StringConstant) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            // Empty string is valid
            if ("".equals(self.getValue()) || self.getValue() != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Value of string constant is not defined: " + self);
        };
    }

    // =========================================================================
    // Literal
    // =========================================================================

    /**
     * Guard: Check if element is Literal.
     */
    public boolean isLiteral(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof Literal;
    }

    /**
     * Validates Literal value is defined.
     */
    @Constraint(
            name = "LiteralResolved",
            message = "Value of literal constant is not defined"
    )
    @Guard(method = "isLiteral")
    public ValidationRule literalResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            Literal self = (Literal) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            if (self.getValue() != null && !self.getValue().isEmpty()) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Value of literal constant is not defined: " + self);
        };
    }

    // =========================================================================
    // CustomData
    // =========================================================================

    /**
     * Guard: Check if element is CustomData.
     */
    public boolean isCustomData(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof CustomData;
    }

    /**
     * Validates CustomData value is defined.
     */
    @Constraint(
            name = "CustomDataResolved",
            message = "Value of custom (primitive) constant is not defined"
    )
    @Guard(method = "isCustomData")
    public ValidationRule customDataResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            CustomData self = (CustomData) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            if (self.getValue() != null && !self.getValue().isEmpty()) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Value of custom (primitive) constant is not defined: " + self);
        };
    }

    // =========================================================================
    // Environment Variables
    // =========================================================================

    /**
     * Guard: Check if element is IntegerEnvironmentVariable.
     */
    public boolean isIntegerEnvironmentVariable(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof IntegerEnvironmentVariable;
    }

    /**
     * Guard: Check if element is DecimalEnvironmentVariable.
     */
    public boolean isDecimalEnvironmentVariable(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof DecimalEnvironmentVariable;
    }

    /**
     * Guard: Check if element is BooleanEnvironmentVariable.
     */
    public boolean isBooleanEnvironmentVariable(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof BooleanEnvironmentVariable;
    }

    /**
     * Guard: Check if element is StringEnvironmentVariable.
     */
    public boolean isStringEnvironmentVariable(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof StringEnvironmentVariable;
    }

    /**
     * Guard: Check if element is LiteralEnvironmentVariable.
     */
    public boolean isLiteralEnvironmentVariable(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof LiteralEnvironmentVariable;
    }

    /**
     * Guard: Check if element is CustomEnvironmentVariable.
     */
    public boolean isCustomEnvironmentVariable(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof CustomEnvironmentVariable;
    }

    /**
     * Validates IntegerEnvironmentVariable.
     */
    @Constraint(
            name = "IntegerEnvironmentVariableResolved",
            message = "Value of integer environment variable is not defined"
    )
    @Guard(method = "isIntegerEnvironmentVariable")
    public ValidationRule integerEnvironmentVariableResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            IntegerEnvironmentVariable self = (IntegerEnvironmentVariable) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            if (self.getVariableName() != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Value of integer environment variable is not defined: " + self);
        };
    }

    /**
     * Validates DecimalEnvironmentVariable.
     */
    @Constraint(
            name = "DecimalEnvironmentVariableResolved",
            message = "Value of decimal environment variable is not defined"
    )
    @Guard(method = "isDecimalEnvironmentVariable")
    public ValidationRule decimalEnvironmentVariableResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            DecimalEnvironmentVariable self = (DecimalEnvironmentVariable) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            if (self.getVariableName() != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Value of decimal environment variable is not defined: " + self);
        };
    }

    /**
     * Validates BooleanEnvironmentVariable.
     */
    @Constraint(
            name = "BooleanEnvironmentVariableResolved",
            message = "Value of boolean environment variable is not defined"
    )
    @Guard(method = "isBooleanEnvironmentVariable")
    public ValidationRule booleanEnvironmentVariableResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            BooleanEnvironmentVariable self = (BooleanEnvironmentVariable) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            if (self.getVariableName() != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Value of boolean environment variable is not defined: " + self);
        };
    }

    /**
     * Validates StringEnvironmentVariable.
     */
    @Constraint(
            name = "StringEnvironmentVariableResolved",
            message = "Value of string environment variable is not defined"
    )
    @Guard(method = "isStringEnvironmentVariable")
    public ValidationRule stringEnvironmentVariableResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            StringEnvironmentVariable self = (StringEnvironmentVariable) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            if (self.getVariableName() != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Value of string environment variable is not defined: " + self);
        };
    }

    /**
     * Validates LiteralEnvironmentVariable.
     */
    @Constraint(
            name = "LiteralEnvironmentVariableResolved",
            message = "Value of literal environment variable is not defined"
    )
    @Guard(method = "isLiteralEnvironmentVariable")
    public ValidationRule literalEnvironmentVariableResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            LiteralEnvironmentVariable self = (LiteralEnvironmentVariable) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            if (self.getVariableName() != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Value of literal environment variable is not defined: " + self);
        };
    }

    /**
     * Validates CustomEnvironmentVariable.
     */
    @Constraint(
            name = "CustomEnvironmentVariableResolved",
            message = "Value of custom (primitive) environment variable is not defined"
    )
    @Guard(method = "isCustomEnvironmentVariable")
    public ValidationRule customEnvironmentVariableResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            CustomEnvironmentVariable self = (CustomEnvironmentVariable) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            if (self.getVariableName() != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Value of custom (primitive) environment variable is not defined: " + self);
        };
    }

    // =========================================================================
    // Instance
    // =========================================================================

    /**
     * Guard: Check if element is Instance.
     */
    public boolean isInstance(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof Instance;
    }

    /**
     * Validates Instance is resolved.
     */
    @Constraint(
            name = "InstanceResolved",
            message = "Unsupported expression"
    )
    @Guard(method = "isInstance")
    public ValidationRule instanceResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
            return ValidationResult.pass();
        };
    }
}
