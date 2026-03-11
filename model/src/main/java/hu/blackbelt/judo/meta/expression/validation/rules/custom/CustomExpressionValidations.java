package hu.blackbelt.judo.meta.expression.validation.rules.custom;

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

import hu.blackbelt.judo.meta.expression.CustomExpression;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.custom.*;
import hu.blackbelt.judo.meta.expression.validation.ExpressionValidationContext;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.Guard;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;
import org.eclipse.emf.ecore.EObject;

/**
 * Validation rules for custom expression elements.
 *
 * <p>Corresponds to constraints in custom.evl</p>
 */
@ValidationContext(CustomExpression.class)
public class CustomExpressionValidations {

    /**
     * Generic resolved check for custom expressions.
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
    // CustomAttribute
    // =========================================================================

    /**
     * Guard: Check if element is CustomAttribute and resolved.
     */
    public boolean isCustomAttributeAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof CustomAttribute)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED);
    }

    /**
     * Validates that CustomAttribute's attribute type is custom.
     */
    @Constraint(
            name = ValidationConstants.ATTRIBUTE_TYPE_IS_CUSTOM,
            message = "Attribute type is not custom"
    )
    @Guard(method = "isCustomAttributeAndResolved")
    public ValidationRule attributeTypeIsCustom() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            CustomAttribute self = (CustomAttribute) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            // getAttributeType returns Optional<P> wrapped in Object
            Object attributeTypeObj = self.getAttributeType(modelAdapter);
            if (attributeTypeObj instanceof java.util.Optional) {
                java.util.Optional<?> optionalType = (java.util.Optional<?>) attributeTypeObj;
                if (optionalType.isPresent() && modelAdapter.isCustom(optionalType.get())) {
                    return ValidationResult.pass();
                }
            }

            String objectTypeName = self.getObjectExpression() != null
                    ? String.valueOf(self.getObjectExpression().getObjectType(modelAdapter))
                    : "unknown";
            return ValidationResult.fail(
                    "Attribute type of " + self.getAttributeName() +
                    " of object type " + objectTypeName + " is not custom (expression type)"
            );
        };
    }

    // =========================================================================
    // CustomSwitchExpression
    // =========================================================================

    /**
     * Guard: Check if element is CustomSwitchExpression with default.
     * Note: Unlike EVL, we don't require the default expression to be "Resolved" first,
     * since Zeta validation runs constraints in a single pass. We simply check the type.
     */
    public boolean isCustomSwitchWithDefault(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof CustomSwitchExpression)) {
            return false;
        }
        CustomSwitchExpression self = (CustomSwitchExpression) element;
        return self.getDefaultExpression() != null;
    }

    /**
     * Validates that CustomSwitchExpression default case is custom.
     */
    @Constraint(
            name = ValidationConstants.TYPE_OF_DEFAULT_CASE_IS_CUSTOM,
            message = "Type of default case expression is not custom"
    )
    @Guard(method = "isCustomSwitchWithDefault")
    public ValidationRule typeOfDefaultCaseIsCustom() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            CustomSwitchExpression self = (CustomSwitchExpression) element;
            if (self.getDefaultExpression() instanceof CustomExpression) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail(
                    "Type of default case expression is not custom (expression type): " + self.getDefaultExpression()
            );
        };
    }
}
