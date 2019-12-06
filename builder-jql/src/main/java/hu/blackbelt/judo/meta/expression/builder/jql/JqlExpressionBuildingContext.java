package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.expression.variable.Variable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

public class JqlExpressionBuildingContext {

    private Deque<Variable> variables = new ArrayDeque<>();
    private Deque<Object> resolvedAccessors = new ArrayDeque<>();
    private Deque<Object> resolvedBases = new ArrayDeque<>();
    private Deque<Expression> baseExpressions = new ArrayDeque<>();

    public void pushAccessor(Object accessor) {
        resolvedAccessors.push(accessor);
    }

    public void popAccessor() {
        resolvedAccessors.pop();
    }

    public boolean containsAccessor(Object accessor) {
        return resolvedAccessors.contains(accessor);
    }

    public void pushVariable(Variable variable) {
        variables.push(variable);
    }

    public Variable popVariable() {
        return variables.pop();
    }

    public Optional<Variable> resolveVariable(String name) {
        return variables.stream()
                .filter(v -> Objects.equals(v.getName(), name))
                .findAny();
    }

    public void pushBase(Object base) {
        if (base != null) {
            resolvedBases.push(base);
        }
    }

    public Object popBase() {
        return resolvedBases.pop();
    }

    public Object peekBase() {
        return resolvedBases.peek();
    }

    public void pushBaseExpression(Expression baseExpression) {
        baseExpressions.push(baseExpression);
    }

    public Expression popBaseExpression() {
        return baseExpressions.poll();
    }

    public Expression peekBaseExpression() {
        return baseExpressions.peek();
    }

}
