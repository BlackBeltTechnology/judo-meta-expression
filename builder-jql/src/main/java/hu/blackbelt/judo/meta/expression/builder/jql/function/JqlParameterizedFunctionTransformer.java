package hu.blackbelt.judo.meta.expression.builder.jql.function;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class JqlParameterizedFunctionTransformer<B extends Expression, P, R extends Expression> extends AbstractJqlFunctionTransformer<B> {

    private Function<JqlExpression, P> parameterMapper;
    private BiFunction<B, P, R> builder;

    public JqlParameterizedFunctionTransformer(JqlTransformers jqlTransformers, BiFunction<B, P, R> builder, Function<JqlExpression, P> parameterMapper) {
        super(jqlTransformers);
        this.parameterMapper = parameterMapper;
        this.builder = builder;
    }

    public JqlParameterizedFunctionTransformer(JqlTransformers jqlTransformers, BiFunction<B, P, R> builder) {
        super(jqlTransformers);
        this.builder = builder;
    }

    @Override
    public R apply(B argument, FunctionCall functionCall, List<ObjectVariable> variables) {
        Object parameter;
        JqlExpression jqlParameterExpression = functionCall.getParameters().get(0).getExpression();
        if (parameterMapper != null) {
            parameter = parameterMapper.apply(jqlParameterExpression);
        } else {
            parameter = jqlTransformers.transform(jqlParameterExpression, variables);
        }
        try {
            P castParam = (P) parameter;
            return builder.apply(argument, castParam);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format("Error in expression %s!%s, illegal argument type: %s", argument, functionCall.getFunction().getName(), parameter.getClass()));
        }

    }
}
