package hu.blackbelt.judo.meta.expression.validation.rules.measured;

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

import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal;
import hu.blackbelt.judo.meta.expression.logical.DecimalComparison;
import hu.blackbelt.judo.meta.expression.logical.IntegerComparison;
import hu.blackbelt.judo.meta.expression.numeric.*;
import hu.blackbelt.judo.meta.expression.operator.DecimalOperator;
import hu.blackbelt.judo.meta.expression.operator.IntegerOperator;
import hu.blackbelt.judo.meta.expression.validation.ExpressionValidationContext;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import hu.blackbelt.judo.meta.expression.variable.MeasuredDecimalEnvironmentVariable;
import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.Guard;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;
import org.eclipse.emf.ecore.EObject;

import java.util.Collection;
import java.util.Optional;

/**
 * Zeta validation rules for measured expressions.
 * Covers validation of numeric expressions with measures (units).
 * 
 * <p>Corresponds to constraints in measured.evl</p>
 */
@ValidationContext(NumericExpression.class)
public class MeasuredExpressionValidations {

    /**
     * Helper method to check if dimension is defined for a numeric expression.
     * Equivalent to EVL's isDimensionDefined() operation.
     */
    private boolean isDimensionDefined(NumericExpression expr, ModelAdapter modelAdapter) {
        Optional<?> dimension = modelAdapter.getDimension(expr);
        if (!dimension.isPresent()) {
            return false;
        }
        Object dim = dimension.get();
        if (dim instanceof Collection) {
            return !((Collection<?>) dim).isEmpty();
        }
        return dim != null;
    }

    // =========================================================================
    // MeasuredDecimal
    // =========================================================================

    public boolean isMeasuredDecimal(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof MeasuredDecimal;
    }

    @Constraint(
            name = ValidationConstants.MEASURE_IS_VALID,
            message = "Measure of decimal constant is invalid"
    )
    @Guard(method = "isMeasuredDecimal")
    public ValidationRule measuredDecimalMeasureIsValid() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            MeasuredDecimal self = (MeasuredDecimal) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (!exprCtx.satisfies(element, ValidationConstants.RESOLVED)) {
                return ValidationResult.pass();
            }

            if (isDimensionDefined(self, modelAdapter)) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail("Measure of decimal constant is invalid: " + self);
        };
    }

    // =========================================================================
    // MeasuredDecimalEnvironmentVariable
    // =========================================================================

    public boolean isMeasuredDecimalEnvironmentVariable(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof MeasuredDecimalEnvironmentVariable;
    }

    @Constraint(
            name = ValidationConstants.MEASURE_IS_VALID,
            message = "Measure of decimal environment variable is invalid"
    )
    @Guard(method = "isMeasuredDecimalEnvironmentVariable")
    public ValidationRule measuredDecimalEnvVarMeasureIsValid() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            MeasuredDecimalEnvironmentVariable self = (MeasuredDecimalEnvironmentVariable) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (!exprCtx.satisfies(element, ValidationConstants.RESOLVED)) {
                return ValidationResult.pass();
            }

            if (isDimensionDefined(self, modelAdapter)) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail("Measure of decimal environment variable is invalid: " + self);
        };
    }

    // =========================================================================
    // IntegerAttribute - MeasureIsValid
    // =========================================================================

    public boolean isIntegerAttributeMeasured(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof IntegerAttribute)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        ModelAdapter modelAdapter = exprCtx.getModelAdapter();
        return modelAdapter.isMeasured((IntegerAttribute) element);
    }

    @Constraint(
            name = ValidationConstants.MEASURE_IS_VALID,
            message = "Measure of expression is invalid"
    )
    @Guard(method = "isIntegerAttributeMeasured")
    public ValidationRule integerAttributeMeasureIsValid() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            IntegerAttribute self = (IntegerAttribute) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (!exprCtx.satisfies(element, ValidationConstants.RESOLVED)) {
                return ValidationResult.pass();
            }

            if (isDimensionDefined(self, modelAdapter)) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail("Measure of expression is invalid: " + self);
        };
    }

    // =========================================================================
    // DecimalAttribute - MeasureIsValid
    // =========================================================================

    public boolean isDecimalAttributeMeasured(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof DecimalAttribute)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        ModelAdapter modelAdapter = exprCtx.getModelAdapter();
        return modelAdapter.isMeasured((DecimalAttribute) element);
    }

    @Constraint(
            name = ValidationConstants.MEASURE_IS_VALID,
            message = "Measure of expression is invalid"
    )
    @Guard(method = "isDecimalAttributeMeasured")
    public ValidationRule decimalAttributeMeasureIsValid() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            DecimalAttribute self = (DecimalAttribute) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (!exprCtx.satisfies(element, ValidationConstants.RESOLVED)) {
                return ValidationResult.pass();
            }

            if (isDimensionDefined(self, modelAdapter)) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail("Measure of expression is invalid: " + self);
        };
    }

    // =========================================================================
    // IntegerArithmeticExpression - Measure validations
    // =========================================================================

    public boolean isIntegerArithmeticMeasured(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof IntegerArithmeticExpression)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        ModelAdapter modelAdapter = exprCtx.getModelAdapter();
        IntegerArithmeticExpression self = (IntegerArithmeticExpression) element;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED) && modelAdapter.isMeasured(self);
    }

    @Constraint(
            name = ValidationConstants.MEASURE_IS_VALID,
            message = "Measure of expression is invalid"
    )
    @Guard(method = "isIntegerArithmeticMeasured")
    public ValidationRule integerArithmeticMeasureIsValid() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            IntegerArithmeticExpression self = (IntegerArithmeticExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (modelAdapter.getDimension(self).isPresent()) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail("Measure of expression is invalid: " + self);
        };
    }

    public boolean isIntegerAdditionWithMeasure(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof IntegerArithmeticExpression)) {
            return false;
        }
        IntegerArithmeticExpression self = (IntegerArithmeticExpression) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        ModelAdapter modelAdapter = exprCtx.getModelAdapter();

        if (!exprCtx.satisfies(element, ValidationConstants.RESOLVED)) {
            return false;
        }

        boolean isAddOrSubtract = self.getOperator() == IntegerOperator.ADD || 
                                   self.getOperator() == IntegerOperator.SUBSTRACT;
        boolean hasMeasuredOperand = modelAdapter.isMeasured(self.getLeft()) || 
                                      modelAdapter.isMeasured(self.getRight());
        return isAddOrSubtract && hasMeasuredOperand;
    }

    @Constraint(
            name = ValidationConstants.MEASURE_OF_ADDITION_IS_VALID,
            message = "Measures of addition are not matching"
    )
    @Guard(method = "isIntegerAdditionWithMeasure")
    public ValidationRule integerAdditionMeasureIsValid() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            IntegerArithmeticExpression self = (IntegerArithmeticExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (!modelAdapter.isMeasured(self)) {
                return ValidationResult.fail("Measures of addition are not matching: " + self);
            }

            Optional<?> selfMeasure = modelAdapter.getMeasure(self);
            Optional<?> leftMeasure = modelAdapter.getMeasure(self.getLeft());
            Optional<?> rightMeasure = modelAdapter.getMeasure(self.getRight());

            Object selfM = selfMeasure.orElse(null);
            Object leftM = leftMeasure.orElse(null);
            Object rightM = rightMeasure.orElse(null);

            if (selfM != null && selfM.equals(leftM) && selfM.equals(rightM)) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail("Measures of addition are not matching: " + self);
        };
    }

    // =========================================================================
    // DecimalArithmeticExpression - Measure validations
    // =========================================================================

    public boolean isDecimalArithmeticMeasured(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof DecimalArithmeticExpression)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        ModelAdapter modelAdapter = exprCtx.getModelAdapter();
        DecimalArithmeticExpression self = (DecimalArithmeticExpression) element;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED) && modelAdapter.isMeasured(self);
    }

    @Constraint(
            name = ValidationConstants.MEASURE_IS_VALID,
            message = "Measure of expression is invalid"
    )
    @Guard(method = "isDecimalArithmeticMeasured")
    public ValidationRule decimalArithmeticMeasureIsValid() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            DecimalArithmeticExpression self = (DecimalArithmeticExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (modelAdapter.getDimension(self).isPresent()) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail("Measure of expression is invalid: " + self);
        };
    }

    public boolean isDecimalAdditionWithMeasure(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof DecimalArithmeticExpression)) {
            return false;
        }
        DecimalArithmeticExpression self = (DecimalArithmeticExpression) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        ModelAdapter modelAdapter = exprCtx.getModelAdapter();

        if (!exprCtx.satisfies(element, ValidationConstants.RESOLVED)) {
            return false;
        }

        boolean isAddOrSubtract = self.getOperator() == DecimalOperator.ADD || 
                                   self.getOperator() == DecimalOperator.SUBSTRACT;
        boolean hasMeasuredOperand = modelAdapter.isMeasured(self.getLeft()) || 
                                      modelAdapter.isMeasured(self.getRight());
        return isAddOrSubtract && hasMeasuredOperand;
    }

    @Constraint(
            name = ValidationConstants.MEASURE_OF_ADDITION_IS_VALID,
            message = "Measures of addition are not matching"
    )
    @Guard(method = "isDecimalAdditionWithMeasure")
    public ValidationRule decimalAdditionMeasureIsValid() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            DecimalArithmeticExpression self = (DecimalArithmeticExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (!modelAdapter.isMeasured(self)) {
                return ValidationResult.fail("Measures of addition are not matching: " + self);
            }

            Optional<?> selfMeasure = modelAdapter.getMeasure(self);
            Optional<?> leftMeasure = modelAdapter.getMeasure(self.getLeft());
            Optional<?> rightMeasure = modelAdapter.getMeasure(self.getRight());

            Object selfM = selfMeasure.orElse(null);
            Object leftM = leftMeasure.orElse(null);
            Object rightM = rightMeasure.orElse(null);

            if (selfM != null && selfM.equals(leftM) && selfM.equals(rightM)) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail("Measures of addition are not matching: " + self);
        };
    }

    // =========================================================================
    // IntegerComparison - Measure validations
    // =========================================================================

    public boolean isIntegerComparisonWithMeasure(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof IntegerComparison)) {
            return false;
        }
        IntegerComparison self = (IntegerComparison) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        ModelAdapter modelAdapter = exprCtx.getModelAdapter();

        if (!exprCtx.satisfies(element, ValidationConstants.RESOLVED)) {
            return false;
        }

        return modelAdapter.isMeasured(self.getLeft()) || modelAdapter.isMeasured(self.getRight());
    }

    @Constraint(
            name = ValidationConstants.COMPARISON_IS_MEASURED,
            message = "Both left and right sides must be measured"
    )
    @Guard(method = "isIntegerComparisonWithMeasure")
    public ValidationRule integerComparisonIsMeasured() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            IntegerComparison self = (IntegerComparison) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (modelAdapter.isMeasured(self.getLeft()) && modelAdapter.isMeasured(self.getRight())) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail("Both left and right sides must be measured: " + self);
        };
    }

    public boolean isIntegerComparisonBothMeasured(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof IntegerComparison)) {
            return false;
        }
        IntegerComparison self = (IntegerComparison) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        ModelAdapter modelAdapter = exprCtx.getModelAdapter();

        if (!exprCtx.satisfies(element, ValidationConstants.RESOLVED)) {
            return false;
        }
        if (!exprCtx.satisfies(element, ValidationConstants.COMPARISON_IS_MEASURED)) {
            return false;
        }

        boolean hasMeasuredOperand = modelAdapter.isMeasured(self.getLeft()) || modelAdapter.isMeasured(self.getRight());
        boolean leftValid = !modelAdapter.isMeasured(self.getLeft()) || exprCtx.satisfies(self.getLeft(), ValidationConstants.MEASURE_IS_VALID);
        boolean rightValid = !modelAdapter.isMeasured(self.getRight()) || exprCtx.satisfies(self.getRight(), ValidationConstants.MEASURE_IS_VALID);

        return hasMeasuredOperand && leftValid && rightValid;
    }

    @Constraint(
            name = ValidationConstants.MEASURES_ARE_MATCHING,
            message = "Dimensions of comparison are not matching"
    )
    @Guard(method = "isIntegerComparisonBothMeasured")
    public ValidationRule integerComparisonMeasuresMatch() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            IntegerComparison self = (IntegerComparison) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            Optional<?> leftDim = modelAdapter.getDimension(self.getLeft());
            Optional<?> rightDim = modelAdapter.getDimension(self.getRight());

            if (leftDim.equals(rightDim)) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail("Dimensions of comparison are not matching: " + self);
        };
    }

    // =========================================================================
    // DecimalComparison - Measure validations
    // =========================================================================

    public boolean isDecimalComparisonWithMeasure(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof DecimalComparison)) {
            return false;
        }
        DecimalComparison self = (DecimalComparison) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        ModelAdapter modelAdapter = exprCtx.getModelAdapter();

        if (!exprCtx.satisfies(element, ValidationConstants.RESOLVED)) {
            return false;
        }

        return modelAdapter.isMeasured(self.getLeft()) || modelAdapter.isMeasured(self.getRight());
    }

    @Constraint(
            name = ValidationConstants.COMPARISON_IS_MEASURED,
            message = "Both left and right sides must be measured"
    )
    @Guard(method = "isDecimalComparisonWithMeasure")
    public ValidationRule decimalComparisonIsMeasured() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            DecimalComparison self = (DecimalComparison) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (modelAdapter.isMeasured(self.getLeft()) && modelAdapter.isMeasured(self.getRight())) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail("Both left and right sides must be measured: " + self);
        };
    }

    public boolean isDecimalComparisonBothMeasured(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof DecimalComparison)) {
            return false;
        }
        DecimalComparison self = (DecimalComparison) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        ModelAdapter modelAdapter = exprCtx.getModelAdapter();

        if (!exprCtx.satisfies(element, ValidationConstants.RESOLVED)) {
            return false;
        }
        if (!exprCtx.satisfies(element, ValidationConstants.COMPARISON_IS_MEASURED)) {
            return false;
        }

        boolean hasMeasuredOperand = modelAdapter.isMeasured(self.getLeft()) || modelAdapter.isMeasured(self.getRight());
        boolean leftValid = !modelAdapter.isMeasured(self.getLeft()) || exprCtx.satisfies(self.getLeft(), ValidationConstants.MEASURE_IS_VALID);
        boolean rightValid = !modelAdapter.isMeasured(self.getRight()) || exprCtx.satisfies(self.getRight(), ValidationConstants.MEASURE_IS_VALID);

        return hasMeasuredOperand && leftValid && rightValid;
    }

    @Constraint(
            name = ValidationConstants.MEASURES_ARE_MATCHING,
            message = "Dimensions of comparison are not matching"
    )
    @Guard(method = "isDecimalComparisonBothMeasured")
    public ValidationRule decimalComparisonMeasuresMatch() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            DecimalComparison self = (DecimalComparison) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            Optional<?> leftDim = modelAdapter.getDimension(self.getLeft());
            Optional<?> rightDim = modelAdapter.getDimension(self.getRight());

            if (leftDim.equals(rightDim)) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail("Dimensions of comparison are not matching: " + self);
        };
    }
}
