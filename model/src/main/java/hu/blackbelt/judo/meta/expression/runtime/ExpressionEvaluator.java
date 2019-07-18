package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.AttributeSelector;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.VariableReference;
import hu.blackbelt.judo.meta.expression.collection.CollectionVariableReference;
import hu.blackbelt.judo.meta.expression.collection.ImmutableCollection;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.object.ObjectVariableReference;
import hu.blackbelt.judo.meta.expression.variable.CollectionVariable;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.expression.variable.Variable;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EMap;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExpressionEvaluator {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionEvaluator.class);
    private final Set<Expression> allExpressions = new HashSet<>();

    private final EMap<Expression, ReferenceExpression> lambdaContainers = ECollections.asEMap(new ConcurrentHashMap<>());
    private final EMap<ReferenceExpression, ReferenceExpression> referenceSources = ECollections.asEMap(new ConcurrentHashMap<>());
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

        getAllInstances(ReferenceExpression.class).forEach(referenceExpression -> {
            // single sources are supported only (ie. filtering unions is unsupported)
            final Optional<ReferenceExpression> referenceSource = getAllInstances(ReferenceExpression.class)
                    .filter(referenceSourceExpression -> referenceExpression.getOperands().contains(referenceSourceExpression))
                    .findAny();

            if (referenceSource.isPresent()) {
                ReferenceExpression referenceSourceExpression = referenceSource.get();

                while (referenceSourceExpression != null && referenceSourceExpression instanceof VariableReference) {
                    final ObjectVariable resolved;

                    if (referenceSourceExpression instanceof ObjectVariableReference) {
                        resolved = ((ObjectVariableReference) referenceSourceExpression).getVariable();
                    } else if (referenceSourceExpression instanceof CollectionVariableReference) {
                        resolved = ((CollectionVariableReference) referenceSourceExpression).getVariable();
                    } else {
                        throw new IllegalStateException("Unsupported variable reference");
                    }

                    if (resolved instanceof ReferenceExpression) {
                        referenceSourceExpression = (ReferenceExpression) resolved;
                    } else {
                        throw new IllegalStateException("Object variable is not a reference");
                    }
                }

                referenceSources.put(referenceExpression, referenceSourceExpression);
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
        referenceSources.clear();
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
            return Collections.singleton(expression);
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

                if (referenceSources.containsKey(expr)) {
                    expr = referenceSources.get(expr);
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

                if (referenceSources.containsKey(expr)) {
                    expr = referenceSources.get(expr);
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
        } if (expression instanceof ReferenceExpression) {
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
