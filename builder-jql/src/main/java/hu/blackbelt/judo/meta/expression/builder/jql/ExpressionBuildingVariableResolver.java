package hu.blackbelt.judo.meta.expression.builder.jql;

/*-
 * #%L
 * JUDO :: Expression :: Model
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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
