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
import hu.blackbelt.judo.meta.expression.logical.DecimalComparison;
import hu.blackbelt.judo.meta.expression.logical.IntegerComparison;
import hu.blackbelt.judo.meta.expression.logical.LogicalAttribute;
import hu.blackbelt.judo.meta.expression.object.ObjectVariableReference;
import hu.blackbelt.judo.meta.expression.operator.LogicalOperator;
import hu.blackbelt.judo.meta.expression.operator.NumericComparator;
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
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;

/**
 * Mock-based validation tests for logical expression constraints.
 * Tests run with Java (Zeta) validator only - EVL validation is skipped for mock models.
 */
public class LogicalExpressionValidationTest extends AbstractMockExpressionValidationTest {

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
    // LogicalAttribute - AttributeTypeIsBoolean Tests
    // =========================================================================

    @ParameterizedTest(name = "testLogicalAttributeWithBooleanType [{0}]")
    @EnumSource(ValidatorType.class)
    void testLogicalAttributeWithBooleanType(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Create model with boolean attribute
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Person", MOCK_NAMESPACE)
                    .withAttribute("isActive", "Boolean")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create logical attribute expression for boolean field
        LogicalAttribute logicalAttr = newLogicalAttributeBuilder()
                .withAttributeName("isActive")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Person"))
                .build();

        addToModel(logicalAttr);

        // Should pass - attribute type is boolean
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testLogicalAttributeWithNonBooleanType [{0}]")
    @EnumSource(ValidatorType.class)
    void testLogicalAttributeWithNonBooleanType(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Create model with string attribute (not boolean)
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Person", MOCK_NAMESPACE)
                    .withAttribute("name", "String")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create logical attribute expression for string field - should fail
        LogicalAttribute logicalAttr = newLogicalAttributeBuilder()
                .withAttributeName("name")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Person"))
                .build();

        addToModel(logicalAttr);

        // Should fail with AttributeTypeIsBoolean error
        runValidation(
                ImmutableList.of(ValidationConstants.ATTRIBUTE_TYPE_IS_BOOLEAN),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // IntegerComparison Tests
    // =========================================================================

    @ParameterizedTest(name = "testIntegerComparisonValid [{0}]")
    @EnumSource(ValidatorType.class)
    void testIntegerComparisonValid(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create valid integer comparison
        IntegerComparison comparison = newIntegerComparisonBuilder()
                .withOperator(NumericComparator.GREATER_THAN)
                .withLeft(newIntegerConstantBuilder()
                        .withValue(BigInteger.valueOf(10))
                        .build())
                .withRight(newIntegerConstantBuilder()
                        .withValue(BigInteger.valueOf(5))
                        .build())
                .build();

        addToModel(comparison);

        // Should pass
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // DecimalComparison Tests
    // =========================================================================

    @ParameterizedTest(name = "testDecimalComparisonValid [{0}]")
    @EnumSource(ValidatorType.class)
    void testDecimalComparisonValid(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create valid decimal comparison with decimal operands
        DecimalComparison comparison = newDecimalComparisonBuilder()
                .withOperator(NumericComparator.LESS_THAN)
                .withLeft(newDecimalConstantBuilder()
                        .withValue(BigDecimal.valueOf(10.5))
                        .build())
                .withRight(newDecimalConstantBuilder()
                        .withValue(BigDecimal.valueOf(20.5))
                        .build())
                .build();

        addToModel(comparison);

        // Should pass without warnings
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testDecimalComparisonWithIntegerOperands [{0}]")
    @EnumSource(ValidatorType.class)
    void testDecimalComparisonWithIntegerOperands(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create decimal comparison with integer operands - should produce warning
        DecimalComparison comparison = newDecimalComparisonBuilder()
                .withOperator(NumericComparator.LESS_THAN)
                .withLeft(newIntegerConstantBuilder()
                        .withValue(BigInteger.valueOf(10))
                        .build())
                .withRight(newIntegerConstantBuilder()
                        .withValue(BigInteger.valueOf(20))
                        .build())
                .build();

        addToModel(comparison);

        // Should pass but with IntegerComparisonIsRecommended warning
        runValidation(
                ImmutableList.of(),
                ImmutableList.of(ValidationConstants.INTEGER_COMPARISON_IS_RECOMMENDED)
        );
    }

    // =========================================================================
    // BooleanConstant Tests
    // =========================================================================

    @ParameterizedTest(name = "testBooleanConstantValid [{0}]")
    @EnumSource(ValidatorType.class)
    void testBooleanConstantValid(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create boolean constant
        var boolConst = newBooleanConstantBuilder()
                .withValue(true)
                .build();

        addToModel(boolConst);

        // Should pass
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // NegationExpression Tests
    // =========================================================================

    @ParameterizedTest(name = "testNegationExpressionValid [{0}]")
    @EnumSource(ValidatorType.class)
    void testNegationExpressionValid(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create negation expression
        var negation = newNegationExpressionBuilder()
                .withExpression(newBooleanConstantBuilder()
                        .withValue(false)
                        .build())
                .build();

        addToModel(negation);

        // Should pass
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // KleeneExpression Tests (AND/OR)
    // =========================================================================

    @ParameterizedTest(name = "testAndExpressionValid [{0}]")
    @EnumSource(ValidatorType.class)
    void testAndExpressionValid(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create AND expression (Kleene expression)
        var andExpr = newKleeneExpressionBuilder()
                .withOperator(LogicalOperator.AND)
                .withLeft(newBooleanConstantBuilder().withValue(true).build())
                .withRight(newBooleanConstantBuilder().withValue(false).build())
                .build();

        addToModel(andExpr);

        // Should pass
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // UndefinedComparison Tests
    // =========================================================================

    @ParameterizedTest(name = "testUndefinedComparisonValid [{0}]")
    @EnumSource(ValidatorType.class)
    void testUndefinedComparisonValid(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create undefined comparison
        var undefinedComp = newUndefinedComparisonBuilder()
                .withExpression(newStringConstantBuilder()
                        .withValue("test")
                        .build())
                .build();

        addToModel(undefinedComp);

        // Should pass
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // StringComparison Tests
    // =========================================================================

    @ParameterizedTest(name = "testStringComparisonValid [{0}]")
    @EnumSource(ValidatorType.class)
    void testStringComparisonValid(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create string comparison
        var stringComp = newStringComparisonBuilder()
                .withLeft(newStringConstantBuilder()
                        .withValue("hello")
                        .build())
                .withRight(newStringConstantBuilder()
                        .withValue("world")
                        .build())
                .build();

        addToModel(stringComp);

        // Should pass
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }
}
