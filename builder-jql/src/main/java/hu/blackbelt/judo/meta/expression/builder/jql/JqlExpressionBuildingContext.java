package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

public class JqlExpressionBuildingContext {

    private Deque<ObjectVariable> variables = new ArrayDeque<>();
    private Deque<Object> resolvedAccessors = new ArrayDeque<>();
    private Deque<Object> resolvedBases = new ArrayDeque<>();

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
        resolvedBases.push(base);
    }

    public void popAccessor() {
        resolvedAccessors.pop();
    }

    public void popBase() {
        if (!resolvedBases.isEmpty()) {
            resolvedBases.pop();
        }
    }

    public boolean containsAccessor(Object accessor) {
        return resolvedAccessors.contains(accessor);
    }

    public boolean containsBase(Object base) {
        return resolvedBases.contains(base);
    }

    public Optional<ObjectVariable> resolveVariable(String name) {
        return variables.stream()
                .filter(v -> Objects.equals(v.getName(), name))
                .findAny();
    }
}
