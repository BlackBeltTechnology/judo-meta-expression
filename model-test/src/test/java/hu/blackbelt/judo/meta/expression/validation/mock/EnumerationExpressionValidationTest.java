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
import hu.blackbelt.judo.meta.expression.enumeration.EnumerationAttribute;
import hu.blackbelt.judo.meta.expression.enumeration.EnumerationSwitchExpression;
import hu.blackbelt.judo.meta.expression.object.ObjectVariableReference;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigInteger;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.enumeration.util.builder.EnumerationBuilders.*;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.*;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;

/**
 * Mock-based validation tests for enumeration expression constraints.
 * Tests run with Java (Zeta) validator only - EVL validation is skipped for mock models.
 */
public class EnumerationExpressionValidationTest extends AbstractMockExpressionValidationTest {

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
                .withNamespace("mock")
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
    // EnumerationAttribute - AttributeTypeIsEnumeration Tests
    // =========================================================================

    private static final String MOCK_NAMESPACE = "mock";

    @ParameterizedTest(name = "testEnumerationAttributeWithEnumerationType [{0}]")
    @EnumSource(ValidatorType.class)
    void testEnumerationAttributeWithEnumerationType(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Create model with enumeration type and attribute
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEnumeration("Status", MOCK_NAMESPACE)
                    .withMember("ACTIVE")
                    .withMember("INACTIVE")
                    .withMember("PENDING")
                    .end()
                .withEntityType("Order", MOCK_NAMESPACE)
                    .withAttribute("status", MOCK_NAMESPACE + "::Status")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create enumeration attribute expression for enumeration field
        EnumerationAttribute enumAttr = newEnumerationAttributeBuilder()
                .withAttributeName("status")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Order"))
                .build();

        addToModel(enumAttr);

        // Should pass - attribute type is enumeration
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testEnumerationAttributeWithNonEnumerationType [{0}]")
    @EnumSource(ValidatorType.class)
    void testEnumerationAttributeWithNonEnumerationType(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Create model with string attribute (not enumeration)
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Order", MOCK_NAMESPACE)
                    .withAttribute("name", "String")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create enumeration attribute expression for string field - should fail
        EnumerationAttribute enumAttr = newEnumerationAttributeBuilder()
                .withAttributeName("name")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Order"))
                .build();

        addToModel(enumAttr);

        // Should fail with AttributeTypeIsEnumeration error
        runValidation(
                ImmutableList.of(ValidationConstants.ATTRIBUTE_TYPE_IS_ENUMERATION),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testEnumerationAttributeWithIntegerType [{0}]")
    @EnumSource(ValidatorType.class)
    void testEnumerationAttributeWithIntegerType(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Create model with integer attribute (not enumeration)
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Order", MOCK_NAMESPACE)
                    .withAttribute("quantity", "Integer")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create enumeration attribute expression for integer field - should fail
        EnumerationAttribute enumAttr = newEnumerationAttributeBuilder()
                .withAttributeName("quantity")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Order"))
                .build();

        addToModel(enumAttr);

        // Should fail with AttributeTypeIsEnumeration error
        runValidation(
                ImmutableList.of(ValidationConstants.ATTRIBUTE_TYPE_IS_ENUMERATION),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testEnumerationAttributeWithBooleanType [{0}]")
    @EnumSource(ValidatorType.class)
    void testEnumerationAttributeWithBooleanType(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Create model with boolean attribute (not enumeration)
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Order", MOCK_NAMESPACE)
                    .withAttribute("isActive", "Boolean")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create enumeration attribute expression for boolean field - should fail
        EnumerationAttribute enumAttr = newEnumerationAttributeBuilder()
                .withAttributeName("isActive")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Order"))
                .build();

        addToModel(enumAttr);

        // Should fail with AttributeTypeIsEnumeration error
        runValidation(
                ImmutableList.of(ValidationConstants.ATTRIBUTE_TYPE_IS_ENUMERATION),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // EnumerationSwitchExpression - TypeOfDefaultCaseIsEnumeration Tests
    // =========================================================================

    @ParameterizedTest(name = "testEnumerationSwitchExpressionWithNonEnumerationDefault [{0}]")
    @EnumSource(ValidatorType.class)
    void testEnumerationSwitchExpressionWithNonEnumerationDefault(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create enumeration switch expression with string default - should fail
        EnumerationSwitchExpression switchExpr = newEnumerationSwitchExpressionBuilder()
                .withDefaultExpression(newStringConstantBuilder()
                        .withValue("not an enumeration")
                        .build())
                .build();

        addToModel(switchExpr);

        // Should fail with TypeOfDefaultCaseIsEnumeration error
        runValidation(
                ImmutableList.of(ValidationConstants.TYPE_OF_DEFAULT_CASE_IS_ENUMERATION),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testEnumerationSwitchExpressionWithIntegerDefault [{0}]")
    @EnumSource(ValidatorType.class)
    void testEnumerationSwitchExpressionWithIntegerDefault(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create enumeration switch expression with integer default - should fail
        EnumerationSwitchExpression switchExpr = newEnumerationSwitchExpressionBuilder()
                .withDefaultExpression(newIntegerConstantBuilder()
                        .withValue(BigInteger.valueOf(42))
                        .build())
                .build();

        addToModel(switchExpr);

        // Should fail with TypeOfDefaultCaseIsEnumeration error
        runValidation(
                ImmutableList.of(ValidationConstants.TYPE_OF_DEFAULT_CASE_IS_ENUMERATION),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testEnumerationSwitchExpressionWithBooleanDefault [{0}]")
    @EnumSource(ValidatorType.class)
    void testEnumerationSwitchExpressionWithBooleanDefault(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create enumeration switch expression with boolean default - should fail
        EnumerationSwitchExpression switchExpr = newEnumerationSwitchExpressionBuilder()
                .withDefaultExpression(newBooleanConstantBuilder()
                        .withValue(true)
                        .build())
                .build();

        addToModel(switchExpr);

        // Should fail with TypeOfDefaultCaseIsEnumeration error
        runValidation(
                ImmutableList.of(ValidationConstants.TYPE_OF_DEFAULT_CASE_IS_ENUMERATION),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // Multiple Enumeration Types Tests
    // =========================================================================

    @ParameterizedTest(name = "testMultipleEnumerationTypesInModel [{0}]")
    @EnumSource(ValidatorType.class)
    void testMultipleEnumerationTypesInModel(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Create model with multiple enumeration types
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEnumeration("Status", MOCK_NAMESPACE)
                    .withMember("ACTIVE")
                    .withMember("INACTIVE")
                    .end()
                .withEnumeration("Priority", MOCK_NAMESPACE)
                    .withMember("LOW")
                    .withMember("MEDIUM")
                    .withMember("HIGH")
                    .end()
                .withEntityType("Task", MOCK_NAMESPACE)
                    .withAttribute("status", MOCK_NAMESPACE + "::Status")
                    .withAttribute("priority", MOCK_NAMESPACE + "::Priority")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create enumeration attribute expressions for both fields
        EnumerationAttribute statusAttr = newEnumerationAttributeBuilder()
                .withAttributeName("status")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Task"))
                .build();

        EnumerationAttribute priorityAttr = newEnumerationAttributeBuilder()
                .withAttributeName("priority")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Task"))
                .build();

        addToModel(statusAttr, priorityAttr);

        // Should pass - both attributes are enumerations
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }
}
