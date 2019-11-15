package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

public class JqlExpressionBuildingContext {

    private Deque<ObjectVariable> variables = new ArrayDeque<>();
    private Deque<Object> resolvedAccessors = new ArrayDeque<>();

    public void pushVariable(ObjectVariable variable) {
        variables.push(variable);
    }

    public ObjectVariable popVariable() {
        return variables.pop();
    }

    public void pushAccessor(Object accessor) {
        resolvedAccessors.push(accessor);
    }

    public void popAccessor() {
        resolvedAccessors.pop();
    }

    public boolean containsAccessor(Object accessor) {
        return resolvedAccessors.contains(accessor);
    }

    public Optional<ObjectVariable> resolveVariable(String name) {
        return variables.stream()
                .filter(v -> Objects.equals(v.getName(), name))
                .findAny();
    }
}
