package hu.blackbelt.judo.meta.expression.builder.jql.function;

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

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.jql.jqldsl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder.NAMESPACE_SEPARATOR;

public class JqlParameterizedFunctionTransformer<BASE extends Expression, PARAM, RESULT extends Expression> extends AbstractJqlFunctionTransformer<BASE> {

    private static final Logger log = LoggerFactory.getLogger(JqlParameterizedFunctionTransformer.class.getName());

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
            log.debug("Function '" + function.getName() + "' must have exactly one parameter");
            throw new IllegalArgumentException("Function '" + function.getName() + getStringOf(parameters) + "' of argument '" + argument + "' cannot be resolved");
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
            if (castParam instanceof QualifiedName &&
                ((QualifiedName) castParam).getNamespaceElements().isEmpty() &&
                context.getContextNamespace().isPresent()) {
                QualifiedName qualifiedName = (QualifiedName) castParam;
                qualifiedName.getNamespaceElements().addAll(List.of(context.getContextNamespace().get().split(NAMESPACE_SEPARATOR)));
            }
            return builder.apply(argument, castParam);
        } catch (ClassCastException e) {
            log.debug(String.format("Error in expression %s!%s, illegal argument type: %s", argument, function.getName(), argument.getClass()), e);
            throw new IllegalArgumentException("Function '" + function.getName() + getStringOf(parameters) + "' of argument '" + argument + "' cannot be resolved");
        }
    }

    private static String getStringOf(List<FunctionParameter> parameters) {
        String parametersString = parameters.stream()
                .map(p -> p.getExpression().getClass().getName())
                .collect(Collectors.joining(", "));
        return "(" + parametersString + ")";
    }

}
