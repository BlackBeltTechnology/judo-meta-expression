package hu.blackbelt.judo.meta.expression.validation.rules.object;

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

import hu.blackbelt.judo.meta.expression.ObjectExpression;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.object.*;
import hu.blackbelt.judo.meta.expression.operator.ObjectSelector;
import hu.blackbelt.judo.meta.expression.validation.ExpressionValidationContext;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.Guard;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;
import org.eclipse.emf.ecore.EObject;

import java.util.Collection;

/**
 * Validation rules for object expression elements.
 *
 * <p>Corresponds to constraints in object.evl</p>
 */
@ValidationContext(ObjectExpression.class)
public class ObjectExpressionValidations {

    /**
     * Generic resolved check for object expressions.
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
    // ObjectNavigationExpression
    // =========================================================================

    /**
     * Guard: Check if element is ObjectNavigationExpression.
     */
    public boolean isObjectNavigationExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof ObjectNavigationExpression;
    }

    /**
     * Validates ObjectNavigationExpression is resolved.
     */
    @Constraint(
            name = "ObjectNavigationResolved",
            message = "Reference not found"
    )
    @Guard(method = "isObjectNavigationExpression")
    public ValidationRule objectNavigationResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ObjectNavigationExpression self = (ObjectNavigationExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (self.getObjectExpression() == null || 
                !exprCtx.satisfies(self.getObjectExpression(), ValidationConstants.RESOLVED)) {
                return ValidationResult.pass(); // Skip if source not resolved
            }

            if (self.getObjectType(modelAdapter) != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            String objectTypeName = self.getObjectExpression().getObjectType(modelAdapter) != null
                    ? String.valueOf(self.getObjectExpression().getObjectType(modelAdapter))
                    : "unknown";
            return ValidationResult.fail(
                    "Reference named " + self.getReferenceName() + 
                    " of object type " + objectTypeName + " not found"
            );
        };
    }

    /**
     * Guard: Check if element is ObjectNavigationExpression and resolved.
     */
    public boolean isObjectNavigationExpressionAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof ObjectNavigationExpression)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED);
    }

    /**
     * Validates that ObjectNavigationExpression target is not a collection.
     */
    @Constraint(
            name = ValidationConstants.TARGET_IS_COLLECTION,
            message = "Reference target is collection"
    )
    @Guard(method = "isObjectNavigationExpressionAndResolved")
    public ValidationRule objectNavigationTargetIsNotCollection() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ObjectNavigationExpression self = (ObjectNavigationExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (!modelAdapter.isCollection(self)) {
                return ValidationResult.pass();
            }

            String objectTypeName = self.getObjectExpression().getObjectType(modelAdapter) != null
                    ? String.valueOf(self.getObjectExpression().getObjectType(modelAdapter))
                    : "unknown";
            return ValidationResult.fail(
                    "Reference named " + self.getReferenceName() + 
                    " of object type " + objectTypeName + " is collection"
            );
        };
    }

    // =========================================================================
    // ObjectSelectorExpression
    // =========================================================================

    /**
     * Guard: Check if element is ObjectSelectorExpression.
     */
    public boolean isObjectSelectorExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof ObjectSelectorExpression;
    }

    /**
     * Validates ObjectSelectorExpression is resolved.
     */
    @Constraint(
            name = "ObjectSelectorResolved",
            message = "Type of object selector expression is not defined"
    )
    @Guard(method = "isObjectSelectorExpression")
    public ValidationRule objectSelectorResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ObjectSelectorExpression self = (ObjectSelectorExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (self.getCollectionExpression() == null || 
                !exprCtx.satisfies(self.getCollectionExpression(), ValidationConstants.RESOLVED)) {
                return ValidationResult.pass(); // Skip if source not resolved
            }

            if (self.getObjectType(modelAdapter) != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Type of object selector expression is not defined: " + self
            );
        };
    }

    /**
     * Guard: Check if element is ObjectSelectorExpression and resolved.
     */
    public boolean isObjectSelectorExpressionAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof ObjectSelectorExpression)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED);
    }

    /**
     * Validates that ObjectSelectorExpression operator is valid (only 'any' is supported).
     */
    @Constraint(
            name = ValidationConstants.OBJECT_SELECTOR_OPERATOR_IS_VALID,
            message = "Only 'any' is supported"
    )
    @Guard(method = "isObjectSelectorExpressionAndResolved")
    public ValidationRule objectSelectorOperatorIsValid() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ObjectSelectorExpression self = (ObjectSelectorExpression) element;
            if (self.getOperator() == ObjectSelector.ANY) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail("Only 'any' is supported: " + self);
        };
    }

    // =========================================================================
    // ObjectFilterExpression
    // =========================================================================

    /**
     * Guard: Check if element is ObjectFilterExpression.
     */
    public boolean isObjectFilterExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof ObjectFilterExpression;
    }

    /**
     * Validates ObjectFilterExpression is resolved.
     */
    @Constraint(
            name = "ObjectFilterResolved",
            message = "Type of object filter expression is not defined"
    )
    @Guard(method = "isObjectFilterExpression")
    public ValidationRule objectFilterResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ObjectFilterExpression self = (ObjectFilterExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (self.getObjectExpression() == null || 
                !exprCtx.satisfies(self.getObjectExpression(), ValidationConstants.RESOLVED)) {
                return ValidationResult.pass(); // Skip if source not resolved
            }

            if (self.getObjectType(modelAdapter) != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Type of object filter expression is not defined: " + self
            );
        };
    }

    // =========================================================================
    // ObjectVariableReference
    // =========================================================================

    /**
     * Guard: Check if element is ObjectVariableReference.
     */
    public boolean isObjectVariableReference(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof ObjectVariableReference;
    }

    /**
     * Validates ObjectVariableReference is resolved.
     */
    @Constraint(
            name = "ObjectVariableReferenceResolved",
            message = "Object variable reference is unknown"
    )
    @Guard(method = "isObjectVariableReference")
    public ValidationRule objectVariableReferenceResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ObjectVariableReference self = (ObjectVariableReference) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            if (self.getVariable() != null && self.getVariable().eClass() != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Object variable reference is unknown: " + self);
        };
    }

    /**
     * Guard: Check if element is ObjectVariableReference and resolved.
     */
    public boolean isObjectVariableReferenceAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof ObjectVariableReference)) {
            return false;
        }
        ObjectVariableReference self = (ObjectVariableReference) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED)
                && self.getVariable() != null
                && exprCtx.satisfies(self.getVariable(), ValidationConstants.RESOLVED);
    }

    /**
     * Validates ObjectVariableReference type is defined.
     */
    @Constraint(
            name = ValidationConstants.TYPE_IS_DEFINED,
            message = "Type of variable is unknown"
    )
    @Guard(method = "isObjectVariableReferenceAndResolved")
    public ValidationRule objectVariableReferenceTypeIsDefined() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ObjectVariableReference self = (ObjectVariableReference) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (self.getObjectType(modelAdapter) != null) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Type of variable " + self.getVariable().getName() + " is unknown"
            );
        };
    }

    // =========================================================================
    // CastObject
    // =========================================================================

    /**
     * Guard: Check if element is CastObject.
     */
    public boolean isCastObject(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof CastObject;
    }

    /**
     * Validates CastObject is resolved.
     */
    @Constraint(
            name = "CastObjectResolved",
            message = "Type of cast object expression is not defined"
    )
    @Guard(method = "isCastObject")
    public ValidationRule castObjectResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            CastObject self = (CastObject) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (self.getObjectExpression() == null || 
                !exprCtx.satisfies(self.getObjectExpression(), ValidationConstants.RESOLVED)) {
                return ValidationResult.pass(); // Skip if source not resolved
            }

            if (self.getObjectType(modelAdapter) != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Type of cast object expression is not defined: " + self
            );
        };
    }

    /**
     * Guard: Check if element is CastObject and resolved.
     */
    public boolean isCastObjectAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof CastObject)) {
            return false;
        }
        CastObject self = (CastObject) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED)
                && self.getObjectExpression() != null
                && exprCtx.satisfies(self.getObjectExpression(), ValidationConstants.RESOLVED);
    }

    /**
     * Validates CastObject casted type is compatible.
     */
    @Constraint(
            name = ValidationConstants.CASTED_TYPE_IS_COMPATIBLE,
            message = "Invalid casting type"
    )
    @Guard(method = "isCastObjectAndResolved")
    public ValidationRule castedTypeIsCompatible() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            CastObject self = (CastObject) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            Object castType = self.getObjectType(modelAdapter);
            Object sourceType = self.getObjectExpression().getObjectType(modelAdapter);

            if (castType == null || sourceType == null) {
                return ValidationResult.pass();
            }

            Collection<?> superTypes = modelAdapter.getSuperTypes(castType);
            if (superTypes != null && superTypes.contains(sourceType)) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Invalid casting type: " + sourceType + " is not supertype of " + castType
            );
        };
    }

    // =========================================================================
    // ContainerExpression
    // =========================================================================

    /**
     * Guard: Check if element is ContainerExpression.
     */
    public boolean isContainerExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof ContainerExpression;
    }

    /**
     * Validates ContainerExpression is resolved.
     */
    @Constraint(
            name = "ContainerExpressionResolved",
            message = "Invalid container type"
    )
    @Guard(method = "isContainerExpression")
    public ValidationRule containerExpressionResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ContainerExpression self = (ContainerExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (self.getObjectExpression() == null || 
                !exprCtx.satisfies(self.getObjectExpression(), ValidationConstants.RESOLVED)) {
                return ValidationResult.pass(); // Skip if source not resolved
            }

            Object objectType = self.getObjectExpression().getObjectType(modelAdapter);
            if (objectType == null) {
                return ValidationResult.pass();
            }

            Collection<?> containerTypes = modelAdapter.getContainerTypesOf(objectType);
            Object elementType = self.getElementName() != null ? modelAdapter.get(self.getElementName()) : null;

            if (containerTypes != null && elementType != null) {
                for (Object containerType : containerTypes) {
                    if (containerType.equals(elementType)) {
                        exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                        return ValidationResult.pass();
                    }
                }
            }

            return ValidationResult.fail("Invalid container type of: " + self);
        };
    }
}
