package hu.blackbelt.judo.meta.expression.builder.jql;

import java.util.Collection;
import java.util.Optional;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.variable.Variable;

public interface ExpressionBuildingVariableResolver {
    void pushAccessor(Object accessor);

    void popAccessor();

    boolean containsAccessor(Object accessor);

    void pushVariable(Variable variable);

    Variable popVariable();

    Optional<Variable> resolveVariable(String name);

    void pushBase(Object base);

    Object popBase();

    Object peekBase();

    void pushBaseExpression(Expression baseExpression);

    Expression popBaseExpression();

    Expression peekBaseExpression();

	Collection<Variable> getVariables();
}
