package hu.blackbelt.judo.meta.expression.validation.rules.temporal;

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
import hu.blackbelt.judo.meta.expression.temporal.*;
import hu.blackbelt.judo.meta.expression.variable.DateEnvironmentVariable;
import hu.blackbelt.judo.meta.expression.variable.TimeEnvironmentVariable;
import hu.blackbelt.judo.meta.expression.variable.TimestampEnvironmentVariable;
import hu.blackbelt.judo.meta.expression.validation.ExpressionValidationContext;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import hu.blackbelt.judo.zeta.annotation.Constraint;
import hu.blackbelt.judo.zeta.annotation.Guard;
import hu.blackbelt.judo.zeta.annotation.ValidationContext;
import hu.blackbelt.judo.zeta.validation.core.ValidationResult;
import hu.blackbelt.judo.zeta.validation.core.ValidationRule;
import org.eclipse.emf.ecore.EObject;

/**
 * Validation rules for temporal expression elements.
 *
 * <p>Corresponds to constraints in temporal.evl</p>
 */
@ValidationContext(Expression.class)
public class TemporalExpressionValidations {

    // =========================================================================
    // DateAttribute
    // =========================================================================

    /**
     * Guard: Check if element is DateAttribute and resolved.
     */
    public boolean isDateAttributeAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof DateAttribute)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED);
    }

    /**
     * Validates that DateAttribute's attribute type is date.
     */
    @Constraint(
            name = ValidationConstants.ATTRIBUTE_TYPE_IS_DATE,
            message = "Attribute type is not date"
    )
    @Guard(method = "isDateAttributeAndResolved")
    public ValidationRule attributeTypeIsDate() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            DateAttribute self = (DateAttribute) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            // getAttributeType returns Optional<P> wrapped in Object
            Object attributeTypeObj = self.getAttributeType(modelAdapter);
            if (attributeTypeObj instanceof java.util.Optional) {
                java.util.Optional<?> optionalType = (java.util.Optional<?>) attributeTypeObj;
                if (optionalType.isPresent() && modelAdapter.isDate(optionalType.get())) {
                    return ValidationResult.pass();
                }
            } else if (attributeTypeObj != null && modelAdapter.isDate(attributeTypeObj)) {
                return ValidationResult.pass();
            }

            String objectTypeName = self.getObjectExpression() != null 
                    ? String.valueOf(self.getObjectExpression().getObjectType(modelAdapter))
                    : "unknown";
            return ValidationResult.fail(
                    "Attribute type of " + self.getAttributeName() + 
                    " of object type " + objectTypeName + " is not date"
            );
        };
    }

    // =========================================================================
    // TimestampAttribute
    // =========================================================================

    /**
     * Guard: Check if element is TimestampAttribute and resolved.
     */
    public boolean isTimestampAttributeAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof TimestampAttribute)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED);
    }

    /**
     * Validates that TimestampAttribute's attribute type is timestamp.
     */
    @Constraint(
            name = ValidationConstants.ATTRIBUTE_TYPE_IS_TIMESTAMP,
            message = "Attribute type is not timestamp"
    )
    @Guard(method = "isTimestampAttributeAndResolved")
    public ValidationRule attributeTypeIsTimestamp() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            TimestampAttribute self = (TimestampAttribute) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            // getAttributeType returns Optional<P> wrapped in Object
            Object attributeTypeObj = self.getAttributeType(modelAdapter);
            if (attributeTypeObj instanceof java.util.Optional) {
                java.util.Optional<?> optionalType = (java.util.Optional<?>) attributeTypeObj;
                if (optionalType.isPresent() && modelAdapter.isTimestamp(optionalType.get())) {
                    return ValidationResult.pass();
                }
            } else if (attributeTypeObj != null && modelAdapter.isTimestamp(attributeTypeObj)) {
                return ValidationResult.pass();
            }

            String objectTypeName = self.getObjectExpression() != null 
                    ? String.valueOf(self.getObjectExpression().getObjectType(modelAdapter))
                    : "unknown";
            return ValidationResult.fail(
                    "Attribute type of " + self.getAttributeName() + 
                    " of object type " + objectTypeName + " is not timestamp"
            );
        };
    }

    // =========================================================================
    // TimeAttribute
    // =========================================================================

    /**
     * Guard: Check if element is TimeAttribute and resolved.
     */
    public boolean isTimeAttributeAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof TimeAttribute)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED);
    }

    /**
     * Validates that TimeAttribute's attribute type is time.
     */
    @Constraint(
            name = ValidationConstants.ATTRIBUTE_TYPE_IS_TIME,
            message = "Attribute type is not time"
    )
    @Guard(method = "isTimeAttributeAndResolved")
    public ValidationRule attributeTypeIsTime() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            TimeAttribute self = (TimeAttribute) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            // getAttributeType returns Optional<P> wrapped in Object
            Object attributeTypeObj = self.getAttributeType(modelAdapter);
            if (attributeTypeObj instanceof java.util.Optional) {
                java.util.Optional<?> optionalType = (java.util.Optional<?>) attributeTypeObj;
                if (optionalType.isPresent() && modelAdapter.isTime(optionalType.get())) {
                    return ValidationResult.pass();
                }
            } else if (attributeTypeObj != null && modelAdapter.isTime(attributeTypeObj)) {
                return ValidationResult.pass();
            }

            String objectTypeName = self.getObjectExpression() != null 
                    ? String.valueOf(self.getObjectExpression().getObjectType(modelAdapter))
                    : "unknown";
            return ValidationResult.fail(
                    "Attribute type of " + self.getAttributeName() + 
                    " of object type " + objectTypeName + " is not time"
            );
        };
    }

    // =========================================================================
    // TimestampAdditionExpression
    // =========================================================================

    /**
     * Guard: Check if element is TimestampAdditionExpression.
     */
    public boolean isTimestampAdditionExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof TimestampAdditionExpression;
    }

    /**
     * Validates TimestampAdditionExpression is resolved.
     */
    @Constraint(
            name = "TimestampAdditionResolved",
            message = "Duration is not measured"
    )
    @Guard(method = "isTimestampAdditionExpression")
    public ValidationRule timestampAdditionResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            TimestampAdditionExpression self = (TimestampAdditionExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (self.getTimestamp() == null || 
                !exprCtx.satisfies(self.getTimestamp(), ValidationConstants.RESOLVED)) {
                return ValidationResult.pass();
            }
            if (self.getDuration() == null || 
                !exprCtx.satisfies(self.getDuration(), ValidationConstants.RESOLVED)) {
                return ValidationResult.pass();
            }

            if (self.getDuration().isMeasured(modelAdapter)) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Duration is not measured: " + self.getDuration());
        };
    }

    /**
     * Guard: Check if element is TimestampAdditionExpression and resolved.
     */
    public boolean isTimestampAdditionExpressionAndResolved(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof TimestampAdditionExpression)) {
            return false;
        }
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        return exprCtx.satisfies(element, ValidationConstants.RESOLVED);
    }

    /**
     * Validates TimestampAdditionExpression duration is system unit.
     */
    @Constraint(
            name = ValidationConstants.DURATION_IS_SYSTEM_UNIT,
            message = "Duration is not a valid temporal unit"
    )
    @Guard(method = "isTimestampAdditionExpressionAndResolved")
    public ValidationRule timestampAdditionDurationIsSystemUnit() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            TimestampAdditionExpression self = (TimestampAdditionExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            Object unit = modelAdapter.getUnit(self.getDuration()).orElse(null);
            if (unit != null && modelAdapter.isDurationSupportingAddition(unit)) {
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Duration is not a valid temporal unit: " + self.getDuration());
        };
    }

    // =========================================================================
    // TimeAdditionExpression
    // =========================================================================

    /**
     * Guard: Check if element is TimeAdditionExpression.
     */
    public boolean isTimeAdditionExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof TimeAdditionExpression;
    }

    /**
     * Validates TimeAdditionExpression is resolved.
     */
    @Constraint(
            name = "TimeAdditionResolved",
            message = "Duration is not measured"
    )
    @Guard(method = "isTimeAdditionExpression")
    public ValidationRule timeAdditionResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            TimeAdditionExpression self = (TimeAdditionExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (self.getTime() == null || 
                !exprCtx.satisfies(self.getTime(), ValidationConstants.RESOLVED)) {
                return ValidationResult.pass();
            }
            if (self.getDuration() == null || 
                !exprCtx.satisfies(self.getDuration(), ValidationConstants.RESOLVED)) {
                return ValidationResult.pass();
            }

            if (self.getDuration().isMeasured(modelAdapter)) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Duration is not measured: " + self.getDuration());
        };
    }

    // =========================================================================
    // TimestampDifferenceExpression
    // =========================================================================

    /**
     * Guard: Check if element is TimestampDifferenceExpression.
     */
    public boolean isTimestampDifferenceExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof TimestampDifferenceExpression;
    }

    /**
     * Validates TimestampDifferenceExpression is resolved.
     */
    @Constraint(
            name = "TimestampDifferenceResolved",
            message = "Difference of timestamps is not duration"
    )
    @Guard(method = "isTimestampDifferenceExpression")
    public ValidationRule timestampDifferenceResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            TimestampDifferenceExpression self = (TimestampDifferenceExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            if (self.getStartTimestamp() == null || 
                !exprCtx.satisfies(self.getStartTimestamp(), ValidationConstants.RESOLVED)) {
                return ValidationResult.pass();
            }
            if (self.getEndTimestamp() == null || 
                !exprCtx.satisfies(self.getEndTimestamp(), ValidationConstants.RESOLVED)) {
                return ValidationResult.pass();
            }

            if (self.getMeasure() != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Difference of timestamps is not duration");
        };
    }

    // =========================================================================
    // TimeDifferenceExpression
    // =========================================================================

    /**
     * Guard: Check if element is TimeDifferenceExpression.
     */
    public boolean isTimeDifferenceExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof TimeDifferenceExpression;
    }

    /**
     * Validates TimeDifferenceExpression is resolved.
     */
    @Constraint(
            name = "TimeDifferenceResolved",
            message = "Difference of time is not duration"
    )
    @Guard(method = "isTimeDifferenceExpression")
    public ValidationRule timeDifferenceResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            TimeDifferenceExpression self = (TimeDifferenceExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            if (self.getStartTime() == null || 
                !exprCtx.satisfies(self.getStartTime(), ValidationConstants.RESOLVED)) {
                return ValidationResult.pass();
            }
            if (self.getEndTime() == null || 
                !exprCtx.satisfies(self.getEndTime(), ValidationConstants.RESOLVED)) {
                return ValidationResult.pass();
            }

            if (self.getMeasure() != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Difference of time is not duration");
        };
    }

    // =========================================================================
    // DateSwitchExpression
    // =========================================================================

    /**
     * Guard: Check if element is DateSwitchExpression with default.
     * Note: Unlike EVL, we don't require the default expression to be "Resolved" first,
     * since Zeta validation runs constraints in a single pass. We simply check the type.
     */
    public boolean isDateSwitchWithDefault(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof DateSwitchExpression)) {
            return false;
        }
        DateSwitchExpression self = (DateSwitchExpression) element;
        return self.getDefaultExpression() != null;
    }

    /**
     * Validates that DateSwitchExpression default case is date.
     */
    @Constraint(
            name = ValidationConstants.TYPE_OF_DEFAULT_CASE_IS_DATE,
            message = "Type of default case expression is not date"
    )
    @Guard(method = "isDateSwitchWithDefault")
    public ValidationRule typeOfDefaultCaseIsDate() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            DateSwitchExpression self = (DateSwitchExpression) element;
            if (self.getDefaultExpression() instanceof DateExpression) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail(
                    "Type of default case expression is not date (expression type): " + self.getDefaultExpression()
            );
        };
    }

    // =========================================================================
    // TimestampSwitchExpression
    // =========================================================================

    /**
     * Guard: Check if element is TimestampSwitchExpression with default.
     * Note: Unlike EVL, we don't require the default expression to be "Resolved" first,
     * since Zeta validation runs constraints in a single pass. We simply check the type.
     */
    public boolean isTimestampSwitchWithDefault(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof TimestampSwitchExpression)) {
            return false;
        }
        TimestampSwitchExpression self = (TimestampSwitchExpression) element;
        return self.getDefaultExpression() != null;
    }

    /**
     * Validates that TimestampSwitchExpression default case is timestamp.
     */
    @Constraint(
            name = ValidationConstants.TYPE_OF_DEFAULT_CASE_IS_TIMESTAMP,
            message = "Type of default case expression is not timestamp"
    )
    @Guard(method = "isTimestampSwitchWithDefault")
    public ValidationRule typeOfDefaultCaseIsTimestamp() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            TimestampSwitchExpression self = (TimestampSwitchExpression) element;
            if (self.getDefaultExpression() instanceof TimestampExpression) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail(
                    "Type of default case expression is not timestamp (expression type): " + self.getDefaultExpression()
            );
        };
    }

    // =========================================================================
    // TimeSwitchExpression
    // =========================================================================

    /**
     * Guard: Check if element is TimeSwitchExpression with default.
     * Note: Unlike EVL, we don't require the default expression to be "Resolved" first,
     * since Zeta validation runs constraints in a single pass. We simply check the type.
     */
    public boolean isTimeSwitchWithDefault(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        if (!(element instanceof TimeSwitchExpression)) {
            return false;
        }
        TimeSwitchExpression self = (TimeSwitchExpression) element;
        return self.getDefaultExpression() != null;
    }

    /**
     * Validates that TimeSwitchExpression default case is time.
     */
    @Constraint(
            name = ValidationConstants.TYPE_OF_DEFAULT_CASE_IS_TIME,
            message = "Type of default case expression is not time"
    )
    @Guard(method = "isTimeSwitchWithDefault")
    public ValidationRule typeOfDefaultCaseIsTime() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            TimeSwitchExpression self = (TimeSwitchExpression) element;
            if (self.getDefaultExpression() instanceof TimeExpression) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail(
                    "Type of default case expression is not time (expression type): " + self.getDefaultExpression()
            );
        };
    }

    // =========================================================================
    // Environment Variables
    // =========================================================================

    /**
     * Guard: Check if element is TimestampEnvironmentVariable.
     */
    public boolean isTimestampEnvironmentVariable(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof TimestampEnvironmentVariable;
    }

    /**
     * Guard: Check if element is TimeEnvironmentVariable.
     */
    public boolean isTimeEnvironmentVariable(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof TimeEnvironmentVariable;
    }

    /**
     * Guard: Check if element is DateEnvironmentVariable.
     */
    public boolean isDateEnvironmentVariable(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof DateEnvironmentVariable;
    }

    /**
     * Validates TimestampEnvironmentVariable is resolved.
     */
    @Constraint(
            name = "TimestampEnvironmentVariableResolved",
            message = "Value of timestamp environment variable is not defined"
    )
    @Guard(method = "isTimestampEnvironmentVariable")
    public ValidationRule timestampEnvironmentVariableResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            TimestampEnvironmentVariable self = (TimestampEnvironmentVariable) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            if (self.getVariableName() != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Value of timestamp environment variable is not defined: " + self);
        };
    }

    /**
     * Validates TimeEnvironmentVariable is resolved.
     */
    @Constraint(
            name = "TimeEnvironmentVariableResolved",
            message = "Value of time environment variable is not defined"
    )
    @Guard(method = "isTimeEnvironmentVariable")
    public ValidationRule timeEnvironmentVariableResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            TimeEnvironmentVariable self = (TimeEnvironmentVariable) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            if (self.getVariableName() != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Value of time environment variable is not defined: " + self);
        };
    }

    /**
     * Validates DateEnvironmentVariable is resolved.
     */
    @Constraint(
            name = "DateEnvironmentVariableResolved",
            message = "Value of date environment variable is not defined"
    )
    @Guard(method = "isDateEnvironmentVariable")
    public ValidationRule dateEnvironmentVariableResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            DateEnvironmentVariable self = (DateEnvironmentVariable) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;

            if (self.getVariableName() != null) {
                exprCtx.markSatisfied(element, ValidationConstants.RESOLVED);
                return ValidationResult.pass();
            }

            return ValidationResult.fail("Value of date environment variable is not defined: " + self);
        };
    }

    // =========================================================================
    // Aggregated Expressions
    // =========================================================================

    /**
     * Guard: Check if element is DateAggregatedExpression.
     */
    public boolean isDateAggregatedExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof DateAggregatedExpression;
    }

    /**
     * Guard: Check if element is TimestampAggregatedExpression.
     */
    public boolean isTimestampAggregatedExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof TimestampAggregatedExpression;
    }

    /**
     * Guard: Check if element is TimeAggregatedExpression.
     */
    public boolean isTimeAggregatedExpression(EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) {
        return element instanceof TimeAggregatedExpression;
    }

    /**
     * Validates DateAggregatedExpression is resolved.
     */
    @Constraint(
            name = "DateAggregatedExpressionResolved",
            message = "Attribute not found"
    )
    @Guard(method = "isDateAggregatedExpression")
    public ValidationRule dateAggregatedExpressionResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            DateAggregatedExpression self = (DateAggregatedExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (self.getCollectionExpression() == null || 
                !exprCtx.satisfies(self.getCollectionExpression(), ValidationConstants.RESOLVED)) {
                return ValidationResult.pass();
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

    /**
     * Validates TimestampAggregatedExpression is resolved.
     */
    @Constraint(
            name = "TimestampAggregatedExpressionResolved",
            message = "Attribute not found"
    )
    @Guard(method = "isTimestampAggregatedExpression")
    public ValidationRule timestampAggregatedExpressionResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            TimestampAggregatedExpression self = (TimestampAggregatedExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (self.getCollectionExpression() == null || 
                !exprCtx.satisfies(self.getCollectionExpression(), ValidationConstants.RESOLVED)) {
                return ValidationResult.pass();
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

    /**
     * Validates TimeAggregatedExpression is resolved.
     */
    @Constraint(
            name = "TimeAggregatedExpressionResolved",
            message = "Attribute not found"
    )
    @Guard(method = "isTimeAggregatedExpression")
    public ValidationRule timeAggregatedExpressionResolved() {
        return (EObject element, hu.blackbelt.judo.zeta.validation.core.ValidationContext ctx) -> {
            TimeAggregatedExpression self = (TimeAggregatedExpression) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            ModelAdapter modelAdapter = exprCtx.getModelAdapter();

            if (self.getCollectionExpression() == null || 
                !exprCtx.satisfies(self.getCollectionExpression(), ValidationConstants.RESOLVED)) {
                return ValidationResult.pass();
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
