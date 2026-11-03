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

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.VariableReference;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionEvaluator;
import hu.blackbelt.judo.meta.expression.validation.ExpressionValidationContext;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import hu.blackbelt.judo.meta.expression.variable.Variable;
import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.Guard;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;
import org.eclipse.emf.ecore.EObject;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.meta.expression.runtime.ExpressionUtils.getValue;

/**
 * Validation rules for Expression elements.
 *
 * <p>Corresponds to constraints in expression.evl:</p>
 * <pre>
 * context EXPR!Expression {
 *     constraint LambdaVariableIsValid {
 *         guard: evaluator.isLambdaFunction(self)
 *         check: evaluator.getVariablesOfScope(self).containsAll(
 *             evaluator.getExpressionTerms(self)
 *                 .select(e | e.isKindOf(EXPR!VariableReference))
 *                 .collect(e | e.variable))
 *         message: "Invalid variable references: ..."
 *     }
 * }
 * </pre>
 */
@ValidationContext(Expression.class)
public class ExpressionValidations {

    /**
     * Guard: Check if expression is a lambda function.
     */
    public boolean isLambdaFunction(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        Expression self = (Expression) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        ExpressionEvaluator evaluator = exprCtx.getEvaluator();
        return evaluator.isLambdaFunction(self);
    }

    /**
     * Validates that lambda variable references are valid within their scope.
     */
    @Constraint(
            name = ValidationConstants.LAMBDA_VARIABLE_IS_VALID,
            message = "Invalid variable references in expression"
    )
    @Guard(method = "isLambdaFunction")
    public ValidationRule lambdaVariableIsValid() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            Expression self = (Expression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ExpressionEvaluator evaluator = exprCtx.getEvaluator();

            Collection<Variable> scopeVariables = evaluator.getVariablesOfScope(self);
            
            // Get referenced variables using reflection (same as EVL's e.variable)
            List<Object> referencedVariables = evaluator.getExpressionTerms(self).stream()
                    .filter(e -> e instanceof VariableReference)
                    .map(e -> getValue(e, "variable"))
                    .filter(v -> v != null)
                    .collect(Collectors.toList());

            if (scopeVariables.containsAll(referencedVariables)) {
                exprCtx.markSatisfied(element, ValidationConstants.LAMBDA_VARIABLE_IS_VALID);
                return ValidationResult.pass();
            }

            // Find invalid references
            List<Object> invalidRefs = referencedVariables.stream()
                    .filter(v -> !scopeVariables.contains(v))
                    .collect(Collectors.toList());

            return ValidationResult.fail(
                    "Invalid variable references: " + invalidRefs + " in expression: " + self
            );
        };
    }
}
