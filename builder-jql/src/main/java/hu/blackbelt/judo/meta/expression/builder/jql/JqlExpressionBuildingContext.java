package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class JqlExpressionBuildingContext {

    private List<ObjectVariable> variables = new ArrayList<>();
    private Stack<Object> resolvedAccessors = new Stack<>();

    public List<ObjectVariable> getVariables() {
        return variables;
    }

    public void addVariable(ObjectVariable variable) {
        variables.add(variable);
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
}
