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
import hu.blackbelt.judo.meta.expression.temporal.DateAttribute;
import hu.blackbelt.judo.meta.expression.temporal.TimestampAttribute;
import hu.blackbelt.judo.meta.expression.temporal.TimeAttribute;
import hu.blackbelt.judo.meta.expression.temporal.DateSwitchExpression;
import hu.blackbelt.judo.meta.expression.temporal.TimestampSwitchExpression;
import hu.blackbelt.judo.meta.expression.temporal.TimeSwitchExpression;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigInteger;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.*;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.*;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;

/**
 * Mock-based validation tests for temporal expression constraints.
 * Tests run with Java (Zeta) validator only - EVL validation is skipped for mock models.
 */
public class TemporalExpressionValidationTest extends AbstractMockExpressionValidationTest {

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
    // DateAttribute - AttributeTypeIsDate Tests
    // =========================================================================

    @ParameterizedTest(name = "testDateAttributeWithDateType [{0}]")
    @EnumSource(ValidatorType.class)
    void testDateAttributeWithDateType(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Create model with date attribute
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Event", MOCK_NAMESPACE)
                    .withAttribute("eventDate", "Date")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create date attribute expression for date field
        DateAttribute dateAttr = newDateAttributeBuilder()
                .withAttributeName("eventDate")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Event"))
                .build();

        addToModel(dateAttr);

        // Should pass - attribute type is date
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testDateAttributeWithNonDateType [{0}]")
    @EnumSource(ValidatorType.class)
    void testDateAttributeWithNonDateType(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Create model with string attribute (not date)
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Event", MOCK_NAMESPACE)
                    .withAttribute("name", "String")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create date attribute expression for string field - should fail
        DateAttribute dateAttr = newDateAttributeBuilder()
                .withAttributeName("name")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Event"))
                .build();

        addToModel(dateAttr);

        // Should fail with AttributeTypeIsDate error
        runValidation(
                ImmutableList.of(ValidationConstants.ATTRIBUTE_TYPE_IS_DATE),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // TimestampAttribute - AttributeTypeIsTimestamp Tests
    // =========================================================================

    @ParameterizedTest(name = "testTimestampAttributeWithTimestampType [{0}]")
    @EnumSource(ValidatorType.class)
    void testTimestampAttributeWithTimestampType(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Create model with timestamp attribute
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Event", MOCK_NAMESPACE)
                    .withAttribute("createdAt", "Timestamp")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create timestamp attribute expression for timestamp field
        TimestampAttribute timestampAttr = newTimestampAttributeBuilder()
                .withAttributeName("createdAt")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Event"))
                .build();

        addToModel(timestampAttr);

        // Should pass - attribute type is timestamp
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testTimestampAttributeWithNonTimestampType [{0}]")
    @EnumSource(ValidatorType.class)
    void testTimestampAttributeWithNonTimestampType(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Create model with integer attribute (not timestamp)
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Event", MOCK_NAMESPACE)
                    .withAttribute("count", "Integer")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create timestamp attribute expression for integer field - should fail
        TimestampAttribute timestampAttr = newTimestampAttributeBuilder()
                .withAttributeName("count")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Event"))
                .build();

        addToModel(timestampAttr);

        // Should fail with AttributeTypeIsTimestamp error
        runValidation(
                ImmutableList.of(ValidationConstants.ATTRIBUTE_TYPE_IS_TIMESTAMP),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // TimeAttribute - AttributeTypeIsTime Tests
    // =========================================================================

    @ParameterizedTest(name = "testTimeAttributeWithTimeType [{0}]")
    @EnumSource(ValidatorType.class)
    void testTimeAttributeWithTimeType(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Create model with time attribute
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Schedule", MOCK_NAMESPACE)
                    .withAttribute("startTime", "Time")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create time attribute expression for time field
        TimeAttribute timeAttr = newTimeAttributeBuilder()
                .withAttributeName("startTime")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Schedule"))
                .build();

        addToModel(timeAttr);

        // Should pass - attribute type is time
        runValidation(
                ImmutableList.of(),
                ImmutableList.of()
        );
    }

    @ParameterizedTest(name = "testTimeAttributeWithNonTimeType [{0}]")
    @EnumSource(ValidatorType.class)
    void testTimeAttributeWithNonTimeType(ValidatorType type) throws Exception {
        this.validatorType = type;

        // Create model with boolean attribute (not time)
        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .withEntityType("Schedule", MOCK_NAMESPACE)
                    .withAttribute("isActive", "Boolean")
                    .end()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create time attribute expression for boolean field - should fail
        TimeAttribute timeAttr = newTimeAttributeBuilder()
                .withAttributeName("isActive")
                .withObjectExpression(createObjectVariableReferenceWithInstance("Schedule"))
                .build();

        addToModel(timeAttr);

        // Should fail with AttributeTypeIsTime error
        runValidation(
                ImmutableList.of(ValidationConstants.ATTRIBUTE_TYPE_IS_TIME),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // DateSwitchExpression - TypeOfDefaultCaseIsDate Tests
    // =========================================================================

    @ParameterizedTest(name = "testDateSwitchExpressionWithNonDateDefault [{0}]")
    @EnumSource(ValidatorType.class)
    void testDateSwitchExpressionWithNonDateDefault(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create date switch expression with string default - should fail
        DateSwitchExpression switchExpr = newDateSwitchExpressionBuilder()
                .withDefaultExpression(newStringConstantBuilder()
                        .withValue("not a date")
                        .build())
                .build();

        addToModel(switchExpr);

        // Should fail with TypeOfDefaultCaseIsDate error
        runValidation(
                ImmutableList.of(ValidationConstants.TYPE_OF_DEFAULT_CASE_IS_DATE),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // TimestampSwitchExpression - TypeOfDefaultCaseIsTimestamp Tests
    // =========================================================================

    @ParameterizedTest(name = "testTimestampSwitchExpressionWithNonTimestampDefault [{0}]")
    @EnumSource(ValidatorType.class)
    void testTimestampSwitchExpressionWithNonTimestampDefault(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create timestamp switch expression with integer default - should fail
        TimestampSwitchExpression switchExpr = newTimestampSwitchExpressionBuilder()
                .withDefaultExpression(newIntegerConstantBuilder()
                        .withValue(BigInteger.valueOf(12345))
                        .build())
                .build();

        addToModel(switchExpr);

        // Should fail with TypeOfDefaultCaseIsTimestamp error
        runValidation(
                ImmutableList.of(ValidationConstants.TYPE_OF_DEFAULT_CASE_IS_TIMESTAMP),
                ImmutableList.of()
        );
    }

    // =========================================================================
    // TimeSwitchExpression - TypeOfDefaultCaseIsTime Tests
    // =========================================================================

    @ParameterizedTest(name = "testTimeSwitchExpressionWithNonTimeDefault [{0}]")
    @EnumSource(ValidatorType.class)
    void testTimeSwitchExpressionWithNonTimeDefault(ValidatorType type) throws Exception {
        this.validatorType = type;

        MockModelAdapter adapter = MockModelBuilder.create()
                .withStandardPrimitives()
                .build();

        initModel();
        setMockModelAdapter(adapter);

        // Create time switch expression with boolean default - should fail
        TimeSwitchExpression switchExpr = newTimeSwitchExpressionBuilder()
                .withDefaultExpression(newBooleanConstantBuilder()
                        .withValue(true)
                        .build())
                .build();

        addToModel(switchExpr);

        // Should fail with TypeOfDefaultCaseIsTime error
        runValidation(
                ImmutableList.of(ValidationConstants.TYPE_OF_DEFAULT_CASE_IS_TIME),
                ImmutableList.of()
        );
    }
}
