package hu.blackbelt.judo.meta.expression.builder.jql.function.temporal;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.TypeNameExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import static hu.blackbelt.judo.meta.expression.variable.util.builder.VariableBuilders.newDateEnvironmentVariableBuilder;
import static hu.blackbelt.judo.meta.expression.variable.util.builder.VariableBuilders.newTimestampEnvironmentVariableBuilder;

public class NowFunctionTransformer<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> extends AbstractJqlFunctionTransformer<TypeNameExpression> {

    public static final String SYSTEM_CATEGORY = "SYSTEM";
    public static final String CURRENT_DATE_VARIABLE_NAME = "current_date";
    public static final String CURRENT_TIMESTAMP_VARIABLE_NAME = "current_timestamp";
    private JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers;

    public NowFunctionTransformer(JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers) {
        super(jqlTransformers);
        this.jqlTransformers = jqlTransformers;
    }


    @Override
    public Expression apply(TypeNameExpression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        if (functionCall.getParameters().size() != 0) {
            throw new IllegalArgumentException("No argument expected");
        }
        NE ne = jqlTransformers.getModelAdapter().get(argument).get();
        TypeName typeName = jqlTransformers.buildTypeName(argument.getNamespace(), argument.getName());
        if (jqlTransformers.getModelAdapter().isPrimitiveType(ne)) {
            P primitiveType = (P) ne;
            if (jqlTransformers.getModelAdapter().isDate(primitiveType)) {
                return newDateEnvironmentVariableBuilder()
                        .withCategory(SYSTEM_CATEGORY)
                        .withVariableName(CURRENT_DATE_VARIABLE_NAME)
                        .withTypeName(typeName)
                        .build();
            } else if (jqlTransformers.getModelAdapter().isTimestamp(primitiveType)) {
                return newTimestampEnvironmentVariableBuilder()
                        .withCategory(SYSTEM_CATEGORY)
                        .withVariableName(CURRENT_TIMESTAMP_VARIABLE_NAME)
                        .withTypeName(typeName)
                        .build();
            } else {
                throw new IllegalArgumentException("Function is not valid for given type");
            }
        } else {
            throw new IllegalArgumentException("Type is not valid for environment variable");
        }
    }
}
