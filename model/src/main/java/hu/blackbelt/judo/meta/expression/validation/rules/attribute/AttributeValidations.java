package hu.blackbelt.judo.meta.expression.validation.rules.attribute;

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

import hu.blackbelt.judo.meta.expression.AttributeSelector;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.validation.ExpressionValidationContext;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;
import org.eclipse.emf.ecore.EObject;

/**
 * Validation rules for attribute selector elements.
 *
 * <p>Corresponds to constraints in attribute.evl</p>
 */
@ValidationContext(AttributeSelector.class)
public class AttributeValidations {

    @Constraint(
            name = ValidationConstants.RESOLVED,
            message = "Attribute not found"
    )
    public ValidationRule resolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            AttributeSelector self = (AttributeSelector) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            // Note: Unlike EVL, we don't require the object expression to be "Resolved" first,
            // since Zeta validation runs constraints in a single pass and may not guarantee order.
            if (self.getObjectExpression() != null) {
                
                // getAttributeType returns Optional<P> wrapped in Object
                Object attributeTypeObj = self.getAttributeType(modelAdapter);
                boolean hasAttributeType = false;
                if (attributeTypeObj instanceof java.util.Optional) {
                    java.util.Optional<?> optionalType = (java.util.Optional<?>) attributeTypeObj;
                    hasAttributeType = optionalType.isPresent();
                } else if (attributeTypeObj != null) {
                    hasAttributeType = true;
                }
                
                if (hasAttributeType) {
                    exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                    return ValidationResult.pass();
                }

                Object objectType = self.getObjectExpression().getObjectType(modelAdapter);
                String typeName = objectType != null ? objectType.toString() : "unknown";
                return ValidationResult.fail(
                        "Attribute named " + self.getAttributeName() + 
                        " of object type " + typeName + " not found"
                );
            }
            return ValidationResult.pass();
        };
    }
}
