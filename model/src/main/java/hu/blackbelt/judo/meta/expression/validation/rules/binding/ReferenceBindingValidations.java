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

import hu.blackbelt.judo.meta.expression.CollectionExpression;
import hu.blackbelt.judo.meta.expression.ObjectExpression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.binding.ReferenceBinding;
import hu.blackbelt.judo.meta.expression.validation.ExpressionValidationContext;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.Guard;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;
import org.eclipse.emf.ecore.EObject;

import java.util.Collection;
import java.util.Optional;

/**
 * Validation rules for reference binding elements.
 *
 * <p>Corresponds to constraints in referenceBinding.evl</p>
 */
@ValidationContext(ReferenceBinding.class)
public class ReferenceBindingValidations {

    /**
     * Validates that reference binding expression is a ReferenceExpression.
     */
    @Constraint(
            name = ValidationConstants.REFERENCE_BINDING_EXPRESSION_IS_VALID,
            message = "Expression of reference binding must be reference expression"
    )
    public ValidationRule referenceBindingExpressionIsValid() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ReferenceBinding self = (ReferenceBinding) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            if (self.getExpression() instanceof ReferenceExpression) {
                exprCtx.markSatisfied(element, ValidationConstants.REFERENCE_BINDING_EXPRESSION_IS_VALID);
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Expression of reference binding for reference named " + self.getReferenceName() + 
                    " must be reference expression"
            );
        };
    }

    /**
     * Guard: Check if reference binding expression is valid.
     */
    public boolean hasValidReferenceExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof ReferenceBinding)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.REFERENCE_BINDING_EXPRESSION_IS_VALID);
    }

    /**
     * Validates that reference expression matches binding.
     */
    @Constraint(
            name = ValidationConstants.REFERENCE_EXPRESSION_MATCHES_BINDING,
            message = "Reference does not match the type of the assigned expression"
    )
    @Guard(method = "hasValidReferenceExpression")
    public ValidationRule referenceExpressionMatchesBinding() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ReferenceBinding self = (ReferenceBinding) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            Optional<?> entityType = modelAdapter.getEntityTypeOfTransferObjectRelationTarget(
                    self.getTypeName(), self.getReferenceName());
            
            if (!entityType.isPresent()) {
                return ValidationResult.fail(
                        "Reference named " + self.getReferenceName() + 
                        " does not match the type of the assigned expression"
                );
            }

            ReferenceExpression refExpr = (ReferenceExpression) self.getExpression();
            Object expressionType = refExpr.getObjectType(modelAdapter);
            
            if (expressionType == null) {
                return ValidationResult.pass(); // Skip if expression type not resolved
            }

            // Check if expression type equals entity type or is a subtype
            if (expressionType.equals(entityType.get())) {
                return ValidationResult.pass();
            }

            Collection<?> superTypes = modelAdapter.getSuperTypes(expressionType);
            if (superTypes != null && superTypes.contains(entityType.get())) {
                return ValidationResult.pass();
            }

            // Also check the reverse - expressionType could be a supertype
            if (superTypes != null) {
                for (Object st : superTypes) {
                    if (st.equals(entityType.get())) {
                        return ValidationResult.pass();
                    }
                }
            }

            return ValidationResult.fail(
                    "Reference named " + self.getReferenceName() + 
                    " does not match the type of the assigned expression"
            );
        };
    }

    /**
     * Guard: Check if expression is ObjectExpression.
     */
    public boolean hasObjectExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof ReferenceBinding)) {
            return false;
        }
        ReferenceBinding self = (ReferenceBinding) element;
        return self.getExpression() instanceof ObjectExpression;
    }

    /**
     * Validates that object expression matches binding (reference should not be collection).
     */
    @Constraint(
            name = ValidationConstants.OBJECT_EXPRESSION_MATCHES_BINDING,
            message = "Reference refers to a collection but the assigned expression evaluates to an object"
    )
    @Guard(method = "hasObjectExpression")
    public ValidationRule objectExpressionMatchesBinding() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ReferenceBinding self = (ReferenceBinding) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (!modelAdapter.isCollectionReference(self.getTypeName(), self.getReferenceName())) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Reference named " + self.getReferenceName() + 
                    " refers to a collection but the assigned expression evaluates to an object."
            );
        };
    }

    /**
     * Guard: Check if expression is CollectionExpression.
     */
    public boolean hasCollectionExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof ReferenceBinding)) {
            return false;
        }
        ReferenceBinding self = (ReferenceBinding) element;
        return self.getExpression() instanceof CollectionExpression;
    }

    /**
     * Validates that collection expression matches binding (reference should be collection).
     */
    @Constraint(
            name = ValidationConstants.COLLECTION_EXPRESSION_MATCHES_BINDING,
            message = "Reference refers to an object but the assigned expression evaluates to a collection"
    )
    @Guard(method = "hasCollectionExpression")
    public ValidationRule collectionExpressionMatchesBinding() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            ReferenceBinding self = (ReferenceBinding) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (modelAdapter.isCollectionReference(self.getTypeName(), self.getReferenceName())) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Reference named " + self.getReferenceName() + 
                    " refers to an object but the assigned expression evaluates to a collection."
            );
        };
    }
}
