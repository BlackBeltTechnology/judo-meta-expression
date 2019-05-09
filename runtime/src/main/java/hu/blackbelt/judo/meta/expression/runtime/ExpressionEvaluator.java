package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableSet;
import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.collection.CollectionVariableReference;
import hu.blackbelt.judo.meta.expression.collection.ImmutableCollection;
import hu.blackbelt.judo.meta.expression.collection.SortExpression;
import hu.blackbelt.judo.meta.expression.constant.*;
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
    private final Map<Variable, Collection<VariableReference>> variableReferences = new ConcurrentHashMap<>();
    private final Map<Expression, EvaluationNode> roots = new ConcurrentHashMap<>();
    private final Map<Expression, EvaluationNode> evaluationMap = new ConcurrentHashMap<>();
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

        roots.putAll(getAllInstances(Expression.class)
                .filter(e -> e.getOperands().isEmpty() && ((e instanceof Instance) || (e instanceof ImmutableCollection) || !(e instanceof ReferenceExpression)))
                .collect(Collectors.toMap(e -> e, e -> evaluate(e, 0))));
    }

    private EvaluationNode evaluate(final Expression expression, final int level) {
        log.debug(pad(level) + "Evaluating expression: {}, type: {}", expression, expression.getClass().getSimpleName());

        if (evaluationMap.containsKey(expression)) {
            log.debug(pad(level) + "[---] already processed");
            return evaluationMap.get(expression);
        }

        final Map<String, Expression> terminals = new TreeMap<>();
        final Map<SetTransformation, EvaluationNode> navigations = new HashMap<>();
        final Map<String, DataExpression> operations = new TreeMap<>();

        boolean processed = false;
        if (expression instanceof AttributeSelector) {
            final AttributeSelector attributeSelector = (AttributeSelector) expression;

            log.debug(pad(level) + "[terminal] attribute selector: {} AS {}", expression, attributeSelector.getAlias());

            terminals.put(attributeSelector.getAlias() != null ? attributeSelector.getAlias() : attributeSelector.getAttributeName(), expression);
            processed = true;
        } else if (expression instanceof DataExpression) {
            final DataExpression dataExpression = (DataExpression) expression;
            log.debug(pad(level) + "[operation] data expression: {} AS {}", expression, dataExpression.getAlias());

            final String alias = dataExpression.getAlias();
            if (alias != null) {
                operations.put(alias != null ? alias : "", dataExpression);
            } else if (isLeaf(dataExpression)) {
                log.warn("No alias is defined for operation: {}", dataExpression);
            }

            processed = true;
        }

        if (variableReferences.containsKey(expression)) {
            variableReferences.get(expression).forEach(r -> {
                if (r.eContainer() instanceof Expression) {
                    final Expression variableReferenceSource = (Expression) r.eContainer();
                    log.debug(pad(level) + "[variable] variable type: {}", variableReferenceSource.getClass().getSimpleName());

                    evaluateContainer(terminals, navigations, operations, variableReferenceSource, level);
                } else {
                    throw new IllegalStateException("Container must be expression");
                }
            });
            processed = true;
        } else if (operandHolders.containsKey(expression)) {
            operandHolders.get(expression).forEach(holder -> {
                log.debug(pad(level) + "[expr] operand of: {}", holder);

                evaluateContainer(terminals, navigations, operations, holder, level);
            });
            processed = true;
        }

        if (lambdaContainers.containsKey(expression)) {
            final ReferenceExpression referenceExpression = lambdaContainers.get(expression);
            log.debug(pad(level) + "[lambda] container: {}", referenceExpression);

            evaluateContainer(terminals, navigations, operations, referenceExpression, level);
        }

        if (!processed) {
            log.warn(pad(level) + "! Unprocessed expression: {}", expression);
        }

        final EvaluationNode result = EvaluationNode.builder()
                .expression(expression)
                .terminals(terminals)
                .operations(operations)
                .navigations(navigations)
                .build();
        evaluationMap.put(expression, result);

        log.debug(pad(level) + "-> {}", result);

        return result;
    }

    private void evaluateContainer(final Map<String, Expression> terminals, final Map<SetTransformation, EvaluationNode> navigations, final Map<String, DataExpression> operations, final Expression expression, final int level) {
        final EvaluationNode evaluationNode = evaluate(expression, level + 1);

        if (expression instanceof NavigationExpression) {
            final NavigationExpression navigationExpression = (NavigationExpression) expression;
            log.debug(pad(level) + "[container] alias: {}", navigationExpression.getAlias());

            final SetTransformation key = SetTransformation.builder()
                    .alias(navigationExpression.getAlias() != null ? navigationExpression.getAlias() : navigationExpression.getReferenceName())
                    .build();

            navigations.put(key, evaluationNode);
        } else if (expression instanceof FilteringExpression) {
            final FilteringExpression filteringExpression = (FilteringExpression) expression;
            log.debug(pad(level) + "[filtering] filtering: {}", expression);

            final SetTransformation key = SetTransformation.builder()
                    .alias(filteringExpression.getAlias())
                    .filtering(Collections.singleton(evaluationNode))
                    .build();

            navigations.put(key, evaluationNode);
        } else if (expression instanceof SortExpression) {
            log.warn(pad(level) + "[ordering] ordering is not supported yet: {}", expression);
        } else if (expression instanceof WindowingExpression) {
            log.warn(pad(level) + "[windowing] windowing is not supported yet: {}", expression);
        } else {
            terminals.putAll(evaluationNode.getTerminals());
            navigations.putAll(evaluationNode.getNavigations());
            operations.putAll(evaluationNode.getOperations());
        }
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
        evaluationMap.clear();
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
