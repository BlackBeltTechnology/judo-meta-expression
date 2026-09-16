package hu.blackbelt.judo.meta.expression.validation.constants;

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

/**
 * Constants for all validation constraint and critique names used in Expression model validation.
 * 
 * <p>These constants correspond to the EVL validation rules defined in:
 * {@code model/src/main/epsilon/validations/expression/}</p>
 * 
 * <p>Using constants ensures consistency between EVL and Java validation implementations
 * and enables compile-time checking of constraint name references.</p>
 */
public final class ValidationConstants {

    private ValidationConstants() {
        // Prevent instantiation
    }

    // =========================================================================
    // Common Constraints (used across multiple contexts)
    // =========================================================================
    
    /**
     * Generic "Resolved" constraint - used to verify that an expression can be resolved.
     * Applied to many expression types.
     */
    public static final String RESOLVED = "Resolved";
    
    /**
     * Verifies that the target of a navigation is a collection (or is not, depending on context).
     */
    public static final String TARGET_IS_COLLECTION = "TargetIsCollection";
    
    /**
     * Verifies that a type is defined for an expression.
     */
    public static final String TYPE_IS_DEFINED = "TypeIsDefined";

    // =========================================================================
    // Expression Core Constraints (expression.evl)
    // =========================================================================
    
    /**
     * Validates that an object type referenced by name exists in the namespace.
     */
    public static final String OBJECT_TYPE_IS_VALID = "ObjectTypeIsValid";
    
    /**
     * Validates that lambda variable references are valid within their scope.
     */
    public static final String LAMBDA_VARIABLE_IS_VALID = "LambdaVariableIsValid";

    // =========================================================================
    // Numeric Constraints (numeric.evl)
    // =========================================================================
    
    /**
     * Validates that an attribute type is integer.
     */
    public static final String ATTRIBUTE_TYPE_IS_INTEGER = "AttributeTypeIsInteger";
    
    /**
     * Validates that an attribute type is decimal.
     */
    public static final String ATTRIBUTE_TYPE_IS_DECIMAL = "AttributeTypeIsDecimal";
    
    /**
     * Validates that the default case expression type is numeric.
     */
    public static final String TYPE_OF_DEFAULT_CASE_IS_NUMERIC = "TypeOfDefaultCaseIsNumeric";
    
    /**
     * Validates that a switch case expression type is numeric.
     */
    public static final String TYPE_OF_SWITCH_CASE_IS_NUMERIC = "TypeOfSwitchCaseIsNumeric";

    // =========================================================================
    // Numeric Critiques (numeric.evl)
    // =========================================================================
    
    /**
     * Recommends using integer arithmetic when both operands are integers.
     */
    public static final String INTEGER_ARITHMETIC_EXPRESSION_IS_RECOMMENDED = 
            "IntegerArithmeticExpressionIsRecommended";
    
    /**
     * Warns when decimal operations are used on integer types.
     */
    public static final String ATTRIBUTE_TYPE_IS_DECIMAL_OR_OPERATION_IS_NOT_SUPPORTED_ON_INTEGERS = 
            "AttributeTypeIsDecimalOrOperationIsNotSupportedOnIntegers";

    // =========================================================================
    // Object Constraints (object.evl)
    // =========================================================================
    
    /**
     * Validates that the object selector operator is valid (only 'any' is supported).
     */
    public static final String OBJECT_SELECTOR_OPERATOR_IS_VALID = "ObjectSelectorOperatorIsValid";
    
    /**
     * Validates that a cast type is compatible with the source type.
     */
    public static final String CASTED_TYPE_IS_COMPATIBLE = "CastedTypeIsCompatible";

    // =========================================================================
    // Collection Constraints (collection.evl)
    // =========================================================================
    
    /**
     * Validates that a cast collection type is compatible.
     */
    public static final String CAST_TYPE_IS_COMPATIBLE = "CastTypeIsCompatible";

    // =========================================================================
    // Logical Constraints (logical.evl)
    // =========================================================================
    
    /**
     * Validates that an element type is compatible for instanceof/typeof checks.
     */
    public static final String ELEMENT_TYPE_IS_COMPATIBLE = "ElementTypeIsCompatible";
    
    /**
     * Validates that an attribute type is boolean.
     */
    public static final String ATTRIBUTE_TYPE_IS_BOOLEAN = "AttributeTypeIsBoolean";
    
    /**
     * Validates that types are compatible for contains/memberOf operations.
     */
    public static final String TYPES_ARE_COMPATIBLE = "TypesAreCompatible";

    // =========================================================================
    // Logical Critiques (logical.evl)
    // =========================================================================
    
    /**
     * Recommends using integer comparison when both operands are integers.
     */
    public static final String INTEGER_COMPARISON_IS_RECOMMENDED = "IntegerComparisonIsRecommended";

    // =========================================================================
    // String Constraints (string.evl)
    // =========================================================================
    
    /**
     * Validates that an attribute type is string.
     */
    public static final String ATTRIBUTE_TYPE_IS_STRING = "AttributeTypeIsString";
    
    /**
     * Validates that the default case expression type is string.
     */
    public static final String TYPE_OF_DEFAULT_CASE_IS_STRING = "TypeOfDefaultCaseIsString";
    
    /**
     * Validates that a switch case expression type is string.
     */
    public static final String TYPE_OF_SWITCH_CASE_IS_STRING = "TypeOfSwitchCaseIsString";

    // =========================================================================
    // Temporal Constraints (temporal.evl)
    // =========================================================================
    
    /**
     * Validates that a duration uses system units.
     */
    public static final String DURATION_IS_SYSTEM_UNIT = "DurationIsSystemUnit";
    
    /**
     * Validates that an attribute type is date.
     */
    public static final String ATTRIBUTE_TYPE_IS_DATE = "AttributeTypeIsDate";
    
    /**
     * Validates that an attribute type is timestamp.
     */
    public static final String ATTRIBUTE_TYPE_IS_TIMESTAMP = "AttributeTypeIsTimestamp";
    
    /**
     * Validates that an attribute type is time.
     */
    public static final String ATTRIBUTE_TYPE_IS_TIME = "AttributeTypeIsTime";
    
    /**
     * Validates that the default case expression type is date.
     */
    public static final String TYPE_OF_DEFAULT_CASE_IS_DATE = "TypeOfDefaultCaseIsDate";
    
    /**
     * Validates that the default case expression type is timestamp.
     */
    public static final String TYPE_OF_DEFAULT_CASE_IS_TIMESTAMP = "TypeOfDefaultCaseIsTimestamp";
    
    /**
     * Validates that the default case expression type is time.
     */
    public static final String TYPE_OF_DEFAULT_CASE_IS_TIME = "TypeOfDefaultCaseIsTime";
    
    /**
     * Validates that a switch case expression type is date.
     */
    public static final String TYPE_OF_SWITCH_CASE_IS_DATE = "TypeOfSwitchCaseIsDate";
    
    /**
     * Validates that a switch case expression type is timestamp.
     */
    public static final String TYPE_OF_SWITCH_CASE_IS_TIMESTAMP = "TypeOfSwitchCaseIsTimestamp";
    
    /**
     * Validates that a switch case expression type is time.
     */
    public static final String TYPE_OF_SWITCH_CASE_IS_TIME = "TypeOfSwitchCaseIsTime";

    // =========================================================================
    // Measured Constraints (measured.evl)
    // =========================================================================
    
    /**
     * Validates that a measure is valid.
     */
    public static final String MEASURE_IS_VALID = "MeasureIsValid";
    
    /**
     * Validates that measures in an addition operation are compatible.
     */
    public static final String MEASURE_OF_ADDITION_IS_VALID = "MeasureOfAdditionIsValid";
    
    /**
     * Validates that the measure of a complete expression is defined.
     */
    public static final String MEASURE_OF_COMPLETE_EXPRESSION_IS_DEFINED = 
            "MeasureOfCompleteExpressionIsDefined";
    
    /**
     * Validates that a comparison involves measured values.
     */
    public static final String COMPARISON_IS_MEASURED = "ComparisonIsMeasured";
    
    /**
     * Validates that measures are matching in a comparison.
     */
    public static final String MEASURES_ARE_MATCHING = "MeasuresAreMatching";

    // =========================================================================
    // Enumeration Constraints (enumeration.evl)
    // =========================================================================
    
    /**
     * Validates that an attribute type is enumeration.
     */
    public static final String ATTRIBUTE_TYPE_IS_ENUMERATION = "AttributeTypeIsEnumeration";
    
    /**
     * Validates that the default case expression type is enumeration.
     */
    public static final String TYPE_OF_DEFAULT_CASE_IS_ENUMERATION = "TypeOfDefaultCaseIsEnumeration";
    
    /**
     * Validates that a switch case expression type is enumeration.
     */
    public static final String TYPE_OF_SWITCH_CASE_IS_ENUMERATION = "TypeOfSwitchCaseIsEnumeration";

    // =========================================================================
    // Custom Type Constraints (custom.evl)
    // =========================================================================
    
    /**
     * Validates that an attribute type is custom.
     */
    public static final String ATTRIBUTE_TYPE_IS_CUSTOM = "AttributeTypeIsCustom";
    
    /**
     * Validates that the default case expression type is custom.
     */
    public static final String TYPE_OF_DEFAULT_CASE_IS_CUSTOM = "TypeOfDefaultCaseIsCustom";
    
    /**
     * Validates that a switch case expression type is custom.
     */
    public static final String TYPE_OF_SWITCH_CASE_IS_CUSTOM = "TypeOfSwitchCaseIsCustom";

    // =========================================================================
    // Binding Constraints (Binding.evl)
    // =========================================================================
    
    /**
     * Validates that an attribute binding is valid (attribute exists on type).
     */
    public static final String ATTRIBUTE_BINDING_IS_VALID = "AttributeBindingIsValid";
    
    /**
     * Validates that an attribute binding expression is valid.
     */
    public static final String ATTRIBUTE_BINDING_EXPRESSION_IS_VALID = "AttributeBindingExpressionIsValid";
    
    /**
     * Validates that an attribute binding type is valid.
     */
    public static final String ATTRIBUTE_BINDING_TYPE_IS_VALID = "AttributeBindingTypeIsValid";
    
    /**
     * Validates that a reference binding is valid (reference exists on type).
     */
    public static final String REFERENCE_BINDING_IS_VALID = "ReferenceBindingIsValid";
    
    /**
     * Validates that a reference binding type is valid.
     */
    public static final String REFERENCE_BINDING_TYPE_IS_VALID = "ReferenceBindingTypeIsValid";
    
    /**
     * Validates that a filter binding is valid.
     */
    public static final String FILTER_BINDING_IS_VALID = "FilterBindingIsValid";
    
    /**
     * Validates that a filter binding expression is valid.
     */
    public static final String FILTER_BINDING_EXPRESSION_IS_VALID = "FilterBindingExpressionIsValid";

    // =========================================================================
    // Attribute Binding Constraints (attributeBinding.evl)
    // =========================================================================
    
    /**
     * Validates that a numeric expression matches its binding.
     */
    public static final String NUMERIC_EXPRESSION_MATCHES_BINDING = "NumericExpressionMatchesBinding";
    
    /**
     * Validates that a boolean expression matches its binding.
     */
    public static final String BOOLEAN_EXPRESSION_MATCHES_BINDING = "BooleanExpressionMatchesBinding";
    
    /**
     * Validates that a string expression matches its binding.
     */
    public static final String STRING_EXPRESSION_MATCHES_BINDING = "StringExpressionMatchesBinding";
    
    /**
     * Validates that an enumeration expression matches its binding.
     */
    public static final String ENUMERATION_EXPRESSION_MATCHES_BINDING = 
            "EnumerationExpressionMatchesBinding";
    
    /**
     * Validates that a date expression matches its binding.
     */
    public static final String DATE_EXPRESSION_MATCHES_BINDING = "DateExpressionMatchesBinding";
    
    /**
     * Validates that a timestamp expression matches its binding.
     */
    public static final String TIMESTAMP_EXPRESSION_MATCHES_BINDING = "TimestampExpressionMatchesBinding";
    
    /**
     * Validates that a time expression matches its binding.
     */
    public static final String TIME_EXPRESSION_MATCHES_BINDING = "TimeExpressionMatchesBinding";
    
    /**
     * Validates that a custom expression matches its binding.
     */
    public static final String CUSTOM_EXPRESSION_MATCHES_BINDING = "CustomExpressionMatchesBinding";

    // =========================================================================
    // Reference Binding Constraints (referenceBinding.evl)
    // =========================================================================
    
    /**
     * Validates that a reference binding expression is valid.
     */
    public static final String REFERENCE_BINDING_EXPRESSION_IS_VALID = 
            "ReferenceBindingExpressionIsValid";
    
    /**
     * Validates that a reference expression matches its binding.
     */
    public static final String REFERENCE_EXPRESSION_MATCHES_BINDING = "ReferenceExpressionMatchesBinding";
    
    /**
     * Validates that an object expression matches its binding.
     */
    public static final String OBJECT_EXPRESSION_MATCHES_BINDING = "ObjectExpressionMatchesBinding";
    
    /**
     * Validates that a collection expression matches its binding.
     */
    public static final String COLLECTION_EXPRESSION_MATCHES_BINDING = "CollectionExpressionMatchesBinding";

    // =========================================================================
    // Filter Binding Constraints (filterBinding.evl)
    // =========================================================================
    
    /**
     * Validates that a logical expression matches its filter binding.
     */
    public static final String LOGICAL_EXPRESSION_MATCHES_BINDING = "LogicalExpressionMatchesBinding";
}
