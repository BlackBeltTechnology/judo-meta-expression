package hu.blackbelt.judo.meta.expression.builder.jql.function.variable;

/*-
 * #%L
 * JUDO :: Expression :: Model
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
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.constant.StringConstant;
import hu.blackbelt.judo.meta.expression.variable.EnvironmentVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import static hu.blackbelt.judo.meta.expression.variable.util.builder.VariableBuilders.*;

public class GetVariableFunctionTransformer<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> extends AbstractJqlFunctionTransformer<TypeNameExpression> {

    private final JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers;
    private final boolean forceConstantVariable;

    public GetVariableFunctionTransformer(JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers, boolean forceConstantVariable) {
        super(jqlTransformers);
        this.jqlTransformers = jqlTransformers;
        this.forceConstantVariable = forceConstantVariable;
    }

    @Override
    public Expression apply(TypeNameExpression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        NE ne = jqlTransformers.getModelAdapter().get(argument).get();
        if (functionCall.getParameters().size() != 2) {
            throw new IllegalArgumentException("Function requires 2 arguments");
        }
        String category = ((StringConstant) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context)).getValue();
        StringExpression variableName = ((StringExpression) jqlTransformers.transform(functionCall.getParameters().get(1).getExpression(), context));
        if (forceConstantVariable && !(variableName instanceof StringConstant)) {
            throw new IllegalArgumentException("Variable name must be constant");
        }
        TypeName typeName = jqlTransformers.buildTypeName(argument.getNamespace(), argument.getName());
        EnvironmentVariable result;
        if (jqlTransformers.getModelAdapter().isPrimitiveType(ne)) {
            P primitiveType = (P) ne;
            if (jqlTransformers.getModelAdapter().isInteger(primitiveType)) {
                if (jqlTransformers.getModelAdapter().isMeasuredType(primitiveType)) {
                    M measure = jqlTransformers.getModelAdapter().getMeasureOfType(primitiveType).get();
                    U unit = jqlTransformers.getModelAdapter().getUnitOfType(primitiveType).get();
                    MeasureName measureName = jqlTransformers.getModelAdapter().buildMeasureName(measure).get();
                    result = newMeasuredDecimalEnvironmentVariableBuilder()
                            .withCategory(category)
                            .withVariableName(variableName)
                            .withTypeName(typeName)
                            .withMeasure(measureName)
                            .withUnitName(jqlTransformers.getModelAdapter().getUnitName(unit))
                            .build();
                } else {
                    result = newIntegerEnvironmentVariableBuilder()
                            .withCategory(category)
                            .withVariableName(variableName)
                            .withTypeName(typeName)
                            .build();
                }
            } else if (jqlTransformers.getModelAdapter().isDecimal(primitiveType)) {
                if (jqlTransformers.getModelAdapter().isMeasuredType(primitiveType)) {
                    M measure = jqlTransformers.getModelAdapter().getMeasureOfType(primitiveType).get();
                    U unit = jqlTransformers.getModelAdapter().getUnitOfType(primitiveType).get();
                    MeasureName measureName = jqlTransformers.getModelAdapter().buildMeasureName(measure).get();
                    result = newMeasuredDecimalEnvironmentVariableBuilder()
                            .withCategory(category)
                            .withVariableName(variableName)
                            .withTypeName(typeName)
                            .withMeasure(measureName)
                            .withUnitName(jqlTransformers.getModelAdapter().getUnitName(unit))
                            .build();
                } else {
                    result = newDecimalEnvironmentVariableBuilder()
                            .withCategory(category)
                            .withVariableName(variableName)
                            .withTypeName(typeName)
                            .build();
                }
            } else if (jqlTransformers.getModelAdapter().isBoolean(primitiveType)) {
                result = newBooleanEnvironmentVariableBuilder()
                        .withCategory(category)
                        .withVariableName(variableName)
                        .withTypeName(typeName)
                        .build();
            } else if (jqlTransformers.getModelAdapter().isString(primitiveType)) {
                result = newStringEnvironmentVariableBuilder()
                        .withCategory(category)
                        .withVariableName(variableName)
                        .withTypeName(typeName)
                        .build();
            } else if (jqlTransformers.getModelAdapter().isEnumeration(primitiveType)) {
                result = newLiteralEnvironmentVariableBuilder()
                        .withCategory(category)
                        .withVariableName(variableName)
                        .withTypeName(typeName)
                        .build();
            } else if (jqlTransformers.getModelAdapter().isCustom(primitiveType)) {
                result = newCustomEnvironmentVariableBuilder()
                        .withCategory(category)
                        .withVariableName(variableName)
                        .withTypeName(typeName)
                        .build();
            } else if (jqlTransformers.getModelAdapter().isDate(primitiveType)) {
                result = newDateEnvironmentVariableBuilder()
                        .withCategory(category)
                        .withVariableName(variableName)
                        .withTypeName(typeName)
                        .build();
            } else if (jqlTransformers.getModelAdapter().isTimestamp(primitiveType)) {
                result = newTimestampEnvironmentVariableBuilder()
                        .withCategory(category)
                        .withVariableName(variableName)
                        .withTypeName(typeName)
                        .build();
            } else if (jqlTransformers.getModelAdapter().isTime(primitiveType)) {
                result = newTimeEnvironmentVariableBuilder()
                        .withCategory(category)
                        .withVariableName(variableName)
                        .withTypeName(typeName)
                        .build();
            }
            else {
                throw new IllegalArgumentException("Unknown type for environment variable");
            }
        } else {
            throw new IllegalArgumentException("Type is not valid for environment variable");
        }
        return result;
    }

}
