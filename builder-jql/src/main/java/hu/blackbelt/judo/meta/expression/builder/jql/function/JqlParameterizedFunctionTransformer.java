package hu.blackbelt.judo.meta.expression.builder.jql.function;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionParameter;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class JqlParameterizedFunctionTransformer<BASE extends Expression, PARAM, RESULT extends Expression> extends AbstractJqlFunctionTransformer<BASE> {

    private Function<JqlExpression, PARAM> parameterMapper;
    private final BiFunction<BASE, PARAM, RESULT> builder;

    public JqlParameterizedFunctionTransformer(ExpressionTransformer jqlTransformers, BiFunction<BASE, PARAM, RESULT> builder, Function<JqlExpression, PARAM> parameterMapper) {
        this(jqlTransformers, builder);
        this.parameterMapper = parameterMapper;
    }

    public JqlParameterizedFunctionTransformer(ExpressionTransformer jqlTransformers, BiFunction<BASE, PARAM, RESULT> builder) {
        super(jqlTransformers);
        this.builder = builder;
    }

    @Override
    public RESULT apply(BASE argument, JqlFunction function, ExpressionBuildingVariableResolver context) {
        List<FunctionParameter> parameters = function.getParameters();
        if (parameters.size() != 1) {
            throw new IllegalArgumentException("Function (" + function.getName() + ") must have exactly one parameter");
        }
        JqlExpression jqlParameterExpression = parameters.get(0).getExpression();

        Object parameter;
        if (parameterMapper != null) {
            parameter = parameterMapper.apply(jqlParameterExpression);
        } else {
            parameter = expressionTransformer.transform(jqlParameterExpression, context);
        }
        try {
            PARAM castParam = (PARAM) parameter;
            return builder.apply(argument, castParam);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format("Error in expression %s!%s, illegal argument type: %s (%s)", argument, function.getName(), argument.getClass(), e.getMessage()));
        }
    }

}
