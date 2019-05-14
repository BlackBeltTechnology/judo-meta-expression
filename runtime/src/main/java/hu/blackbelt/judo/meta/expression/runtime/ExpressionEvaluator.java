package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableSet;
import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.collection.*;
import hu.blackbelt.judo.meta.expression.constant.*;
import hu.blackbelt.judo.meta.expression.object.ObjectVariableReference;
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

    private final Set<Expression> allExpressions = new HashSet<>();

    private final Map<Expression, ReferenceExpression> lambdaContainers = new ConcurrentHashMap<>();
    private final Map<NavigationExpression, ReferenceExpression> navigationSources = new ConcurrentHashMap<>();
    private final Map<Expression, Collection<Expression>> operandHolders = new ConcurrentHashMap<>();
    private final Map<Expression, EvaluationNode> roots = new ConcurrentHashMap<>();
    private final Map<Expression, EvaluationNode> evaluationMap = new ConcurrentHashMap<>();
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

        getAllInstances(Expression.class).forEach(expression ->
                expression.getOperands().forEach(operand -> {
                    log.trace("Checking operand {} of {}", operand, expression);
                    Expression expr = operand;
                    while (expr != null) {
                        final ObjectVariable variable;
                        if (expr instanceof ObjectVariableReference) {
                            final ObjectVariableReference variableReference = (ObjectVariableReference) expr;
                            variable = variableReference.getVariable();
                        } else if (expr instanceof CollectionVariableReference) {
                            final CollectionVariableReference variableReference = (CollectionVariableReference) expr;
                            variable = variableReference.getVariable();
                        } else {
                            variable = null;
                        }
                        log.trace("  - expr: {}, variable: {}", expr, variable);

                        final Collection<Expression> holders;
                        if (operandHolders.containsKey(expr)) {
                            holders = operandHolders.get(expr);
                        } else {
                            holders = new ArrayList<>();
                            operandHolders.put(expr, holders);
                        }
                        holders.add(expression);

                        if (variable instanceof Expression) {
                            expr = (Expression) variable;
                        } else {
                            expr = null;
                        }
                    }
                }));

        leaves.addAll(getAllInstances(Expression.class)
                .filter(expression -> !getAllInstances(Expression.class)
                        .anyMatch(e -> (e.getOperands().contains(expression) || e.getLambdaFunctions().contains(expression))))
                .collect(Collectors.toSet()));

        // TODO - include data allExpressions without variable references
        roots.putAll(getAllInstances(Expression.class)
                .filter(e -> e.getOperands().isEmpty() && !(e instanceof ReferenceExpression) || (e instanceof Instance) || (e instanceof ImmutableCollection))
                .collect(Collectors.toMap(e -> e, e -> evaluate(e, 0))));
    }

    private void addToAllExpressions(final Expression expression) {
        if (!allExpressions.contains(expression)) {
            allExpressions.add(expression);
            expression.getOperands().forEach(e -> addToAllExpressions(e));
            expression.getLambdaFunctions().forEach(e -> addToAllExpressions(e));
        }
    }

    public EvaluationNode getEvaluationNode(final Expression expression) {
        return evaluationMap.get(expression);
    }

    private EvaluationNode evaluate(final Expression expression, final int level) {
        log.debug("--------------------------------------------------------------------------------");
        log.debug(pad(level) + "Evaluating expression: {}, type: {}", expression, expression.getClass().getSimpleName());

        if (evaluationMap.containsKey(expression)) {
            log.debug(pad(level) + "[---] already processed");
            return evaluationMap.get(expression);
        } else if ((expression instanceof InstanceDefinition)) {
            // instance/collection reference is used by holder (instance/immutable set)
            log.debug(pad(level) + "[definition] container: {}", expression.eContainer());

            return EvaluationNode.builder()
                    .terminals(Collections.emptyMap())
                    .navigations(Collections.emptyMap())
                    .operations(Collections.emptyMap())
                    .build();
        }

        final Map<AttributeRole, Expression> terminals = new HashMap<>();
        final Map<SetTransformation, EvaluationNode> navigations = new HashMap<>();
        final Map<String, DataExpression> operations = new TreeMap<>();

        boolean processed = false;
        if (expression instanceof AttributeSelector) {
            final AttributeSelector attributeSelector = (AttributeSelector) expression;

            final String alias = attributeSelector.getAlias() != null ? attributeSelector.getAlias() : attributeSelector.getAttributeName();

            log.debug(pad(level) + "[terminal] attribute selector: {} AS {}", expression, alias);

            final AttributeRole key = AttributeRole.builder()
                    .alias(alias)
                    .projection(true)
                    .build();

            if (terminals.keySet().stream()
                    .anyMatch(k -> Objects.equals(k.getAlias(), alias))) {
                throw new IllegalStateException("Attribute already processed");
            } else {
                terminals.put(key, expression);
            }
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

        if (operandHolders.containsKey(expression)) {
            operandHolders.get(expression).forEach(holder -> {
                log.debug(pad(level) + "[expr] operand of: {} ({})", holder, holder.getClass().getSimpleName());

                if (!evaluationMap.containsKey(holder)) {
                    // container is not processed yet
                    evaluateContainer(terminals, navigations, operations, holder, level);
                }
            });
            processed = true;
        }

//        if (lambdaContainers.containsKey(expression)) {
//            final ReferenceExpression referenceExpression = lambdaContainers.get(expression);
//            log.debug(pad(level) + "[lambda] container: {}", referenceExpression);
//
//            evaluateContainer(terminals, navigations, operations, referenceExpression, level);
//        }

        if (!processed) {
            log.warn(pad(level) + "! Unprocessed/unused expression: {}", expression);
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

    private void evaluateContainer(final Map<AttributeRole, Expression> terminals, final Map<SetTransformation, EvaluationNode> navigations, final Map<String, DataExpression> operations, final Expression expression, final int level) {
        final EvaluationNode evaluationNode = evaluate(expression, level + 1);

        if (expression instanceof NavigationExpression) {
            final NavigationExpression navigationExpression = (NavigationExpression) expression;
            log.debug(pad(level) + "[container] alias: {}, name: {}", navigationExpression.getAlias(), navigationExpression.getReferenceName());

            final String alias = navigationExpression.getAlias() != null ? navigationExpression.getAlias() : navigationExpression.getReferenceName();
            final SetTransformation key = SetTransformation.builder()
                    .alias(alias)
                    .build();

            final Optional<Map.Entry<SetTransformation, EvaluationNode>> entry = navigations.entrySet().stream()
                    .filter(n -> Objects.equals(n.getKey().getAlias(), alias))
                    .findAny();
            if (entry.isPresent()) {
                final EvaluationNode found = entry.get().getValue();
                final List<String> foundTerminalAliases = (List<String>) found.getTerminals().keySet().stream()
                        .map(k -> ((AttributeRole) k).getAlias())
                        .collect(Collectors.toList());
                final List<String> foundNavigationAliases = (List<String>) found.getNavigations().keySet().stream()
                        .map(k -> ((SetTransformation) k).getAlias())
                        .collect(Collectors.toList());
                final Set<String> foundOperationAliases = found.getOperations().keySet();

                final List<String> newTerminalAliases = (List<String>) evaluationNode.getTerminals().keySet().stream()
                        .map(k -> ((AttributeRole) k).getAlias())
                        .collect(Collectors.toList());
                final List<String> newNavigationAliases = (List<String>) evaluationNode.getNavigations().keySet().stream()
                        .map(k -> ((SetTransformation) k).getAlias())
                        .collect(Collectors.toList());
                final Set<String> newOperationAliases = evaluationNode.getOperations().keySet();

                log.debug(pad(level) + "[merge] found: {}", found.getExpression());
                log.debug(pad(level) + "  - terminals: {}", foundTerminalAliases);
                log.debug(pad(level) + "  - navigations: {}", foundNavigationAliases);
                log.debug(pad(level) + "  - operations: {}", foundOperationAliases);
                log.debug(pad(level) + "[merge] processing: {}", expression);
                log.debug(pad(level) + "  - terminals: {}", newTerminalAliases);
                log.debug(pad(level) + "  - navigations: {}", newNavigationAliases);
                log.debug(pad(level) + "  - operations: {}", newOperationAliases);

                if (newTerminalAliases.stream().anyMatch(a -> foundTerminalAliases.contains(a))) {
                    throw new IllegalStateException("Found terminal alias(es) that are already processed");
                }

                if (newNavigationAliases.stream().anyMatch(a -> foundNavigationAliases.contains(a))) {
                    throw new IllegalStateException("Found navigation alias(es) that are already processed");
                }

                if (newOperationAliases.stream().anyMatch(a -> foundOperationAliases.contains(a))) {
                    throw new IllegalStateException("Found operation alias(es) that are already processed");
                }

                found.getTerminals().putAll(evaluationNode.getTerminals());
                found.getNavigations().putAll(evaluationNode.getNavigations());
                found.getOperations().putAll(evaluationNode.getOperations());
            } else {
                navigations.put(key, evaluationNode);
            }
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
            log.debug(pad(level) + "[copy] {} ({})", expression, expression.getClass().getSimpleName());
            terminals.putAll(evaluationNode.getTerminals());
            navigations.putAll(evaluationNode.getNavigations());
            operations.putAll(evaluationNode.getOperations());
        }
    }

    private static String pad(int level) {
        return StringUtils.leftPad("", level * 2, " ");
    }

    public void cleanup() {
        allExpressions.clear();
        lambdaContainers.clear();
        navigationSources.clear();
        operandHolders.clear();
        leaves.clear();
        roots.clear();
        evaluationMap.clear();
    }

    <T> Stream<T> getAllInstances(final Class<T> clazz) {
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
     * Terms are atomic segments for evaluating an expression including variable references, constants and aggregators. Terms of lambda allExpressions are excluded because they are used in subprocess.
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
