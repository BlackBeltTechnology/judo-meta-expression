package hu.blackbelt.judo.meta.expression.builder.jql.constant;

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
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlExpressionTransformerFunction;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.TimeStampLiteral;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newTimestampConstantBuilder;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class JqlTimestampLiteralTransformer implements JqlExpressionTransformerFunction {

    @Override
    public Expression apply(JqlExpression expression, ExpressionBuildingVariableResolver context) {
        TimeStampLiteral timestampLiteral = (TimeStampLiteral) expression;
        String stringValue = timestampLiteral.getValue();
        // convert timestamp constant with offset to UTC
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(stringValue, DateTimeFormatter.ISO_OFFSET_DATE_TIME).atZoneSameInstant(ZoneOffset.UTC.normalized()).toOffsetDateTime();
        return newTimestampConstantBuilder().withValue(offsetDateTime).build();
    }

}
