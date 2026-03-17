package hu.blackbelt.judo.meta.expression.validation.mock;

/*-
 * #%L
 * Judo :: Expression :: Model :: Test
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

import com.google.common.collect.ImmutableList;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.ValidatorType;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.numeric.DecimalArithmeticExpression;
import hu.blackbelt.judo.meta.expression.numeric.DecimalAttribute;
import hu.blackbelt.judo.meta.expression.numeric.DecimalSwitchExpression;
import hu.blackbelt.judo.meta.expression.numeric.IntegerArithmeticExpression;
import hu.blackbelt.judo.meta.expression.numeric.IntegerAttribute;
import hu.blackbelt.judo.meta.expression.numeric.IntegerSwitchExpression;
import hu.blackbelt.judo.meta.expression.object.ObjectVariableReference;
import hu.blackbelt.judo.meta.expression.operator.DecimalOperator;
import hu.blackbelt.judo.meta.expression.operator.IntegerOperator;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.math.BigInteger;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.*;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.*;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.*;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.*;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;

/**
 * Validation tests for numeric expressions (IntegerAttribute, DecimalAttribute, 
 * arithmetic expressions, switch expressions).
 */
public class NumericExpressionValidationTest extends AbstractMockExpressionValidationTest {

    private static final String MOCK_NAMESPACE = "mock";

    private void addToModel(Object... elements) {
        Resource resource = expressionModel.getResource();
        for (Object element : elements) {
            if (element instanceof org.eclipse.emf.ecore.EObject) {
                resource.getContents().add((org.eclipse.emf.ecore.EObject) element);
            }
        }
    }

    /**
     * Create an ObjectVariableReference with a properly contained Instance.
     * Both TypeName and Instance must be added to the resource separately 
     * because both 'elementName' and 'variable' references are non-containment in EMF.
     */
    private ObjectVariableReference createObjectVariableReferenceWithInstance(String typeName) {
        // Create TypeName - must be added to resource (elementName is non-containment)
        TypeName type = newTypeNameBuilder()
                .withNamespace(MOCK_NAMESPACE)
                .withName(typeName)
                .build();
        addToModel(type);
        
        // Create Instance with the TypeName
        Instance instance = newInstanceBuilder()
                .withElementName(type)
                .build();
        // Add Instance to the resource (variable ref is non-containment)
        addToModel(instance);
        
        // Create ObjectVariableReference pointing to the Instance
        return newObjectVariableReferenceBuilder()
                .withVariable(instance)
                .build();
    }

    // =========================================================================
    // IntegerAttribute Tests
    // =========================================================================

    @ParameterizedTest(name = "testIntegerAttributeValid [{0}]")
    @EnumSource(ValidatorType.class)
    void testIntegerAttributeValid(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Setup mock model with Integer attribute
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Person", MOCK_NAMESPACE)
                    .withAttribute("age", "Integer")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create IntegerAttribute expression
        IntegerAttribute ageAttr = newIntegerAttributeBuilder()
                .withAttributeName("age")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Person"))
                .build();

        addToModel(ageAttr);

        // Run validation - should pass with no errors
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testIntegerAttributeWithNonNumericType [{0}]")
    @EnumSource(ValidatorType.class)
    void testIntegerAttributeWithNonNumericType(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Setup mock model with String attribute (not numeric)
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Person", MOCK_NAMESPACE)
                    .withAttribute("name", "String")  // String, not Integer
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create IntegerAttribute pointing to String attribute - should fail
        IntegerAttribute nameAsInt = newIntegerAttributeBuilder()
                .withAttributeName("name")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Person"))
                .build();

        addToModel(nameAsInt);

        // Run validation - should fail with AttributeTypeIsInteger error
        runValidation(
                ImmutableList.of(ValidationConstants.ATTRIBUTE_TYPE_IS_INTEGER),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // DecimalAttribute Tests
    // =========================================================================

    @ParameterizedTest(name = "testDecimalAttributeValid [{0}]")
    @EnumSource(ValidatorType.class)
    void testDecimalAttributeValid(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Setup mock model with Decimal attribute
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Product", MOCK_NAMESPACE)
                    .withAttribute("price", "Decimal")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        DecimalAttribute priceAttr = newDecimalAttributeBuilder()
                .withAttributeName("price")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Product"))
                .build();

        addToModel(priceAttr);

        // Run validation - should pass with no errors
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testDecimalAttributeWithNonNumericType [{0}]")
    @EnumSource(ValidatorType.class)
    void testDecimalAttributeWithNonNumericType(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Setup mock model with Boolean attribute (not numeric)
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Product", MOCK_NAMESPACE)
                    .withAttribute("active", "Boolean")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create DecimalAttribute pointing to Boolean attribute - should fail
        DecimalAttribute activeAsDecimal = newDecimalAttributeBuilder()
                .withAttributeName("active")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Product"))
                .build();

        addToModel(activeAsDecimal);

        // Run validation - should fail with AttributeTypeIsDecimal error
        runValidation(
                ImmutableList.of(ValidationConstants.ATTRIBUTE_TYPE_IS_DECIMAL),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // Arithmetic Expression Tests
    // =========================================================================

    @ParameterizedTest(name = "testIntegerArithmeticExpressionValid [{0}]")
    @EnumSource(ValidatorType.class)
    void testIntegerArithmeticExpressionValid(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create simple integer arithmetic: 2 + 3
        IntegerArithmeticExpression expr = newIntegerArithmeticExpressionBuilder()
                .withOperator(IntegerOperator.ADD)
                .withLeft(newIntegerConstantBuilder()
                        .withValue(BigInteger.valueOf(2))
                        .build())
                .withRight(newIntegerConstantBuilder()
                        .withValue(BigInteger.valueOf(3))
                        .build())
                .build();

        addToModel(expr);

        // Run validation - should pass
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testDecimalArithmeticWithIntegerOperandsWarning [{0}]")
    @EnumSource(ValidatorType.class)
    void testDecimalArithmeticWithIntegerOperandsWarning(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create decimal arithmetic with integer operands: 2 + 3 (using DecimalArithmeticExpression)
        // This should trigger IntegerArithmeticExpressionIsRecommended critique
        DecimalArithmeticExpression expr = newDecimalArithmeticExpressionBuilder()
                .withOperator(DecimalOperator.ADD)
                .withLeft(newIntegerConstantBuilder()
                        .withValue(BigInteger.valueOf(2))
                        .build())
                .withRight(newIntegerConstantBuilder()
                        .withValue(BigInteger.valueOf(3))
                        .build())
                .build();

        addToModel(expr);

        // Run validation - should pass with warning about using integer arithmetic
        runValidation(
                ImmutableList.of(),
                ImmutableList.of(ValidationConstants.INTEGER_ARITHMETIC_EXPRESSION_IS_RECOMMENDED)
        );
    }

    @ParameterizedTest(name = "testDecimalArithmeticWithDecimalOperandsNoWarning [{0}]")
    @EnumSource(ValidatorType.class)
    void testDecimalArithmeticWithDecimalOperandsNoWarning(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create decimal arithmetic with decimal operands: 2.5 + 3.5
        DecimalArithmeticExpression expr = newDecimalArithmeticExpressionBuilder()
                .withOperator(DecimalOperator.ADD)
                .withLeft(newDecimalConstantBuilder()
                        .withValue(BigDecimal.valueOf(2.5))
                        .build())
                .withRight(newDecimalConstantBuilder()
                        .withValue(BigDecimal.valueOf(3.5))
                        .build())
                .build();

        addToModel(expr);

        // Run validation - should pass with no warnings
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // Switch Expression Tests
    // =========================================================================

    @ParameterizedTest(name = "testDecimalSwitchExpressionValid [{0}]")
    @EnumSource(ValidatorType.class)
    void testDecimalSwitchExpressionValid(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create switch expression with numeric default and cases
        DecimalSwitchExpression switchExpr = newDecimalSwitchExpressionBuilder()
                .withDefaultExpression(newDecimalConstantBuilder()
                        .withValue(BigDecimal.valueOf(0))
                        .build())
                .withCases(newSwitchCaseBuilder()
                        .withCondition(newBooleanConstantBuilder()
                                .withValue(true)
                                .build())
                        .withExpression(newDecimalConstantBuilder()
                                .withValue(BigDecimal.valueOf(100))
                                .build())
                        .build())
                .build();

        addToModel(switchExpr);

        // Run validation - should pass
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testDecimalSwitchExpressionWithNonNumericDefault [{0}]")
    @EnumSource(ValidatorType.class)
    void testDecimalSwitchExpressionWithNonNumericDefault(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create switch expression with STRING default - should fail
        DecimalSwitchExpression switchExpr = newDecimalSwitchExpressionBuilder()
                .withDefaultExpression(newStringConstantBuilder()
                        .withValue("not a number")
                        .build())
                .withCases(newSwitchCaseBuilder()
                        .withCondition(newBooleanConstantBuilder()
                                .withValue(true)
                                .build())
                        .withExpression(newDecimalConstantBuilder()
                                .withValue(BigDecimal.valueOf(100))
                                .build())
                        .build())
                .build();

        addToModel(switchExpr);

        // Run validation - should fail with TypeOfDefaultCaseIsNumeric error
        runValidation(
                ImmutableList.of(ValidationConstants.TYPE_OF_DEFAULT_CASE_IS_NUMERIC),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testSwitchCaseWithNonNumericExpression [{0}]")
    @EnumSource(ValidatorType.class)
    void testSwitchCaseWithNonNumericExpression(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create switch expression with non-numeric case expression - should fail
        DecimalSwitchExpression switchExpr = newDecimalSwitchExpressionBuilder()
                .withDefaultExpression(newDecimalConstantBuilder()
                        .withValue(BigDecimal.valueOf(0))
                        .build())
                .withCases(newSwitchCaseBuilder()
                        .withCondition(newBooleanConstantBuilder()
                                .withValue(true)
                                .build())
                        .withExpression(newStringConstantBuilder()  // String instead of numeric
                                .withValue("invalid")
                                .build())
                        .build())
                .build();

        addToModel(switchExpr);

        // Run validation - should fail with TypeOfSwitchCaseIsNumeric error
        runValidation(
                ImmutableList.of(ValidationConstants.TYPE_OF_SWITCH_CASE_IS_NUMERIC),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testIntegerSwitchExpressionValid [{0}]")
    @EnumSource(ValidatorType.class)
    void testIntegerSwitchExpressionValid(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create integer switch expression with valid numeric default and cases
        IntegerSwitchExpression switchExpr = newIntegerSwitchExpressionBuilder()
                .withDefaultExpression(newIntegerConstantBuilder()
                        .withValue(BigInteger.valueOf(0))
                        .build())
                .withCases(newSwitchCaseBuilder()
                        .withCondition(newBooleanConstantBuilder()
                                .withValue(true)
                                .build())
                        .withExpression(newIntegerConstantBuilder()
                                .withValue(BigInteger.valueOf(42))
                                .build())
                        .build())
                .build();

        addToModel(switchExpr);

        // Run validation - should pass
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }
}
