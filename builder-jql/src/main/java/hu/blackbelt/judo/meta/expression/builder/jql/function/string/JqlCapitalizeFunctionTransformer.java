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
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.math.BigInteger;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newIntegerConstantBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newLengthBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.*;

public class JqlCapitalizeFunctionTransformer extends AbstractJqlFunctionTransformer<Expression> {

    public JqlCapitalizeFunctionTransformer(ExpressionTransformer jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(Expression expression, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        if (!(expression instanceof StringExpression)) {
            throw new IllegalArgumentException("Capitalize is not supported on " + expression.getClass().getSimpleName());
        }

        // if the string expression is more complex this solution could be slower than expected
        final StringExpression expressionForSubstringLeft = (StringExpression) expression;
        final StringExpression expressionForSubstringRight = EcoreUtil.copy((StringExpression) expression);
        final StringExpression expressionForLength = EcoreUtil.copy((StringExpression) expression);

        // because of undefined rules, strings to be capitalized does not course error if those are undefined, empty or contain only 1 character.
        return newConcatenateBuilder()
                .withLeft(newUpperCaseBuilder()
                                  .withExpression(newSubStringBuilder()
                                                          .withExpression(expressionForSubstringLeft)
                                                          .withPosition(newIntegerConstantBuilder().withValue(BigInteger.valueOf(1)).build())
                                                          .withLength(newIntegerConstantBuilder().withValue(BigInteger.valueOf(1)).build())
                                                          .build())
                                  .build())
                .withRight(newLowerCaseBuilder()
                                   .withExpression(newSubStringBuilder()
                                                           .withExpression(expressionForSubstringRight)
                                                           .withPosition(newIntegerConstantBuilder().withValue(BigInteger.valueOf(2)).build())
                                                           .withLength(newLengthBuilder().withExpression(expressionForLength).build()) // overflow allowed
                                                           .build())
                                   .build())
                .build();
    }

}
