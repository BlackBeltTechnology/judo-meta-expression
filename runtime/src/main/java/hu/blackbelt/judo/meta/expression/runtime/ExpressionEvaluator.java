package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableSet;
import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.collection.*;
import hu.blackbelt.judo.meta.expression.constant.*;
import hu.blackbelt.judo.meta.expression.object.ObjectVariableReference;
import hu.blackbelt.judo.meta.expression.variable.CollectionVariable;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.expression.variable.Variable;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ExpressionEvaluator {

    private final Set<Expression> allExpressions = new HashSet<>();

    private final EMap<Expression, ReferenceExpression> lambdaContainers = ECollections.asEMap(new ConcurrentHashMap<>());
    private final EMap<NavigationExpression, ReferenceExpression> navigationSources = ECollections.asEMap(new ConcurrentHashMap<>());
    private final Set<Expression> leaves = new HashSet<>();

    public void init(final Collection<Expression> expressions) {
        expressions.forEach(expression -> addToAllExpressions(expression));

        getAllInstances(Expression.class).forEach(expression -> {
            final Optional<ReferenceExpression> lambdaContainer = getAllInstances(ReferenceExpression.class)
                    .filter(referenceExpression -> referenceExpression.getLambdaFunctions().contains(expression))
                    .findAny();

            if (lambdaContainer.isPresent()) {
                lambdaContainers.put(expression, lambdaContainer.get());
            }
        });

        getAllInstances(NavigationExpression.class).forEach(navigationExpression -> {
            // single sources are supported only (ie. filtering unions is unsupported)
            final Optional<ReferenceExpression> navigationSource = getAllInstances(ReferenceExpression.class)
                    .filter(referenceExpression -> navigationExpression.getOperands().contains(referenceExpression))
                    .findAny();

            if (navigationSource.isPresent()) {
                ReferenceExpression referenceExpression = navigationSource.get();

                while (referenceExpression != null && referenceExpression instanceof VariableReference) {
                    final ObjectVariable resolved;

                    if (referenceExpression instanceof ObjectVariableReference) {
                        resolved = ((ObjectVariableReference) referenceExpression).getVariable();
                    } else if (referenceExpression instanceof CollectionVariableReference) {
                        resolved = ((CollectionVariableReference) referenceExpression).getVariable();
                    } else {
                        throw new IllegalStateException("Unsupported variable reference");
                    }

                    if (resolved instanceof ReferenceExpression) {
                        referenceExpression = (ReferenceExpression) resolved;
                    } else {
                        throw new IllegalStateException("Object variable is not a reference");
                    }
                }

                navigationSources.put(navigationExpression, referenceExpression);
            }
        });

        leaves.addAll(getAllInstances(Expression.class)
                .filter(expression -> !getAllInstances(Expression.class)
                        .anyMatch(e -> (e.getOperands().contains(expression) || e.getLambdaFunctions().contains(expression))))
                .collect(Collectors.toSet()));
    }

    private void addToAllExpressions(final Expression expression) {
        if (!allExpressions.contains(expression)) {
            allExpressions.add(expression);
            expression.getOperands().forEach(e -> addToAllExpressions(e));
            expression.getLambdaFunctions().forEach(e -> addToAllExpressions(e));
        }
    }

    public void cleanup() {
        allExpressions.clear();
        lambdaContainers.clear();
        navigationSources.clear();
        leaves.clear();
    }

    public <T> Stream<T> getAllInstances(final Class<T> clazz) {
        return allExpressions.stream()
                .filter(e -> clazz.isAssignableFrom(e.getClass()))
                .map(e -> (T) e);
    }

    public boolean isLambdaFunction(final Expression expression) {
        return lambdaContainers.containsKey(expression);
    }

    public boolean isLeaf(final Expression expression) {
        return leaves.contains(expression);
    }

    /**
     * Get terms of a given expression.
     * <p>
     * Terms are atomic segments for evaluating an expression including variable references, constants and aggregators. Terms of lambda allExpressions are excluded because they are used in subprocess.
     *
     * @param expression an expression
     * @return expression terms
     */
    public static Set<Expression> getExpressionTerms(final Expression expression) {
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
            final Set<Variable> variables = new HashSet<>();

            Expression expr = expression;

            while (expr != null) {
                if (expr instanceof Variable) {
                    variables.add((Variable) expr);
                }

                if (navigationSources.containsKey(expr)) {
                    expr = navigationSources.get(expr);
                } else {
                    expr = null;
                }
            }

            return variables;
        } else {
            final Set<Variable> variables = new HashSet<>();

            Expression expr = lambdaContainers.get(expression);

            while (expr != null) {
                if (expr instanceof Variable) {
                    variables.add((Variable) expr);
                }

                if (navigationSources.containsKey(expr)) {
                    expr = navigationSources.get(expr);
                } else {
                    variables.addAll(getVariablesOfScope(expr));
                    expr = null;
                }
            }

            return variables;
        }
    }

    public static Expression getBase(final Expression expression) {
        if ((expression instanceof Instance) || (expression instanceof ImmutableCollection)) {
            return expression;
        } if (expression instanceof NavigationExpression) {
            final List<Expression> sources = expression.getOperands();
            if (sources.isEmpty()) {
                return expression;
            } else if (sources.size() == 1) {
                return getBase(sources.get(0));
            } else {
                throw new UnsupportedOperationException("Multiple sources are not supported");
            }
        } else if (expression instanceof AttributeSelector) {
            return getBase(((AttributeSelector) expression).getObjectExpression());
        } else if (expression instanceof ObjectVariableReference) {
            final ObjectVariable objectVariable = ((ObjectVariableReference) expression).getVariable();
            return objectVariable instanceof Expression ? (Expression) objectVariable : null;
        } else if (expression instanceof CollectionVariableReference) {
            final CollectionVariable collectionVariable = ((CollectionVariableReference) expression).getVariable();
            return collectionVariable instanceof Expression ? (Expression) collectionVariable : null;
        } else {
            log.error("TYPE: {}", expression.getClass().getSimpleName());
            throw new UnsupportedOperationException("Not supported yet");
        }
    }
}
