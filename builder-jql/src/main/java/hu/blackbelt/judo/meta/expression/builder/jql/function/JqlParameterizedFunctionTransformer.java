package hu.blackbelt.judo.meta.expression.builder.jql.function;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class JqlParameterizedFunctionTransformer<BASE extends Expression, PARAM, RESULT extends Expression> extends AbstractJqlFunctionTransformer<BASE> {

    private Function<JqlExpression, PARAM> parameterMapper;
    private final BiFunction<BASE, PARAM, RESULT> builder;

    public JqlParameterizedFunctionTransformer(JqlTransformers jqlTransformers, BiFunction<BASE, PARAM, RESULT> builder, Function<JqlExpression, PARAM> parameterMapper) {
        this(jqlTransformers, builder);
        this.parameterMapper = parameterMapper;
    }

    public JqlParameterizedFunctionTransformer(JqlTransformers jqlTransformers, BiFunction<BASE, PARAM, RESULT> builder) {
        super(jqlTransformers);
        this.builder = builder;
    }

    @Override
    public RESULT apply(BASE argument, FunctionCall functionCall, List<ObjectVariable> variables) {
        Object parameter;
        JqlExpression jqlParameterExpression = functionCall.getParameters().get(0).getExpression();
        if (parameterMapper != null) {
            parameter = parameterMapper.apply(jqlParameterExpression);
        } else {
            parameter = jqlTransformers.transform(jqlParameterExpression, variables);
        }
        try {
            PARAM castParam = (PARAM) parameter;
            return builder.apply(argument, castParam);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format("Error in expression %s!%s, illegal argument type: %s (%s)", argument, functionCall.getFunction().getName(), argument.getClass(), e.getMessage()));
        }

    }
}
