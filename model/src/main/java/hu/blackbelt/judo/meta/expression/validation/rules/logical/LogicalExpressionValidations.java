package hu.blackbelt.judo.meta.expression.validation.rules.logical;

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
import hu.blackbelt.judo.meta.expression.logical.*;
import hu.blackbelt.judo.meta.expression.validation.ExpressionValidationContext;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.Critique;
import hu.blackbelt.judo.zeta.annotation.Guard;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;
import org.eclipse.emf.ecore.EObject;

import java.util.Collection;

/**
 * Validation rules for logical expression elements.
 *
 * <p>Corresponds to constraints in logical.evl</p>
 */
@ValidationContext(LogicalExpression.class)
public class LogicalExpressionValidations {

    /**
     * Generic resolved check for logical expressions.
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
    // LogicalAttribute
    // =========================================================================

    /**
     * Guard: Check if element is LogicalAttribute and resolved.
     */
    public boolean isLogicalAttributeAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof LogicalAttribute)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED);
    }

    /**
     * Validates that LogicalAttribute's attribute type is boolean.
     */
    @Constraint(
            name = ValidationConstants.ATTRIBUTE_TYPE_IS_BOOLEAN,
            message = "Attribute type is not boolean"
    )
    @Guard(method = "isLogicalAttributeAndResolved")
    public ValidationRule attributeTypeIsBoolean() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            LogicalAttribute self = (LogicalAttribute) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            // getAttributeType returns Optional<P> wrapped in Object
            Object attributeTypeObj = self.getAttributeType(modelAdapter);
            if (attributeTypeObj instanceof java.util.Optional) {
                java.util.Optional<?> optionalType = (java.util.Optional<?>) attributeTypeObj;
                if (optionalType.isPresent() && modelAdapter.isBoolean(optionalType.get())) {
                    return ValidationResult.pass();
                }
            } else if (attributeTypeObj != null && modelAdapter.isBoolean(attributeTypeObj)) {
                return ValidationResult.pass();
            }

            String objectTypeName = self.getObjectExpression() != null 
                    ? String.valueOf(self.getObjectExpression().getObjectType(modelAdapter))
                    : "unknown";
            return ValidationResult.fail(
                    "Attribute type of " + self.getAttributeName() + 
                    " of object type " + objectTypeName + " is not boolean"
            );
        };
    }

    // =========================================================================
    // InstanceOfExpression
    // =========================================================================

    /**
     * Guard: Check if element is InstanceOfExpression with all prerequisites resolved.
     */
    public boolean isInstanceOfExpressionWithPrerequisites(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof InstanceOfExpression)) {
            return false;
        }
        InstanceOfExpression self = (InstanceOfExpression) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED)
                && self.getObjectExpression() != null 
                && exprCtx.satisfies(self.getObjectExpression(), ValidationConstants.RESOLVED)
                && self.getElementName() != null
                && exprCtx.satisfies(self.getElementName(), ValidationConstants.OBJECT_TYPE_IS_VALID);
    }

    /**
     * Validates that InstanceOfExpression element type is compatible.
     */
    @Constraint(
            name = ValidationConstants.ELEMENT_TYPE_IS_COMPATIBLE,
            message = "Element type is not compatible"
    )
    @Guard(method = "isInstanceOfExpressionWithPrerequisites")
    public ValidationRule instanceOfElementTypeIsCompatible() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            InstanceOfExpression self = (InstanceOfExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            Object elementType = self.getElementName().get(modelAdapter);
            Object objectType = self.getObjectExpression().getObjectType(modelAdapter);

            if (elementType == null || objectType == null) {
                return ValidationResult.pass();
            }

            // Check if element type equals object type or is a subtype
            if (elementType.equals(objectType)) {
                return ValidationResult.pass();
            }

            Collection<?> superTypes = modelAdapter.getSuperTypes(elementType);
            if (superTypes != null && superTypes.contains(objectType)) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Element type (" + elementType + ") is not compatible with " + objectType
            );
        };
    }

    // =========================================================================
    // TypeOfExpression
    // =========================================================================

    /**
     * Guard: Check if element is TypeOfExpression with all prerequisites resolved.
     */
    public boolean isTypeOfExpressionWithPrerequisites(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof TypeOfExpression)) {
            return false;
        }
        TypeOfExpression self = (TypeOfExpression) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED)
                && self.getObjectExpression() != null 
                && exprCtx.satisfies(self.getObjectExpression(), ValidationConstants.RESOLVED)
                && self.getElementName() != null
                && exprCtx.satisfies(self.getElementName(), ValidationConstants.OBJECT_TYPE_IS_VALID);
    }

    /**
     * Validates that TypeOfExpression element type is compatible.
     */
    @Constraint(
            name = "TypeOfElementTypeIsCompatible",
            message = "Element type is not compatible"
    )
    @Guard(method = "isTypeOfExpressionWithPrerequisites")
    public ValidationRule typeOfElementTypeIsCompatible() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            TypeOfExpression self = (TypeOfExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            Object elementType = self.getElementName().get(modelAdapter);
            Object objectType = self.getObjectExpression().getObjectType(modelAdapter);

            if (elementType == null || objectType == null) {
                return ValidationResult.pass();
            }

            if (elementType.equals(objectType)) {
                return ValidationResult.pass();
            }

            Collection<?> superTypes = modelAdapter.getSuperTypes(elementType);
            if (superTypes != null && superTypes.contains(objectType)) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Element type (" + elementType + ") is not " + objectType
            );
        };
    }

    // =========================================================================
    // ContainsExpression / MemberOfExpression
    // =========================================================================

    /**
     * Guard: Check if element is ContainsExpression and resolved.
     */
    public boolean isContainsExpressionAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof ContainsExpression)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED);
    }

    /**
     * Guard: Check if element is MemberOfExpression and resolved.
     */
    public boolean isMemberOfExpressionAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof MemberOfExpression)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED);
    }

    /**
     * Validates that ContainsExpression types are compatible.
     */
    @Constraint(
            name = "ContainsTypesAreCompatible",
            message = "Types of collection and object are not compatible"
    )
    @Guard(method = "isContainsExpressionAndResolved")
    public ValidationRule containsTypesAreCompatible() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ContainsExpression self = (ContainsExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            Object collectionType = self.getCollectionExpression().getObjectType(modelAdapter);
            Object objectType = self.getObjectExpression().getObjectType(modelAdapter);

            if (collectionType == null || objectType == null) {
                return ValidationResult.pass();
            }

            if (collectionType.equals(objectType)) {
                return ValidationResult.pass();
            }

            Collection<?> collectionSuperTypes = modelAdapter.getSuperTypes(collectionType);
            Collection<?> objectSuperTypes = modelAdapter.getSuperTypes(objectType);

            if ((collectionSuperTypes != null && collectionSuperTypes.contains(objectType)) ||
                (objectSuperTypes != null && objectSuperTypes.contains(collectionType))) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Types of collection and object are not compatible");
        };
    }

    /**
     * Validates that MemberOfExpression types are compatible.
     */
    @Constraint(
            name = "MemberOfTypesAreCompatible",
            message = "Types of collection and object are not compatible"
    )
    @Guard(method = "isMemberOfExpressionAndResolved")
    public ValidationRule memberOfTypesAreCompatible() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            MemberOfExpression self = (MemberOfExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            Object collectionType = self.getCollectionExpression().getObjectType(modelAdapter);
            Object objectType = self.getObjectExpression().getObjectType(modelAdapter);

            if (collectionType == null || objectType == null) {
                return ValidationResult.pass();
            }

            if (collectionType.equals(objectType)) {
                return ValidationResult.pass();
            }

            Collection<?> collectionSuperTypes = modelAdapter.getSuperTypes(collectionType);
            Collection<?> objectSuperTypes = modelAdapter.getSuperTypes(objectType);

            if ((collectionSuperTypes != null && collectionSuperTypes.contains(objectType)) ||
                (objectSuperTypes != null && objectSuperTypes.contains(collectionType))) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Types of collection and object are not compatible");
        };
    }

    // =========================================================================
    // DecimalComparison Critique
    // =========================================================================

    /**
     * Guard: Check if element is DecimalComparison and resolved.
     */
    public boolean isDecimalComparisonAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof DecimalComparison)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED);
    }

    /**
     * Critique: Recommends using integer comparison when both operands are integers.
     */
    @Critique(
            name = ValidationConstants.INTEGER_COMPARISON_IS_RECOMMENDED,
            message = "Both arguments are integer so integer comparison is recommended"
    )
    @Guard(method = "isDecimalComparisonAndResolved")
    public ValidationRule integerComparisonIsRecommended() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            DecimalComparison self = (DecimalComparison) element;

            if (self.getLeft() instanceof IntegerExpression && 
                self.getRight() instanceof IntegerExpression) {
                return ValidationResult.fail(
                        "Both arguments are integer so integer comparison is recommended in: " + self
                );
            }
            return ValidationResult.pass();
        };
    }

    // =========================================================================
    // Exists / ForAll / Empty
    // =========================================================================

    /**
     * Guard: Check if element is Exists with resolved collection.
     */
    public boolean isExistsWithResolvedCollection(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof Exists)) {
            return false;
        }
        Exists self = (Exists) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return self.getCollectionExpression() != null 
                && exprCtx.satisfies(self.getCollectionExpression(), ValidationConstants.RESOLVED);
    }

    /**
     * Guard: Check if element is ForAll with resolved collection.
     */
    public boolean isForAllWithResolvedCollection(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof ForAll)) {
            return false;
        }
        ForAll self = (ForAll) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return self.getCollectionExpression() != null 
                && exprCtx.satisfies(self.getCollectionExpression(), ValidationConstants.RESOLVED);
    }

    /**
     * Guard: Check if element is Empty with resolved collection.
     */
    public boolean isEmptyWithResolvedCollection(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof Empty)) {
            return false;
        }
        Empty self = (Empty) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return self.getCollectionExpression() != null 
                && exprCtx.satisfies(self.getCollectionExpression(), ValidationConstants.RESOLVED);
    }

    /**
     * Validates Exists expression is resolved.
     */
    @Constraint(
            name = "ExistsResolved",
            message = "Unsupported expression"
    )
    @Guard(method = "isExistsWithResolvedCollection")
    public ValidationRule existsResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
            return ValidationResult.pass();
        };
    }

    /**
     * Validates ForAll expression is resolved.
     */
    @Constraint(
            name = "ForAllResolved",
            message = "Unsupported expression"
    )
    @Guard(method = "isForAllWithResolvedCollection")
    public ValidationRule forAllResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
            return ValidationResult.pass();
        };
    }

    /**
     * Validates Empty expression is resolved.
     */
    @Constraint(
            name = "EmptyResolved",
            message = "Unsupported expression"
    )
    @Guard(method = "isEmptyWithResolvedCollection")
    public ValidationRule emptyResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
            return ValidationResult.pass();
        };
    }
}
