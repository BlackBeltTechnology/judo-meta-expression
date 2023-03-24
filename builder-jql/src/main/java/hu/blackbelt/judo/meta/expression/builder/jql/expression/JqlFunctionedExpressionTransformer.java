package hu.blackbelt.judo.meta.expression.builder.jql.expression;

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

import hu.blackbelt.judo.meta.expression.CollectionExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.ObjectExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionedExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.NavigationExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JqlFunctionedExpressionTransformer<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> extends AbstractJqlExpressionTransformer<FunctionedExpression, NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> {

    private static final Logger LOG = LoggerFactory.getLogger(JqlFunctionedExpressionTransformer.class.getName());

    public JqlFunctionedExpressionTransformer(JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    protected Expression doTransform(FunctionedExpression jqlExpression, ExpressionBuildingVariableResolver context) {
        Expression operand = jqlTransformers.transform(jqlExpression.getOperand(), context);
        C objectType;
        if (operand instanceof ObjectExpression) {
            objectType = (C) ((ObjectExpression) operand).getObjectType(getModelAdapter());
        } else if (operand instanceof CollectionExpression) {
            objectType = (C) ((CollectionExpression) operand).getObjectType(getModelAdapter());
        } else {
            objectType = null;
        }
        return jqlTransformers.applyFunctions(jqlExpression, operand, objectType, context);
    }
}
