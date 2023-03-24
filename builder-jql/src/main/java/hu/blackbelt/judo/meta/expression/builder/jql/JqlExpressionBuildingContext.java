package hu.blackbelt.judo.meta.expression.builder.jql;

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
import hu.blackbelt.judo.meta.expression.variable.Variable;

import java.util.*;
import java.util.stream.Stream;

public class JqlExpressionBuildingContext<TO> implements ExpressionBuildingVariableResolver {

    private final Deque<Deque<Variable>> lambdaScopes = new ArrayDeque<>();
    private final Deque<Object> resolvedAccessors = new ArrayDeque<>();
    private final Deque<Object> resolvedBases = new ArrayDeque<>();
    private final Deque<Expression> baseExpressions = new ArrayDeque<>();
    private TO inputParameterType;
    private String contextNamespace;

    private final JqlExpressionBuilderConfig config;

    public JqlExpressionBuildingContext() {
        this(new JqlExpressionBuilderConfig());
    }

    public JqlExpressionBuildingContext(JqlExpressionBuilderConfig config) {
        this(config, null);
    }

    public JqlExpressionBuildingContext(JqlExpressionBuilderConfig config, TO inputParameterType) {
        this.inputParameterType = inputParameterType;
        this.config = Objects.requireNonNullElseGet(config, JqlExpressionBuilderConfig::new);
        pushVariableScope();
    }

    @Override
    public void pushAccessor(Object accessor) {
        resolvedAccessors.push(accessor);
    }

    @Override
    public void popAccessor() {
        resolvedAccessors.pop();
    }

    @Override
    public boolean containsAccessor(Object accessor) {
        return resolvedAccessors.contains(accessor);
    }

    @Override
    public void pushVariable(Variable variable) {
        variableScope().push(variable);
    }

    @Override
    public Variable popVariable() {
        return variableScope().pop();
    }

    private Deque<Variable> variableScope() {
        return lambdaScopes.peek();
    }

    @Override
    public Optional<Variable> resolveVariable(String name) {
        if (name == null || name.isEmpty()) return Optional.empty();

        Stream<Variable> variablesInScope;
        if (resolveOnlyCurrentLambdaScope()) {
            variablesInScope = variableScope().stream();
        } else {
            variablesInScope = lambdaScopes.stream().flatMap(Deque::stream);
        }
        return variablesInScope
                .filter(v -> name.equals(Objects.requireNonNullElse(v.getHumanName(), v.getName())))
                .findAny();
    }

    @Override
    public void pushBase(Object base) {
        if (base != null) {
            resolvedBases.push(base);
        }
    }

    @Override
    public Object popBase() {
        return resolvedBases.pop();
    }

    @Override
    public Object peekBase() {
        return resolvedBases.peek();
    }

    @Override
    public void setInputParameterType(Object inputParameterType) {
        this.inputParameterType = (TO) inputParameterType;
    }

    @Override
    public TO getInputParameterType() {
        return inputParameterType;
    }

    @Override
    public Optional<String> getContextNamespace() {
        return Optional.ofNullable(contextNamespace);
    }

    @Override
    public void setContextNamespace(String contextNamespace) {
        this.contextNamespace = contextNamespace;
    }

    @Override
    public void pushBaseExpression(Expression baseExpression) {
        baseExpressions.push(baseExpression);
    }

    @Override
    public Expression popBaseExpression() {
        return baseExpressions.poll();
    }

    @Override
    public Expression peekBaseExpression() {
        return baseExpressions.peek();
    }

    /**
     * Returns unmodifiable collection of variables.
     */
    @Override
    public Collection<Variable> getVariables() {
        Collection<Variable> result = new ArrayList<>();
        if (resolveOnlyCurrentLambdaScope()) {
            result.addAll(variableScope());
        } else {
            lambdaScopes.forEach(scope -> result.addAll(scope));
        }
        return Collections.unmodifiableCollection(result);
    }

    @Override
    public void removeVariable(Variable variable) {
        if (resolveOnlyCurrentLambdaScope()) {
            variableScope().remove(variable);
        } else {
            lambdaScopes.forEach(scope -> scope.remove(variable));
        }
    }

    @Override
    public boolean resolveOnlyCurrentLambdaScope() {
        return config.isResolveOnlyCurrentLambdaScope();
    }

    @Override
    public void pushVariableScope() {
        lambdaScopes.push(new ArrayDeque<>());
    }

    @Override
    public void popVariableScope() {
        lambdaScopes.pop();
    }

}
