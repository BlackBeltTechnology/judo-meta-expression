package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

public class JqlExpressionBuildingContext {

    private Deque<ObjectVariable> variables = new ArrayDeque<>();
    private Deque<Object> resolvedAccessors = new ArrayDeque<>();
    private Deque<Object> resolvedBases = new ArrayDeque<>();
    private Deque<Expression> baseExpressions = new ArrayDeque<>();

    public void pushVariable(ObjectVariable variable) {
        variables.push(variable);
    }

    public ObjectVariable popVariable() {
        return variables.pop();
    }

    public void pushAccessor(Object accessor) {
        resolvedAccessors.push(accessor);
    }

    public void pushBase(Object base) {
        if (base != null) {
            resolvedBases.push(base);
        }
    }

    public Object popBase() {
        return resolvedBases.pop();
    }

    public void popAccessor() {
        resolvedAccessors.pop();
    }

    public Object peekBase() {
        if (!resolvedBases.isEmpty()) {
            return resolvedBases.peek();
        } else {
            return null;
        }
    }

    public boolean containsAccessor(Object accessor) {
        return resolvedAccessors.contains(accessor);
    }

    public Optional<ObjectVariable> resolveVariable(String name) {
        return variables.stream()
                .filter(v -> Objects.equals(v.getName(), name))
                .findAny();
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
