package hu.blackbelt.judo.meta.expression.validation.rules.collection;

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

import hu.blackbelt.judo.meta.expression.CollectionExpression;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.collection.*;
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
 * Validation rules for collection expression elements.
 *
 * <p>Corresponds to constraints in collection.evl</p>
 */
@ValidationContext(CollectionExpression.class)
public class CollectionExpressionValidations {

    /**
     * Generic resolved check for collection expressions.
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
    // ImmutableCollection
    // =========================================================================

    /**
     * Guard: Check if element is ImmutableCollection.
     */
    public boolean isImmutableCollection(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof ImmutableCollection;
    }

    /**
     * Validates ImmutableCollection is resolved.
     */
    @Constraint(
            name = "ImmutableCollectionResolved",
            message = "Type of collection expression is not defined"
    )
    @Guard(method = "isImmutableCollection")
    public ValidationRule immutableCollectionResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ImmutableCollection self = (ImmutableCollection) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (self.getElementName() == null || 
                !exprCtx.satisfies(self.getElementName(), ValidationConstants.OBJECT_TYPE_IS_VALID)) {
                return ValidationResult.pass(); // Skip if element name not valid
            }

            if (self.getObjectType(modelAdapter) != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Type of collection expression is not defined: " + self
            );
        };
    }

    // =========================================================================
    // CollectionNavigationFromObjectExpression
    // =========================================================================

    /**
     * Guard: Check if element is CollectionNavigationFromObjectExpression.
     */
    public boolean isCollectionNavigationFromObject(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof CollectionNavigationFromObjectExpression;
    }

    /**
     * Validates CollectionNavigationFromObjectExpression is resolved.
     */
    @Constraint(
            name = "CollectionNavigationFromObjectResolved",
            message = "Reference not found"
    )
    @Guard(method = "isCollectionNavigationFromObject")
    public ValidationRule collectionNavigationFromObjectResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            CollectionNavigationFromObjectExpression self = (CollectionNavigationFromObjectExpression) element;
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
     * Guard: Check if element is CollectionNavigationFromObjectExpression and resolved.
     */
    public boolean isCollectionNavigationFromObjectAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof CollectionNavigationFromObjectExpression)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED);
    }

    /**
     * Validates that CollectionNavigationFromObjectExpression target is a collection.
     */
    @Constraint(
            name = "CollectionNavigationFromObjectTargetIsCollection",
            message = "Reference target is not collection"
    )
    @Guard(method = "isCollectionNavigationFromObjectAndResolved")
    public ValidationRule collectionNavigationFromObjectTargetIsCollection() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            CollectionNavigationFromObjectExpression self = (CollectionNavigationFromObjectExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (modelAdapter.isCollection(self)) {
                return ValidationResult.pass();
            }

            String objectTypeName = self.getObjectExpression().getObjectType(modelAdapter) != null
                    ? String.valueOf(self.getObjectExpression().getObjectType(modelAdapter))
                    : "unknown";
            return ValidationResult.fail(
                    "Reference named " + self.getReferenceName() + 
                    " of object type " + objectTypeName + " is not collection"
            );
        };
    }

    // =========================================================================
    // CollectionNavigationFromCollectionExpression
    // =========================================================================

    /**
     * Guard: Check if element is CollectionNavigationFromCollectionExpression.
     */
    public boolean isCollectionNavigationFromCollection(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof CollectionNavigationFromCollectionExpression;
    }

    /**
     * Validates CollectionNavigationFromCollectionExpression is resolved.
     */
    @Constraint(
            name = "CollectionNavigationFromCollectionResolved",
            message = "Reference not found"
    )
    @Guard(method = "isCollectionNavigationFromCollection")
    public ValidationRule collectionNavigationFromCollectionResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            CollectionNavigationFromCollectionExpression self = (CollectionNavigationFromCollectionExpression) element;
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

            String objectTypeName = self.getCollectionExpression().getObjectType(modelAdapter) != null
                    ? String.valueOf(self.getCollectionExpression().getObjectType(modelAdapter))
                    : "unknown";
            return ValidationResult.fail(
                    "Reference named " + self.getReferenceName() + 
                    " of object type " + objectTypeName + " not found"
            );
        };
    }

    /**
     * Guard: Check if element is CollectionNavigationFromCollectionExpression and resolved.
     */
    public boolean isCollectionNavigationFromCollectionAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof CollectionNavigationFromCollectionExpression)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED);
    }

    /**
     * Validates that CollectionNavigationFromCollectionExpression target is a collection.
     */
    @Constraint(
            name = "CollectionNavigationFromCollectionTargetIsCollection",
            message = "Reference target is not collection"
    )
    @Guard(method = "isCollectionNavigationFromCollectionAndResolved")
    public ValidationRule collectionNavigationFromCollectionTargetIsCollection() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            CollectionNavigationFromCollectionExpression self = (CollectionNavigationFromCollectionExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (modelAdapter.isCollection(self)) {
                return ValidationResult.pass();
            }

            String objectTypeName = self.getCollectionExpression().getObjectType(modelAdapter) != null
                    ? String.valueOf(self.getCollectionExpression().getObjectType(modelAdapter))
                    : "unknown";
            return ValidationResult.fail(
                    "Reference named " + self.getReferenceName() + 
                    " of object type " + objectTypeName + " is not collection"
            );
        };
    }

    // =========================================================================
    // ObjectNavigationFromCollectionExpression
    // =========================================================================

    /**
     * Guard: Check if element is ObjectNavigationFromCollectionExpression.
     */
    public boolean isObjectNavigationFromCollection(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof ObjectNavigationFromCollectionExpression;
    }

    /**
     * Validates ObjectNavigationFromCollectionExpression is resolved.
     */
    @Constraint(
            name = "ObjectNavigationFromCollectionResolved",
            message = "Reference not found"
    )
    @Guard(method = "isObjectNavigationFromCollection")
    public ValidationRule objectNavigationFromCollectionResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ObjectNavigationFromCollectionExpression self = (ObjectNavigationFromCollectionExpression) element;
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

            String objectTypeName = self.getCollectionExpression().getObjectType(modelAdapter) != null
                    ? String.valueOf(self.getCollectionExpression().getObjectType(modelAdapter))
                    : "unknown";
            return ValidationResult.fail(
                    "Reference named " + self.getReferenceName() + 
                    " of object type " + objectTypeName + " not found"
            );
        };
    }

    /**
     * Guard: Check if element is ObjectNavigationFromCollectionExpression and resolved.
     */
    public boolean isObjectNavigationFromCollectionAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof ObjectNavigationFromCollectionExpression)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED);
    }

    /**
     * Validates that ObjectNavigationFromCollectionExpression target is not a collection.
     */
    @Constraint(
            name = "ObjectNavigationFromCollectionTargetIsNotCollection",
            message = "Reference target is collection"
    )
    @Guard(method = "isObjectNavigationFromCollectionAndResolved")
    public ValidationRule objectNavigationFromCollectionTargetIsNotCollection() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ObjectNavigationFromCollectionExpression self = (ObjectNavigationFromCollectionExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (!modelAdapter.isCollection(self)) {
                return ValidationResult.pass();
            }

            String objectTypeName = self.getCollectionExpression().getObjectType(modelAdapter) != null
                    ? String.valueOf(self.getCollectionExpression().getObjectType(modelAdapter))
                    : "unknown";
            return ValidationResult.fail(
                    "Reference named " + self.getReferenceName() + 
                    " of object type " + objectTypeName + " is collection"
            );
        };
    }

    // =========================================================================
    // CollectionFilterExpression
    // =========================================================================

    /**
     * Guard: Check if element is CollectionFilterExpression.
     */
    public boolean isCollectionFilterExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof CollectionFilterExpression;
    }

    /**
     * Validates CollectionFilterExpression is resolved.
     */
    @Constraint(
            name = "CollectionFilterResolved",
            message = "Type of collection expression is not defined"
    )
    @Guard(method = "isCollectionFilterExpression")
    public ValidationRule collectionFilterResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            CollectionFilterExpression self = (CollectionFilterExpression) element;
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
                    "Type of collection expression is not defined: " + self
            );
        };
    }

    // =========================================================================
    // SortExpression
    // =========================================================================

    /**
     * Guard: Check if element is SortExpression.
     */
    public boolean isSortExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof SortExpression;
    }

    /**
     * Validates SortExpression is resolved.
     */
    @Constraint(
            name = "SortExpressionResolved",
            message = "Type of sort collection expression is not defined"
    )
    @Guard(method = "isSortExpression")
    public ValidationRule sortExpressionResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            SortExpression self = (SortExpression) element;
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
                    "Type of sort collection expression is not defined: " + self
            );
        };
    }

    // =========================================================================
    // CastCollection
    // =========================================================================

    /**
     * Guard: Check if element is CastCollection.
     */
    public boolean isCastCollection(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof CastCollection;
    }

    /**
     * Validates CastCollection is resolved.
     */
    @Constraint(
            name = "CastCollectionResolved",
            message = "Type of cast collection expression is not defined"
    )
    @Guard(method = "isCastCollection")
    public ValidationRule castCollectionResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            CastCollection self = (CastCollection) element;
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
                    "Type of cast collection expression is not defined: " + self
            );
        };
    }

    /**
     * Guard: Check if element is CastCollection and resolved.
     */
    public boolean isCastCollectionAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof CastCollection)) {
            return false;
        }
        CastCollection self = (CastCollection) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED)
                && self.getCollectionExpression() != null
                && exprCtx.satisfies(self.getCollectionExpression(), ValidationConstants.RESOLVED);
    }

    /**
     * Validates CastCollection cast type is compatible.
     */
    @Constraint(
            name = ValidationConstants.CAST_TYPE_IS_COMPATIBLE,
            message = "Invalid casting type"
    )
    @Guard(method = "isCastCollectionAndResolved")
    public ValidationRule castTypeIsCompatible() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            CastCollection self = (CastCollection) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            Object castType = self.getObjectType(modelAdapter);
            Object collectionType = self.getCollectionExpression().getObjectType(modelAdapter);

            if (castType == null || collectionType == null) {
                return ValidationResult.pass();
            }

            Collection<?> superTypes = modelAdapter.getSuperTypes(castType);
            if (superTypes != null && superTypes.contains(collectionType)) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Invalid casting: " + collectionType + " as " + castType + 
                    ". " + collectionType + " is not supertype of " + castType
            );
        };
    }

    // =========================================================================
    // CollectionVariableReference
    // =========================================================================

    /**
     * Guard: Check if element is CollectionVariableReference.
     */
    public boolean isCollectionVariableReference(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof CollectionVariableReference;
    }

    /**
     * Validates CollectionVariableReference is resolved.
     */
    @Constraint(
            name = "CollectionVariableReferenceResolved",
            message = "Variable is unknown"
    )
    @Guard(method = "isCollectionVariableReference")
    public ValidationRule collectionVariableReferenceResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            CollectionVariableReference self = (CollectionVariableReference) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            if (self.getVariable() != null && self.getVariable().eClass() != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Variable is unknown: " + self);
        };
    }

    /**
     * Guard: Check if element is CollectionVariableReference and resolved.
     */
    public boolean isCollectionVariableReferenceAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof CollectionVariableReference)) {
            return false;
        }
        CollectionVariableReference self = (CollectionVariableReference) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED)
                && self.getVariable() != null
                && exprCtx.satisfies(self.getVariable(), ValidationConstants.RESOLVED);
    }

    /**
     * Validates CollectionVariableReference type is defined.
     */
    @Constraint(
            name = "CollectionVariableReferenceTypeIsDefined",
            message = "Type of variable is unknown"
    )
    @Guard(method = "isCollectionVariableReferenceAndResolved")
    public ValidationRule collectionVariableReferenceTypeIsDefined() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            CollectionVariableReference self = (CollectionVariableReference) element;
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
}
