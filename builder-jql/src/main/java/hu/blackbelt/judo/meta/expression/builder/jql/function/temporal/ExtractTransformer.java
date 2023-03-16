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

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.temporal.DatePart;
import hu.blackbelt.judo.meta.expression.temporal.TimePart;
import hu.blackbelt.judo.meta.expression.temporal.TimestampPart;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import java.time.temporal.ChronoUnit;

import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.*;

public class ExtractTransformer extends AbstractJqlFunctionTransformer<Expression> {

    private ChronoUnit chronoUnit;

    public ExtractTransformer(ExpressionTransformer jqlTransformers, ChronoUnit chronoUnit) {
        super(jqlTransformers);
        this.chronoUnit = chronoUnit;
    }

    @Override
    public Expression apply(Expression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        if (argument instanceof DateExpression) {
            return newExtractDateExpressionBuilder()
                    .withDate((DateExpression) argument)
                    .withPart(getDatePart())
                    .build();
        } else if (argument instanceof TimestampExpression) {
            return newExtractTimestampExpressionBuilder()
                    .withTimestamp((TimestampExpression) argument)
                    .withPart(getTimestampPart())
                    .build();
        } else if (argument instanceof TimeExpression) {
            return newExtractTimeExpressionBuilder()
                    .withTime((TimeExpression) argument)
                    .withPart(getTimePart())
                    .build();
        } else {
            throw new IllegalArgumentException("Unsupported argument type: " + argument);
        }
    }

    private DatePart getDatePart() {
        switch (chronoUnit) {
            case YEARS:
                return DatePart.YEAR;
            case MONTHS:
                return DatePart.MONTH;
            case DAYS:
                return DatePart.DAY;
        }
        throw new IllegalArgumentException("Unsupported date part: " + chronoUnit);
    }

    private TimestampPart getTimestampPart() {
        switch (chronoUnit) {
            case YEARS:
                return TimestampPart.YEAR;
            case MONTHS:
                return TimestampPart.MONTH;
            case DAYS:
                return TimestampPart.DAY;
            case HOURS:
                return TimestampPart.HOUR;
            case MINUTES:
                return TimestampPart.MINUTE;
            case SECONDS:
                return TimestampPart.SECOND;
            case MILLIS:
                return TimestampPart.MILLISECOND;
        }
        throw new IllegalArgumentException("Unsupported timestamp part: " + chronoUnit);
    }

    private TimePart getTimePart() {
        switch (chronoUnit) {
            case HOURS:
                return TimePart.HOUR;
            case MINUTES:
                return TimePart.MINUTE;
            case SECONDS:
                return TimePart.SECOND;
            case MILLIS:
                return TimePart.MILLISECOND;
        }
        throw new IllegalArgumentException("Unsupported time part: " + chronoUnit);
    }
}
