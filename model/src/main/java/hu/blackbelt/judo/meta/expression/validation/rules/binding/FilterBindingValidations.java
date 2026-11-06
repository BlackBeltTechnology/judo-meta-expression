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

import hu.blackbelt.judo.meta.expression.LogicalExpression;
import hu.blackbelt.judo.meta.expression.binding.FilterBinding;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;
import org.eclipse.emf.ecore.EObject;

/**
 * Validation rules for filter binding elements.
 *
 * <p>Corresponds to constraints in filterBinding.evl</p>
 */
@ValidationContext(FilterBinding.class)
public class FilterBindingValidations {

    /**
     * Validates that filter binding expression is a LogicalExpression.
     */
    @Constraint(
            name = ValidationConstants.LOGICAL_EXPRESSION_MATCHES_BINDING,
            message = "Expression of filter binding must be logical expression"
    )
    public ValidationRule logicalExpressionMatchesBinding() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            FilterBinding self = (FilterBinding) element;

            if (self.getExpression() instanceof LogicalExpression) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail(
                    "Expression of filter binding for filter of " + self.getTypeName() + 
                    " must be logical expression"
            );
        };
    }
}
