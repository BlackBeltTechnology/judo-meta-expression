package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.variable.Variable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class JqlExpressionBuildingContext implements ExpressionBuildingVariableResolver {

    private final Deque<Deque<Variable>> lambdaScopes = new ArrayDeque<>();
	private final Deque<Object> resolvedAccessors = new ArrayDeque<>();
    private final Deque<Object> resolvedBases = new ArrayDeque<>();
    private final Deque<Expression> baseExpressions = new ArrayDeque<>();

    private final JqlExpressionBuilderConfig config;
    
    public JqlExpressionBuildingContext() {
        this(new JqlExpressionBuilderConfig());
    }
    
    public JqlExpressionBuildingContext(JqlExpressionBuilderConfig config) {
        this.config = config;
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
        Stream<Variable> variablesInScope;
        if (resolveOnlyCurrentLambdaScope()) {
            variablesInScope = variableScope().stream();
        } else {
            variablesInScope = lambdaScopes.stream().flatMap(Deque::stream);
        }
        return variablesInScope
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
