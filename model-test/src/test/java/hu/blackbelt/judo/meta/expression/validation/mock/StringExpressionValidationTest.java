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
import hu.blackbelt.judo.meta.expression.object.ObjectVariableReference;
import hu.blackbelt.judo.meta.expression.string.StringAttribute;
import hu.blackbelt.judo.meta.expression.string.StringSwitchExpression;
import hu.blackbelt.judo.meta.expression.string.Concatenate;
import hu.blackbelt.judo.meta.expression.string.UpperCase;
import hu.blackbelt.judo.meta.expression.string.LowerCase;
import hu.blackbelt.judo.meta.expression.string.Trim;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigInteger;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.*;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.*;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;

/**
 * Mock-based validation tests for string expression constraints.
 * Tests run with Java (Zeta) validator only - EVL validation is skipped for mock models.
 */
public class StringExpressionValidationTest extends AbstractMockExpressionValidationTest {

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
    // StringAttribute - AttributeTypeIsString Tests
    // =========================================================================

    @ParameterizedTest(name = "testStringAttributeWithStringType [{0}]")
    @EnumSource(ValidatorType.class)
    void testStringAttributeWithStringType(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Create model with string attribute
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Person", MOCK_NAMESPACE)
                    .withAttribute("name", "String")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create string attribute expression for string field
        StringAttribute stringAttr = newStringAttributeBuilder()
                .withAttributeName("name")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Person"))
                .build();

        addToModel(stringAttr);

        // Should pass - attribute type is string
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testStringAttributeWithNonStringType [{0}]")
    @EnumSource(ValidatorType.class)
    void testStringAttributeWithNonStringType(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Create model with integer attribute (not string)
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Person", MOCK_NAMESPACE)
                    .withAttribute("age", "Integer")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create string attribute expression for integer field - should fail
        StringAttribute stringAttr = newStringAttributeBuilder()
                .withAttributeName("age")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Person"))
                .build();

        addToModel(stringAttr);

        // Should fail with AttributeTypeIsString error
        runValidation(
                ImmutableList.of(ValidationConstants.ATTRIBUTE_TYPE_IS_STRING),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testStringAttributeWithBooleanType [{0}]")
    @EnumSource(ValidatorType.class)
    void testStringAttributeWithBooleanType(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Create model with boolean attribute (not string)
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Person", MOCK_NAMESPACE)
                    .withAttribute("isActive", "Boolean")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create string attribute expression for boolean field - should fail
        StringAttribute stringAttr = newStringAttributeBuilder()
                .withAttributeName("isActive")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Person"))
                .build();

        addToModel(stringAttr);

        // Should fail with AttributeTypeIsString error
        runValidation(
                ImmutableList.of(ValidationConstants.ATTRIBUTE_TYPE_IS_STRING),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // StringSwitchExpression - TypeOfDefaultCaseIsString Tests
    // =========================================================================

    @ParameterizedTest(name = "testStringSwitchExpressionWithStringDefault [{0}]")
    @EnumSource(ValidatorType.class)
    void testStringSwitchExpressionWithStringDefault(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create string switch expression with string default
        StringSwitchExpression switchExpr = newStringSwitchExpressionBuilder()
                .withDefaultExpression(newStringConstantBuilder()
                        .withValue("default value")
                        .build())
                .build();

        addToModel(switchExpr);

        // Should pass - default expression is StringExpression
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testStringSwitchExpressionWithNonStringDefault [{0}]")
    @EnumSource(ValidatorType.class)
    void testStringSwitchExpressionWithNonStringDefault(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create string switch expression with integer default - should fail
        StringSwitchExpression switchExpr = newStringSwitchExpressionBuilder()
                .withDefaultExpression(newIntegerConstantBuilder()
                        .withValue(BigInteger.valueOf(42))
                        .build())
                .build();

        addToModel(switchExpr);

        // Should fail with TypeOfDefaultCaseIsString error
        runValidation(
                ImmutableList.of(ValidationConstants.TYPE_OF_DEFAULT_CASE_IS_STRING),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // Concatenate Tests
    // =========================================================================

    @ParameterizedTest(name = "testConcatenateValid [{0}]")
    @EnumSource(ValidatorType.class)
    void testConcatenateValid(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create concatenate expression
        Concatenate concatenate = newConcatenateBuilder()
                .withLeft(newStringConstantBuilder()
                        .withValue("Hello, ")
                        .build())
                .withRight(newStringConstantBuilder()
                        .withValue("World!")
                        .build())
                .build();

        addToModel(concatenate);

        // Should pass
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // UpperCase Tests
    // =========================================================================

    @ParameterizedTest(name = "testUpperCaseValid [{0}]")
    @EnumSource(ValidatorType.class)
    void testUpperCaseValid(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create upper case expression
        UpperCase upperCase = newUpperCaseBuilder()
                .withExpression(newStringConstantBuilder()
                        .withValue("hello")
                        .build())
                .build();

        addToModel(upperCase);

        // Should pass
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // LowerCase Tests
    // =========================================================================

    @ParameterizedTest(name = "testLowerCaseValid [{0}]")
    @EnumSource(ValidatorType.class)
    void testLowerCaseValid(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create lower case expression
        LowerCase lowerCase = newLowerCaseBuilder()
                .withExpression(newStringConstantBuilder()
                        .withValue("HELLO")
                        .build())
                .build();

        addToModel(lowerCase);

        // Should pass
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // Trim Tests
    // =========================================================================

    @ParameterizedTest(name = "testTrimValid [{0}]")
    @EnumSource(ValidatorType.class)
    void testTrimValid(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create trim expression
        Trim trim = newTrimBuilder()
                .withExpression(newStringConstantBuilder()
                        .withValue("  hello  ")
                        .build())
                .build();

        addToModel(trim);

        // Should pass
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // StringConstant Tests
    // =========================================================================

    @ParameterizedTest(name = "testStringConstantValid [{0}]")
    @EnumSource(ValidatorType.class)
    void testStringConstantValid(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create string constant
        var stringConst = newStringConstantBuilder()
                .withValue("test string")
                .build();

        addToModel(stringConst);

        // Should pass
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // AsString Tests
    // =========================================================================

    @ParameterizedTest(name = "testAsStringValid [{0}]")
    @EnumSource(ValidatorType.class)
    void testAsStringValid(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create AsString expression to convert integer to string
        var asString = newAsStringBuilder()
                .withExpression(newIntegerConstantBuilder()
                        .withValue(BigInteger.valueOf(42))
                        .build())
                .build();

        addToModel(asString);

        // Should pass
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }
}
