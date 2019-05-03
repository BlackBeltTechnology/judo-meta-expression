package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableSet;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.expression.variable.Variable;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ExpressionEvaluator {

    private final Collection<Expression> expressions = new ArrayList<>();

    private final Map<Expression, ReferenceExpression> lambdaContainers = new ConcurrentHashMap<>();
    private final Map<ReferenceExpression, Expression> sources = new ConcurrentHashMap<>();
    private final Set<Expression> rootExpressions = new HashSet<>();

    public void init(final Collection<Expression> expressions) {
        this.expressions.addAll(expressions);

        getAllInstances(Expression.class).forEach(expression -> {
            final Optional<ReferenceExpression> lambdaContainer = getAllInstances(ReferenceExpression.class)
                    .filter(referenceExpression -> referenceExpression.getLambdaFunctions().contains(expression))
                    .findAny();

            if (lambdaContainer.isPresent()) {
                lambdaContainers.put(expression, lambdaContainer.get());
            }
        });

        getAllInstances(ReferenceExpression.class).forEach(referenceExpression -> {
            // single sources are supported only (ie. filtering unions is unsupported)
            final Optional<ReferenceExpression> source = getAllInstances(ReferenceExpression.class)
                    .filter(e -> referenceExpression.getOperands().contains(e))
                    .findAny();

            if (source.isPresent()) {
                sources.put(referenceExpression, source.get());
            }
        });

        rootExpressions.addAll(getAllInstances(Expression.class)
                .filter(expression -> getAllInstances(Expression.class)
                        .anyMatch(e -> !(e.getOperands().contains(expression) || e.getLambdaFunctions().contains(expression))))
                .collect(Collectors.toSet()));
    }

    public void cleanup() {
        expressions.clear();
        lambdaContainers.clear();
        sources.clear();
        rootExpressions.clear();
    }

    <T> Stream<T> getAllInstances(final Class<T> clazz) {
        return expressions.stream()
                .filter(e -> clazz.isAssignableFrom(e.getClass()))
                .map(e -> (T) e);
    }

    public boolean isLambdaFunction(final Expression expression) {
        return lambdaContainers.containsKey(expression);
    }

    public boolean isRoot(final Expression expression) {
        return rootExpressions.contains(expression);
    }

    /**
     * Get operation expression of a given expression.
     * <p>
     * Operation expression is the expression holding parameter expression as operand or the parameter expression itself.
     *
     * @param expression expression evaluated as operand
     * @return operator expression
     */
    public Expression getOperationExpression(final Expression expression) {
        final Optional<Expression> operationExpression = getAllInstances(Expression.class)
                .filter(e -> e.getOperands().contains(expression))
                .findAny();
        return operationExpression.orElse(expression);
    }

    /**
     * Get terms of a given expression.
     * <p>
     * Terms are atomic segments for evaluating an expression including variable references, constants and aggregators. Terms of lambda expressions are excluded because they are used in subprocess.
     *
     * @param expression an expression
     * @return expression terms
     */
    public Set<Expression> getExpressionTerms(final Expression expression) {
        final List<Expression> terms = new ArrayList<>(expression.getOperands());
        terms.removeAll(expression.getLambdaFunctions());

        if (terms.isEmpty()) {
            return terms.stream()
                    .map(e -> getExpressionTerms(e))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
        } else {
            return ImmutableSet.of(expression);
        }
    }

    /**
     * Get list of variables that are available in scope of a given expression.
     *
     * @param expression expression
     * @return available variables
     */
    public Set<Variable> getVariablesOfScope(final Expression expression) {
        if (!isLambdaFunction(expression)) {
            return getExpressionTerms(expression).stream()
                    .filter(e -> (e instanceof ImmutableSet) || (e instanceof Instance))
                    .map(e -> (ObjectVariable)e)
                    .collect(Collectors.toSet());
        } else {
            final Set<Variable> variables = new HashSet<>();

            Expression expr = lambdaContainers.get(expression);

            while (expr != null) {
                if (expr instanceof Variable) {
                    variables.add((Variable) expr);
                }

                if (sources.containsKey(expr)) {
                    expr = sources.get(expr);
                } else {
                    variables.addAll(getVariablesOfScope(expr));
                    expr = null;
                }
            }

            return variables;
        }
    }
}
