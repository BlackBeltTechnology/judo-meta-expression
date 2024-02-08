package hu.blackbelt.judo.meta.expression.runtime;

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

    private final Map<Expression, ReferenceExpression> lambdaContainers = new ConcurrentHashMap<>();
    private final Map<ReferenceExpression, ReferenceExpression> referenceSources = new ConcurrentHashMap<>();
    private final Set<Expression> leaves = new HashSet<>();

    public void init(final Collection<Expression> expressions) {
        expressions.forEach(expression -> addToAllExpressions(expression));

        Set<Expression> allPlainExpression = getAllInstances(Expression.class).collect(Collectors.toSet());
        Set<ReferenceExpression> allReferenceExpression = getAllInstances(ReferenceExpression.class).collect(Collectors.toSet());

        allPlainExpression.parallelStream().forEach(expression -> {
            final Optional<ReferenceExpression> lambdaContainer = allReferenceExpression.stream()
                    .filter(referenceExpression -> referenceExpression.getLambdaFunctions().contains(expression))
                    .findAny();

            if (lambdaContainer.isPresent()) {
                lambdaContainers.put(expression, lambdaContainer.get());
            }

        });

        allReferenceExpression.parallelStream().forEach(referenceExpression -> {
            // single sources are supported only (ie. filtering unions is unsupported)
            final Optional<ReferenceExpression> referenceSource = allReferenceExpression.stream()
                    .filter(referenceSourceExpression -> referenceExpression.getOperands().contains(referenceSourceExpression))
                    .findAny();

            if (referenceSource.isPresent()) {
                ReferenceExpression referenceSourceExpression = referenceSource.get();

                while (referenceSourceExpression != null && referenceSourceExpression instanceof VariableReference) {
                    final Variable resolved;

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
        if (expression instanceof AttributeSelector) {
            final AttributeSelector attributeSelector = (AttributeSelector) expression;
            return getVariablesOfScope(attributeSelector.getObjectExpression());
        } else if (!isLambdaFunction(expression)) {
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
        } else if (expression instanceof ReferenceExpression) {
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
