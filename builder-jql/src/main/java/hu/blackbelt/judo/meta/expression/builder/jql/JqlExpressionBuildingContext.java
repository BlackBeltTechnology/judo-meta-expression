package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.expression.variable.Variable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

public class JqlExpressionBuildingContext implements ExpressionBuildingVariableResolver {

    private Deque<Variable> variables = new ArrayDeque<>();
    private Deque<Object> resolvedAccessors = new ArrayDeque<>();
    private Deque<Object> resolvedBases = new ArrayDeque<>();
    private Deque<Expression> baseExpressions = new ArrayDeque<>();

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
        variables.push(variable);
    }

    @Override
    public Variable popVariable() {
        return variables.pop();
    }

    @Override
    public Optional<Variable> resolveVariable(String name) {
        return variables.stream()
                .filter(v -> Objects.equals(v.getName(), name))
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

}
