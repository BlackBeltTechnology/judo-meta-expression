package hu.blackbelt.judo.meta.expression.validation.rules.binding;

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
import hu.blackbelt.judo.meta.expression.binding.AttributeBinding;
import hu.blackbelt.judo.meta.expression.validation.ExpressionValidationContext;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.Guard;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;
import org.eclipse.emf.ecore.EObject;

import java.util.Optional;

/**
 * Validation rules for attribute binding elements.
 *
 * <p>Corresponds to constraints in attributeBinding.evl</p>
 */
@ValidationContext(AttributeBinding.class)
public class AttributeBindingValidations {

    // =========================================================================
    // NumericExpressionMatchesBinding
    // =========================================================================

    /**
     * Guard: Check if expression is NumericExpression.
     */
    public boolean hasNumericExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof AttributeBinding)) {
            return false;
        }
        AttributeBinding self = (AttributeBinding) element;
        return self.getExpression() instanceof NumericExpression;
    }

    /**
     * Validates that numeric expression matches binding attribute type.
     */
    @Constraint(
            name = ValidationConstants.NUMERIC_EXPRESSION_MATCHES_BINDING,
            message = "Attribute must be numeric type"
    )
    @Guard(method = "hasNumericExpression")
    public ValidationRule numericExpressionMatchesBinding() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            AttributeBinding self = (AttributeBinding) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            Object type = modelAdapter.get(self.getTypeName());
            if (type == null) {
                return ValidationResult.pass(); // Skip if type not resolved
            }

            Optional<?> attributeType = modelAdapter.getAttributeType(type, self.getAttributeName());
            if (attributeType.isPresent() && modelAdapter.isNumeric(attributeType.get())) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Attribute named " + self.getAttributeName() + 
                    " must be numeric type, because the assigned expression evaluates to a number."
            );
        };
    }

    // =========================================================================
    // BooleanExpressionMatchesBinding
    // =========================================================================

    /**
     * Guard: Check if expression is LogicalExpression.
     */
    public boolean hasLogicalExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof AttributeBinding)) {
            return false;
        }
        AttributeBinding self = (AttributeBinding) element;
        return self.getExpression() instanceof LogicalExpression;
    }

    /**
     * Validates that boolean expression matches binding attribute type.
     */
    @Constraint(
            name = ValidationConstants.BOOLEAN_EXPRESSION_MATCHES_BINDING,
            message = "Attribute must be boolean type"
    )
    @Guard(method = "hasLogicalExpression")
    public ValidationRule booleanExpressionMatchesBinding() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            AttributeBinding self = (AttributeBinding) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            Object type = modelAdapter.get(self.getTypeName());
            if (type == null) {
                return ValidationResult.pass(); // Skip if type not resolved
            }

            Optional<?> attributeType = modelAdapter.getAttributeType(type, self.getAttributeName());
            if (attributeType.isPresent() && modelAdapter.isBoolean(attributeType.get())) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Attribute named " + self.getAttributeName() + 
                    " must be boolean type, because the assigned expression evaluates to a boolean."
            );
        };
    }

    // =========================================================================
    // StringExpressionMatchesBinding
    // =========================================================================

    /**
     * Guard: Check if expression is StringExpression.
     */
    public boolean hasStringExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof AttributeBinding)) {
            return false;
        }
        AttributeBinding self = (AttributeBinding) element;
        return self.getExpression() instanceof StringExpression;
    }

    /**
     * Validates that string expression matches binding attribute type.
     */
    @Constraint(
            name = ValidationConstants.STRING_EXPRESSION_MATCHES_BINDING,
            message = "Attribute must be string type"
    )
    @Guard(method = "hasStringExpression")
    public ValidationRule stringExpressionMatchesBinding() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            AttributeBinding self = (AttributeBinding) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            Object type = modelAdapter.get(self.getTypeName());
            if (type == null) {
                return ValidationResult.pass(); // Skip if type not resolved
            }

            Optional<?> attributeType = modelAdapter.getAttributeType(type, self.getAttributeName());
            if (attributeType.isPresent() && modelAdapter.isString(attributeType.get())) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Attribute named " + self.getAttributeName() + 
                    " must be string type, because the assigned expression evaluates to a string."
            );
        };
    }

    // =========================================================================
    // EnumerationExpressionMatchesBinding
    // =========================================================================

    /**
     * Guard: Check if expression is EnumerationExpression.
     */
    public boolean hasEnumerationExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof AttributeBinding)) {
            return false;
        }
        AttributeBinding self = (AttributeBinding) element;
        return self.getExpression() instanceof EnumerationExpression;
    }

    /**
     * Validates that enumeration expression matches binding attribute type.
     */
    @Constraint(
            name = ValidationConstants.ENUMERATION_EXPRESSION_MATCHES_BINDING,
            message = "Attribute must be enumeration type"
    )
    @Guard(method = "hasEnumerationExpression")
    public ValidationRule enumerationExpressionMatchesBinding() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            AttributeBinding self = (AttributeBinding) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            Object type = modelAdapter.get(self.getTypeName());
            if (type == null) {
                return ValidationResult.pass(); // Skip if type not resolved
            }

            Optional<?> attributeType = modelAdapter.getAttributeType(type, self.getAttributeName());
            if (attributeType.isPresent() && modelAdapter.isEnumeration(attributeType.get())) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Attribute named " + self.getAttributeName() + 
                    " must be enumeration type, because the assigned expression evaluates to an enumeration."
            );
        };
    }

    // =========================================================================
    // DateExpressionMatchesBinding
    // =========================================================================

    /**
     * Guard: Check if expression is DateExpression.
     */
    public boolean hasDateExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof AttributeBinding)) {
            return false;
        }
        AttributeBinding self = (AttributeBinding) element;
        return self.getExpression() instanceof DateExpression;
    }

    /**
     * Validates that date expression matches binding attribute type.
     */
    @Constraint(
            name = ValidationConstants.DATE_EXPRESSION_MATCHES_BINDING,
            message = "Attribute must be date type"
    )
    @Guard(method = "hasDateExpression")
    public ValidationRule dateExpressionMatchesBinding() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            AttributeBinding self = (AttributeBinding) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            Object type = modelAdapter.get(self.getTypeName());
            if (type == null) {
                return ValidationResult.pass(); // Skip if type not resolved
            }

            Optional<?> attributeType = modelAdapter.getAttributeType(type, self.getAttributeName());
            if (attributeType.isPresent() && modelAdapter.isDate(attributeType.get())) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Attribute named " + self.getAttributeName() + 
                    " must be date type, because the assigned expression evaluates to a date."
            );
        };
    }

    // =========================================================================
    // TimestampExpressionMatchesBinding
    // =========================================================================

    /**
     * Guard: Check if expression is TimestampExpression.
     */
    public boolean hasTimestampExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof AttributeBinding)) {
            return false;
        }
        AttributeBinding self = (AttributeBinding) element;
        return self.getExpression() instanceof TimestampExpression;
    }

    /**
     * Validates that timestamp expression matches binding attribute type.
     */
    @Constraint(
            name = ValidationConstants.TIMESTAMP_EXPRESSION_MATCHES_BINDING,
            message = "Attribute must be timestamp type"
    )
    @Guard(method = "hasTimestampExpression")
    public ValidationRule timestampExpressionMatchesBinding() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            AttributeBinding self = (AttributeBinding) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            Object type = modelAdapter.get(self.getTypeName());
            if (type == null) {
                return ValidationResult.pass(); // Skip if type not resolved
            }

            Optional<?> attributeType = modelAdapter.getAttributeType(type, self.getAttributeName());
            if (attributeType.isPresent() && modelAdapter.isTimestamp(attributeType.get())) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Attribute named " + self.getAttributeName() + 
                    " must be timestamp type, because the assigned expression evaluates to a timestamp."
            );
        };
    }

    // =========================================================================
    // TimeExpressionMatchesBinding
    // =========================================================================

    /**
     * Guard: Check if expression is TimeExpression.
     */
    public boolean hasTimeExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof AttributeBinding)) {
            return false;
        }
        AttributeBinding self = (AttributeBinding) element;
        return self.getExpression() instanceof TimeExpression;
    }

    /**
     * Validates that time expression matches binding attribute type.
     */
    @Constraint(
            name = ValidationConstants.TIME_EXPRESSION_MATCHES_BINDING,
            message = "Attribute must be time type"
    )
    @Guard(method = "hasTimeExpression")
    public ValidationRule timeExpressionMatchesBinding() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            AttributeBinding self = (AttributeBinding) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            Object type = modelAdapter.get(self.getTypeName());
            if (type == null) {
                return ValidationResult.pass(); // Skip if type not resolved
            }

            Optional<?> attributeType = modelAdapter.getAttributeType(type, self.getAttributeName());
            if (attributeType.isPresent() && modelAdapter.isTime(attributeType.get())) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Attribute named " + self.getAttributeName() + 
                    " must be time type, because the assigned expression evaluates to a time."
            );
        };
    }

    // =========================================================================
    // CustomExpressionMatchesBinding
    // =========================================================================

    /**
     * Guard: Check if expression is CustomExpression.
     */
    public boolean hasCustomExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof AttributeBinding)) {
            return false;
        }
        AttributeBinding self = (AttributeBinding) element;
        return self.getExpression() instanceof CustomExpression;
    }

    /**
     * Validates that custom expression matches binding attribute type.
     */
    @Constraint(
            name = ValidationConstants.CUSTOM_EXPRESSION_MATCHES_BINDING,
            message = "Attribute must be custom type"
    )
    @Guard(method = "hasCustomExpression")
    public ValidationRule customExpressionMatchesBinding() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            AttributeBinding self = (AttributeBinding) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            Object type = modelAdapter.get(self.getTypeName());
            if (type == null) {
                return ValidationResult.pass(); // Skip if type not resolved
            }

            Optional<?> attributeType = modelAdapter.getAttributeType(type, self.getAttributeName());
            if (attributeType.isPresent() && modelAdapter.isCustom(attributeType.get())) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Attribute named " + self.getAttributeName() + 
                    " must be custom type, because the assigned expression evaluates to a custom type."
            );
        };
    }
}
