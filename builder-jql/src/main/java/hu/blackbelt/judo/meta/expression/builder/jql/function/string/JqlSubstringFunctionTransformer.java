package hu.blackbelt.judo.meta.expression.builder.jql.function.string;

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
import hu.blackbelt.judo.meta.expression.IntegerExpression;
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.JqlFunctionTransformer;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newSubStringBuilder;

public class JqlSubstringFunctionTransformer implements JqlFunctionTransformer<StringExpression> {

    private ExpressionTransformer jqlTransformers;

    public JqlSubstringFunctionTransformer(ExpressionTransformer jqlTransformers) {
        this.jqlTransformers = jqlTransformers;
    }

    @Override
    public Expression apply(StringExpression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        IntegerExpression position = (IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context);
        IntegerExpression length = (IntegerExpression) jqlTransformers.transform(functionCall.getParameters().get(1).getExpression(), context);
        return newSubStringBuilder().withExpression(argument).withPosition(position).withLength(length).build();
    }

}
