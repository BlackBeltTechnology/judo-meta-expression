package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableSet;
import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.collection.CollectionVariableReference;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.object.ObjectVariableReference;
import hu.blackbelt.judo.meta.expression.variable.CollectionVariable;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.expression.variable.Variable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ExpressionEvaluator {

    private final Collection<Expression> expressions = new ArrayList<>();

    private final Map<Expression, ReferenceExpression> lambdaContainers = new ConcurrentHashMap<>();
    private final Map<NavigationExpression, ReferenceExpression> navigationSources = new ConcurrentHashMap<>();
    private final Map<Expression, Collection<Expression>> operandHolders = new ConcurrentHashMap<>();
    private final Map<VariableReference, Expression> variableReferenceSources = new ConcurrentHashMap<>();
    private final Map<Variable, Collection<VariableReference>> variableReferences = new ConcurrentHashMap<>();
    private final Map<ReferenceExpression, EvaluationNode> roots = new HashMap<>();
    private final Set<Expression> leaves = new HashSet<>();

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

        getAllInstances(Expression.class).forEach(expression ->
            expression.getOperands().forEach(operand -> {
                final Collection<Expression> holders;
                if (operandHolders.containsKey(operand)) {
                    holders = operandHolders.get(operand);
                } else {
                    holders = new ArrayList<>();
                    operandHolders.put(operand, holders);
                }
                holders.add(expression);
            }));

        getAllInstances(Expression.class).forEach(navigationExpression -> {
            variableReferenceSources.putAll(getAllInstances(VariableReference.class)
                    .filter(referenceExpression -> navigationExpression.getOperands().contains(referenceExpression))
                    .collect(Collectors.toMap(variableReference -> variableReference, variableReference -> navigationExpression)));
        });

        getAllInstances(VariableReference.class).forEach(variableReference -> {
            final Collection<VariableReference> references;
            if (variableReference instanceof ObjectVariableReference) {
                final ObjectVariable objectVariable = ((ObjectVariableReference) variableReference).getVariable();
                if (variableReferences.containsKey(objectVariable)) {
                    references = variableReferences.get(objectVariable);
                } else {
                    references = new ArrayList<>();
                    variableReferences.put(objectVariable, references);
                }
            } else if (variableReference instanceof CollectionVariableReference) {
                final CollectionVariable collectionVariable = ((CollectionVariableReference) variableReference).getVariable();
                if (variableReferences.containsKey(collectionVariable)) {
                    references = variableReferences.get(collectionVariable);
                } else {
                    references = new ArrayList<>();
                    variableReferences.put(collectionVariable, references);
                }
            } else {
                throw new IllegalStateException("Unsupported reference type");
            }
            references.add(variableReference);
        });

        leaves.addAll(getAllInstances(Expression.class)
                .filter(expression -> !getAllInstances(Expression.class)
                        .anyMatch(e -> (e.getOperands().contains(expression) || e.getLambdaFunctions().contains(expression))))
                .collect(Collectors.toSet()));

        roots.putAll(navigationSources.values().stream()
                .filter(s -> !navigationSources.containsKey(s))
                .collect(Collectors.toSet())
                .stream()
                .collect(Collectors.toMap(r -> r, r -> evaluate(r, 0))));

        log.debug("ROOTS:\n{}", roots);
    }

    private EvaluationNode evaluate(final Expression expression, final int level) {
        log.debug(pad(level) + "Evaluating expression: {}, type: {}", expression, expression.getClass().getSimpleName());

        final Collection<Expression> terminals = new ArrayList<>();
        final Map<String, EvaluationNode> navigations = new TreeMap<>();

        if (expression instanceof AttributeSelector) {
            log.debug(pad(level) + "[terminal] attribute selector: {}", expression);
            terminals.add(expression);
        } else if (expression instanceof AggregatedExpression) {
            log.debug(pad(level) + "[aggregated] aggregated expression: {}, NOT SUPPORTED YET", expression);
        } else if (variableReferences.containsKey(expression)) {
            final Collection<VariableReference> references = variableReferences.get(expression);
            references.forEach(r -> {
                if (variableReferenceSources.containsKey(r)) {
                    final Expression variableReferenceSource = variableReferenceSources.get(r);
                    log.debug(pad(level) + "[variable] variable type: {}", variableReferenceSource.getClass().getSimpleName());

                    final EvaluationNode evaluationNode = evaluate(variableReferenceSource, level + 1);
                    log.debug(pad(level) + "-> evaluated: {}", evaluationNode);

                    if (variableReferenceSource instanceof NavigationExpression) {
                        // variable must be base of navigation expressions so no further looking is required for source
                        final NavigationExpression navigationExpression = (NavigationExpression) variableReferenceSource;
                        navigations.put(navigationExpression.getReferenceName(), evaluationNode);
                    } else {
                        terminals.addAll(evaluationNode.getTerminals());
                        navigations.putAll(evaluationNode.getNavigations());
                    }
                }
            });
        } else if (operandHolders.containsKey(expression)) {
            operandHolders.get(expression).forEach(holder -> {
                log.debug(pad(level) + "[expr] operand of: {}", holder);
                final EvaluationNode evaluationNode = evaluate(holder, level + 1);
                log.debug(pad(level) + "=> evaluated: {}", evaluationNode);

                if (holder instanceof NavigationExpression) {
                    final NavigationExpression navigationExpression = (NavigationExpression) holder;
                    navigations.put(navigationExpression.getReferenceName(), evaluationNode);
                } else {
                    terminals.addAll(evaluationNode.getTerminals());
                    navigations.putAll(evaluationNode.getNavigations());
                }
            });
        } else {
            log.debug(pad(level) + "[---] {}", expression);
        }

        return EvaluationNode.builder()
                .expression(expression)
                .terminals(terminals)
                .navigations(navigations)
                .build();
    }

    private static String pad(int level) {
        return StringUtils.leftPad("", level * 2, " ");
    }

    public void cleanup() {
        expressions.clear();
        lambdaContainers.clear();
        navigationSources.clear();
        operandHolders.clear();
        leaves.clear();
        roots.clear();
        variableReferences.clear();
    }

    <T> Stream<T> getAllInstances(final Class<T> clazz) {
        return expressions.stream()
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
                    .map(e -> (ObjectVariable) e)
                    .collect(Collectors.toSet());
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
}
