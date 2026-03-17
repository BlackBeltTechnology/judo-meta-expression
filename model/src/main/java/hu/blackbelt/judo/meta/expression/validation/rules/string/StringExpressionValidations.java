package hu.blackbelt.judo.meta.expression.validation.rules.string;

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

import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.string.*;
import hu.blackbelt.judo.meta.expression.validation.ExpressionValidationContext;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.Guard;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;
import org.eclipse.emf.ecore.EObject;

/**
 * Validation rules for string expression elements.
 *
 * <p>Corresponds to constraints in string.evl</p>
 */
@ValidationContext(StringExpression.class)
public class StringExpressionValidations {

    /**
     * Generic resolved check for string expressions.
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
    // StringAttribute
    // =========================================================================

    /**
     * Guard: Check if element is StringAttribute and resolved.
     */
    public boolean isStringAttributeAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof StringAttribute)) {
            return false;
        }
        // Check resolution inline rather than depending on resolved() having run first,
        // since Zeta validation does not guarantee rule execution order
        StringAttribute self = (StringAttribute) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        ModelAdapter modelAdapter = exprCtx.getModelAdapter();
        Object attributeTypeObj = self.getAttributeType(modelAdapter);
        if (attributeTypeObj instanceof java.util.Optional) {
            return ((java.util.Optional<?>) attributeTypeObj).isPresent();
        }
        return attributeTypeObj != null;
    }

    /**
     * Validates that StringAttribute's attribute type is string.
     */
    @Constraint(
            name = ValidationConstants.ATTRIBUTE_TYPE_IS_STRING,
            message = "Attribute type is not string"
    )
    @Guard(method = "isStringAttributeAndResolved")
    public ValidationRule attributeTypeIsString() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            StringAttribute self = (StringAttribute) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            // getAttributeType returns Optional<P> wrapped in Object
            Object attributeTypeObj = self.getAttributeType(modelAdapter);
            if (attributeTypeObj instanceof java.util.Optional) {
                java.util.Optional<?> optionalType = (java.util.Optional<?>) attributeTypeObj;
                if (optionalType.isPresent() && modelAdapter.isString(optionalType.get())) {
                    return ValidationResult.pass();
                }
            } else if (attributeTypeObj != null && modelAdapter.isString(attributeTypeObj)) {
                return ValidationResult.pass();
            }

            String objectTypeName = self.getObjectExpression() != null 
                    ? String.valueOf(self.getObjectExpression().getObjectType(modelAdapter))
                    : "unknown";
            return ValidationResult.fail(
                    "Attribute type of " + self.getAttributeName() + 
                    " of object type " + objectTypeName + " is not string"
            );
        };
    }

    // =========================================================================
    // StringSwitchExpression
    // =========================================================================

    /**
     * Guard: Check if element is StringSwitchExpression with default.
     * Note: Unlike EVL, we don't require the default expression to be "Resolved" first,
     * since Zeta validation runs constraints in a single pass. We simply check the type.
     */
    public boolean isStringSwitchWithDefault(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof StringSwitchExpression)) {
            return false;
        }
        StringSwitchExpression self = (StringSwitchExpression) element;
        return self.getDefaultExpression() != null;
    }

    /**
     * Validates that StringSwitchExpression default case is string.
     */
    @Constraint(
            name = ValidationConstants.TYPE_OF_DEFAULT_CASE_IS_STRING,
            message = "Type of default case expression is not string"
    )
    @Guard(method = "isStringSwitchWithDefault")
    public ValidationRule typeOfDefaultCaseIsString() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            StringSwitchExpression self = (StringSwitchExpression) element;
            if (self.getDefaultExpression() instanceof StringExpression) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail(
                    "Type of default case expression is not string: " + self.getDefaultExpression()
            );
        };
    }

    // =========================================================================
    // StringAggregatedExpression
    // =========================================================================

    /**
     * Guard: Check if element is StringAggregatedExpression.
     */
    public boolean isStringAggregatedExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof StringAggregatedExpression;
    }

    /**
     * Validates StringAggregatedExpression is resolved.
     */
    @Constraint(
            name = "StringAggregatedExpressionResolved",
            message = "Attribute not found"
    )
    @Guard(method = "isStringAggregatedExpression")
    public ValidationRule stringAggregatedExpressionResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            StringAggregatedExpression self = (StringAggregatedExpression) element;
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
}
