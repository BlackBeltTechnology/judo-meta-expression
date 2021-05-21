package hu.blackbelt.judo.meta.expression.builder.jql;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.variable.Variable;

public class JqlExpressionBuildingContext implements ExpressionBuildingVariableResolver {

    private final Deque<Variable> variables = new ArrayDeque<>();
	private final Deque<Object> resolvedAccessors = new ArrayDeque<>();
    private final Deque<Object> resolvedBases = new ArrayDeque<>();
    private final Deque<Expression> baseExpressions = new ArrayDeque<>();

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
                .filter(v -> Objects.equals(Optional.ofNullable(v.getHumanName()).orElse(v.getName()), name))
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
    
    @Override
    public Collection<Variable> getVariables() {
		return variables;
	}

	@Override
	public void removeVariable(Variable variable) {
		variables.remove(variable);
	}


}
