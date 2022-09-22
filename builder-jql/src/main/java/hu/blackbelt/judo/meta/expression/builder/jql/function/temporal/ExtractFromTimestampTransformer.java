package hu.blackbelt.judo.meta.expression.builder.jql.function.temporal;

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
import hu.blackbelt.judo.meta.expression.TimestampExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.temporal.TimestampPart;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newExtractDateOfTimestampExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newExtractTimeOfTimestampExpressionBuilder;

public class ExtractFromTimestampTransformer extends AbstractJqlFunctionTransformer<TimestampExpression> {

    private final TimestampPart timestampPart;

    public ExtractFromTimestampTransformer(ExpressionTransformer jqlTransformers, TimestampPart timestampPart) {
        super(jqlTransformers);
        this.timestampPart = timestampPart;
    }

    @Override
    public Expression apply(TimestampExpression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        if (TimestampPart.DATE.equals(timestampPart)) {
            return newExtractDateOfTimestampExpressionBuilder()
                    .withTimestamp(argument)
                    .build();
        } else if (TimestampPart.TIME.equals(timestampPart)) {
            return newExtractTimeOfTimestampExpressionBuilder()
                    .withTimestamp(argument)
                    .build();
        } else {
            throw new IllegalArgumentException("Unsupported timestamp part: " + timestampPart);
        }
    }

}
