package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.variable.Variable;

import java.util.Optional;

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
}
