package hu.blackbelt.judo.meta.expression.builder.jql.function.variable;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.TypeNameExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.constant.StringConstant;
import hu.blackbelt.judo.meta.expression.variable.EnvironmentVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import static hu.blackbelt.judo.meta.expression.variable.util.builder.VariableBuilders.*;

public class GetVariableFunctionTransformer<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> extends AbstractJqlFunctionTransformer<TypeNameExpression> {

    private JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers;

    public GetVariableFunctionTransformer(JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers) {
        super(jqlTransformers);
        this.jqlTransformers = jqlTransformers;
    }

    @Override
    public Expression apply(TypeNameExpression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        NE ne = jqlTransformers.getModelAdapter().get(argument).get();
        if (functionCall.getParameters().size() != 2) {
            throw new IllegalArgumentException("Function requires 2 arguments");
        }
        String category = ((StringConstant) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context)).getValue();
        String variableName = ((StringConstant) jqlTransformers.transform(functionCall.getParameters().get(1).getExpression(), context)).getValue();
        TypeName typeName = jqlTransformers.buildTypeName(argument.getNamespace(), argument.getName());
        EnvironmentVariable result;
        if (jqlTransformers.getModelAdapter().isPrimitiveType(ne)) {
            P primitiveType = (P) ne;
            if (jqlTransformers.getModelAdapter().isInteger(primitiveType)) {
                if (jqlTransformers.getModelAdapter().isMeasuredType(primitiveType)) {
                    M measure = jqlTransformers.getModelAdapter().getMeasureOfType(primitiveType).get();
                    U unit = jqlTransformers.getModelAdapter().getUnitOfType(primitiveType).get();
                    MeasureName measureName = jqlTransformers.getModelAdapter().buildMeasureName(measure).get();
                    result = newMeasuredIntegerEnvironmentVariableBuilder()
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
