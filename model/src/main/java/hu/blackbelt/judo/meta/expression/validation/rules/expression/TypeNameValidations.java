package hu.blackbelt.judo.meta.expression.validation.rules.expression;

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

import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.validation.ExpressionValidationContext;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;
import org.eclipse.emf.ecore.EObject;

/**
 * Validation rules for TypeName elements.
 *
 * <p>Corresponds to constraints in expression.evl:</p>
 * <pre>
 * context EXPR!TypeName {
 *     constraint ObjectTypeIsValid {
 *         check: self.get(modelAdapter).isDefined()
 *         message: "Element named " + self.name + " not found in namespace " + self.namespace
 *     }
 * }
 * </pre>
 */
@ValidationContext(TypeName.class)
public class TypeNameValidations {

    /**
     * Validates that an object type referenced by name exists in the namespace.
     */
    @Constraint(
            name = ValidationConstants.OBJECT_TYPE_IS_VALID,
            message = "Element not found in namespace"
    )
    public ValidationRule objectTypeIsValid() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            TypeName self = (TypeName) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (self.get(modelAdapter) != null) {
                exprCtx.markSatisfied(element, ValidationConstants.OBJECT_TYPE_IS_VALID);
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Element named " + self.getName() + 
                    " not found in namespace " + self.getNamespace()
            );
        };
    }
}
