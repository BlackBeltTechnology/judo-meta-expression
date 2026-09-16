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
import hu.blackbelt.judo.meta.expression.constant.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;

/**
 * Mock-based validation tests for constant expression constraints.
 * Tests run with Java (Zeta) validator only - EVL validation is skipped for mock models.
 */
public class ConstantExpressionValidationTest extends AbstractMockExpressionValidationTest {

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
     * Get expected errors based on validator type.
     * EVL uses generic "Resolved" constraint names across contexts,
     * while Zeta uses context-specific names like "IntegerConstantResolved".
     */
    private Collection<String> expectedErrors(String zetaName, String evlName) {
        return validatorType == ValidatorType.JAVA 
                ? ImmutableList.of(zetaName) 
                : ImmutableList.of(evlName);
    }

    // =========================================================================
    // IntegerConstant Tests
    // =========================================================================

    @ParameterizedTest(name = "testIntegerConstantWithValue [{0}]")
    @EnumSource(ValidatorType.class)
    void testIntegerConstantWithValue(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create integer constant with value
        IntegerConstant intConst = newIntegerConstantBuilder()
                .withValue(BigInteger.valueOf(42))
                .build();

        addToModel(intConst);

        // Should pass - value is defined
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testIntegerConstantWithNullValue [{0}]")
    @EnumSource(ValidatorType.class)
    void testIntegerConstantWithNullValue(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create integer constant without value
        IntegerConstant intConst = newIntegerConstantBuilder()
                .build();

        addToModel(intConst);

        // Should fail - value is not defined
        runValidation(
                expectedErrors("IntegerConstantResolved", "Resolved"),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // DecimalConstant Tests
    // =========================================================================

    @ParameterizedTest(name = "testDecimalConstantWithValue [{0}]")
    @EnumSource(ValidatorType.class)
    void testDecimalConstantWithValue(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create decimal constant with value
        DecimalConstant decConst = newDecimalConstantBuilder()
                .withValue(BigDecimal.valueOf(3.14))
                .build();

        addToModel(decConst);

        // Should pass - value is defined
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testDecimalConstantWithNullValue [{0}]")
    @EnumSource(ValidatorType.class)
    void testDecimalConstantWithNullValue(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create decimal constant without value
        DecimalConstant decConst = newDecimalConstantBuilder()
                .build();

        addToModel(decConst);

        // Should fail - value is not defined
        runValidation(
                expectedErrors("DecimalConstantResolved", "Resolved"),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // BooleanConstant Tests
    // =========================================================================

    @ParameterizedTest(name = "testBooleanConstantWithTrueValue [{0}]")
    @EnumSource(ValidatorType.class)
    void testBooleanConstantWithTrueValue(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create boolean constant with true
        BooleanConstant boolConst = newBooleanConstantBuilder()
                .withValue(true)
                .build();

        addToModel(boolConst);

        // Should pass - value is always defined for boolean
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testBooleanConstantWithFalseValue [{0}]")
    @EnumSource(ValidatorType.class)
    void testBooleanConstantWithFalseValue(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create boolean constant with false
        BooleanConstant boolConst = newBooleanConstantBuilder()
                .withValue(false)
                .build();

        addToModel(boolConst);

        // Should pass - value is always defined for boolean
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // StringConstant Tests
    // =========================================================================

    @ParameterizedTest(name = "testStringConstantWithValue [{0}]")
    @EnumSource(ValidatorType.class)
    void testStringConstantWithValue(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create string constant with value
        StringConstant strConst = newStringConstantBuilder()
                .withValue("hello world")
                .build();

        addToModel(strConst);

        // Should pass - value is defined
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testStringConstantWithEmptyValue [{0}]")
    @EnumSource(ValidatorType.class)
    void testStringConstantWithEmptyValue(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create string constant with empty value
        StringConstant strConst = newStringConstantBuilder()
                .withValue("")
                .build();

        addToModel(strConst);

        // Should pass - empty string is valid
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // Literal Tests
    // =========================================================================

    @ParameterizedTest(name = "testLiteralWithValue [{0}]")
    @EnumSource(ValidatorType.class)
    void testLiteralWithValue(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create literal with value
        Literal literal = newLiteralBuilder()
                .withValue("SOME_ENUM_VALUE")
                .build();

        addToModel(literal);

        // Should pass - value is defined
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testLiteralWithEmptyValue [{0}]")
    @EnumSource(ValidatorType.class)
    void testLiteralWithEmptyValue(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create literal with empty value
        Literal literal = newLiteralBuilder()
                .withValue("")
                .build();

        addToModel(literal);

        // Should fail - empty value is not valid for literal
        runValidation(
                expectedErrors("LiteralResolved", "Resolved"),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // Instance Tests
    // =========================================================================

    @ParameterizedTest(name = "testInstanceIsAlwaysResolved [{0}]")
    @EnumSource(ValidatorType.class)
    void testInstanceIsAlwaysResolved(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Person", MOCK_NAMESPACE)
                    .withAttribute("name", "String")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create TypeName - must be added to resource (elementName is non-containment)
        TypeName typeName = newTypeNameBuilder()
                .withNamespace(MOCK_NAMESPACE)
                .withName("Person")
                .build();
        addToModel(typeName);

        // Create instance with proper TypeName reference
        Instance instance = newInstanceBuilder()
                .withElementName(typeName)
                .build();

        addToModel(instance);

        // Should pass - instance is always resolved
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }
}
