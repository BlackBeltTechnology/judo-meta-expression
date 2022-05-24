package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.variable.Variable;

import java.util.Collection;
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

    void setInputParameterType(Object inputParameterType);

    Object getInputParameterType();

    Optional<String> getContextNamespace();

    void setContextNamespace(String contextNamespace);

    void pushBaseExpression(Expression baseExpression);

    Expression popBaseExpression();

    Expression peekBaseExpression();

	Collection<Variable> getVariables();

	void removeVariable(Variable variable);

	boolean resolveOnlyCurrentLambdaScope();

	void pushVariableScope();

    void popVariableScope();
}
