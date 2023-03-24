package hu.blackbelt.judo.meta.expression.builder.jql.operation;

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

import hu.blackbelt.judo.meta.expression.DecimalExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.IntegerExpression;
import hu.blackbelt.judo.meta.expression.LogicalExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.AbstractJqlExpressionTransformer;
import hu.blackbelt.judo.meta.jql.jqldsl.UnaryOperation;

import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newNegationExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalOppositeExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerOppositeExpressionBuilder;

public class JqlUnaryOperationTransformer<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> extends AbstractJqlExpressionTransformer<UnaryOperation, NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> {

    public JqlUnaryOperationTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    protected Expression doTransform(UnaryOperation unaryOperation, ExpressionBuildingVariableResolver context) {
        Expression operand = jqlTransformers.transform(unaryOperation.getOperand(), context);
        String operator = unaryOperation.getOperator();
        if (operand instanceof IntegerExpression) {
            if ("-".equals(operator)) {
                return newIntegerOppositeExpressionBuilder().withExpression((IntegerExpression) operand).build();
            }
            throw new UnsupportedOperationException("Invalid integer unary operation: " + operator);
        } else if (operand instanceof DecimalExpression) {
            if ("-".equals(operator)) {
                return newDecimalOppositeExpressionBuilder().withExpression((DecimalExpression) operand).build();
            }
            throw new UnsupportedOperationException("Invalid decimal unary operation: " + operator);
        } else if (operand instanceof LogicalExpression) {
            if ("not".equalsIgnoreCase(operator)) {
                return newNegationExpressionBuilder().withExpression((LogicalExpression) operand).build();
            }
            throw new UnsupportedOperationException("Invalid logical unary operation: " + operator);
        }
        throw new UnsupportedOperationException("Not supported operand type");
    }
}
