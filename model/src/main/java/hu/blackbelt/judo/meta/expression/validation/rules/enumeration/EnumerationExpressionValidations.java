package hu.blackbelt.judo.meta.expression.validation.rules.enumeration;

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

import hu.blackbelt.judo.meta.expression.EnumerationExpression;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.enumeration.*;
import hu.blackbelt.judo.meta.expression.validation.ExpressionValidationContext;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.Guard;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;
import org.eclipse.emf.ecore.EObject;

/**
 * Validation rules for enumeration expression elements.
 *
 * <p>Corresponds to constraints in enumeration.evl</p>
 */
@ValidationContext(EnumerationExpression.class)
public class EnumerationExpressionValidations {

    /**
     * Generic resolved check for enumeration expressions.
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
    // EnumerationAttribute
    // =========================================================================

    /**
     * Guard: Check if element is EnumerationAttribute and resolved.
     */
    public boolean isEnumerationAttributeAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof EnumerationAttribute)) {
            return false;
        }
        // Check resolution inline rather than depending on resolved() having run first,
        // since Zeta validation does not guarantee rule execution order
        EnumerationAttribute self = (EnumerationAttribute) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        ModelAdapter modelAdapter = exprCtx.getModelAdapter();
        Object attributeTypeObj = self.getAttributeType(modelAdapter);
        if (attributeTypeObj instanceof java.util.Optional) {
            return ((java.util.Optional<?>) attributeTypeObj).isPresent();
        }
        return attributeTypeObj != null;
    }

    /**
     * Validates that EnumerationAttribute's attribute type is enumeration.
     */
    @Constraint(
            name = ValidationConstants.ATTRIBUTE_TYPE_IS_ENUMERATION,
            message = "Attribute type is not enumeration"
    )
    @Guard(method = "isEnumerationAttributeAndResolved")
    public ValidationRule attributeTypeIsEnumeration() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            EnumerationAttribute self = (EnumerationAttribute) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            // getAttributeType returns Optional<P> wrapped in Object
            Object attributeTypeObj = self.getAttributeType(modelAdapter);
            if (attributeTypeObj instanceof java.util.Optional) {
                java.util.Optional<?> optionalType = (java.util.Optional<?>) attributeTypeObj;
                if (optionalType.isPresent() && modelAdapter.isEnumeration(optionalType.get())) {
                    return ValidationResult.pass();
                }
            } else if (attributeTypeObj != null && modelAdapter.isEnumeration(attributeTypeObj)) {
                return ValidationResult.pass();
            }

            String objectTypeName = self.getObjectExpression() != null 
                    ? String.valueOf(self.getObjectExpression().getObjectType(modelAdapter))
                    : "unknown";
            return ValidationResult.fail(
                    "Attribute type of " + self.getAttributeName() + 
                    " of object type " + objectTypeName + " is not enumeration"
            );
        };
    }

    // =========================================================================
    // EnumerationSwitchExpression
    // =========================================================================

    /**
     * Guard: Check if element is EnumerationSwitchExpression with default.
     * Note: Unlike EVL, we don't require the default expression to be "Resolved" first,
     * since Zeta validation runs constraints in a single pass. We simply check the type.
     */
    public boolean isEnumerationSwitchWithDefault(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof EnumerationSwitchExpression)) {
            return false;
        }
        EnumerationSwitchExpression self = (EnumerationSwitchExpression) element;
        return self.getDefaultExpression() != null;
    }

    /**
     * Validates that EnumerationSwitchExpression default case is enumeration.
     */
    @Constraint(
            name = ValidationConstants.TYPE_OF_DEFAULT_CASE_IS_ENUMERATION,
            message = "Type of default case expression is not enumeration"
    )
    @Guard(method = "isEnumerationSwitchWithDefault")
    public ValidationRule typeOfDefaultCaseIsEnumeration() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            EnumerationSwitchExpression self = (EnumerationSwitchExpression) element;
            if (self.getDefaultExpression() instanceof EnumerationExpression) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail(
                    "Type of default case expression is not enumeration: " + self.getDefaultExpression()
            );
        };
    }
}
