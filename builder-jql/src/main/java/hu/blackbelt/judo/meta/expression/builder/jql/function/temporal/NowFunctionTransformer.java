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
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.TypeNameExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newStringConstantBuilder;
import static hu.blackbelt.judo.meta.expression.variable.util.builder.VariableBuilders.*;

public class NowFunctionTransformer<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> extends AbstractJqlFunctionTransformer<TypeNameExpression> {

    public static final String SYSTEM_CATEGORY = "SYSTEM";
    public static final String CURRENT_DATE_VARIABLE_NAME = "current_date";
    public static final String CURRENT_TIMESTAMP_VARIABLE_NAME = "current_timestamp";
    public static final String CURRENT_TIME_VARIABLE_NAME = "current_time";

    private JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers;

    public NowFunctionTransformer(JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers) {
        super(jqlTransformers);
        this.jqlTransformers = jqlTransformers;
    }


    @Override
    public Expression apply(TypeNameExpression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        if (functionCall.getParameters().size() != 0) {
            throw new IllegalArgumentException("No argument expected");
        }
        NE ne = jqlTransformers.getModelAdapter().get(argument).get();
        TypeName typeName = jqlTransformers.buildTypeName(argument.getNamespace(), argument.getName());
        if (jqlTransformers.getModelAdapter().isPrimitiveType(ne)) {
            P primitiveType = (P) ne;
            if (jqlTransformers.getModelAdapter().isDate(primitiveType)) {
                return newDateEnvironmentVariableBuilder()
                        .withCategory(SYSTEM_CATEGORY)
                        .withVariableName(newStringConstantBuilder().withValue(CURRENT_DATE_VARIABLE_NAME).build())
                        .withTypeName(typeName)
                        .build();
            } else if (jqlTransformers.getModelAdapter().isTimestamp(primitiveType)) {
                return newTimestampEnvironmentVariableBuilder()
                        .withCategory(SYSTEM_CATEGORY)
                        .withVariableName(newStringConstantBuilder().withValue(CURRENT_TIMESTAMP_VARIABLE_NAME).build())
                        .withTypeName(typeName)
                        .build();
            } else if (jqlTransformers.getModelAdapter().isTime(primitiveType)) {
                return newTimeEnvironmentVariableBuilder()
                        .withCategory(SYSTEM_CATEGORY)
                        .withVariableName(newStringConstantBuilder().withValue(CURRENT_TIME_VARIABLE_NAME).build())
                        .withTypeName(typeName)
                        .build();
            } else {
                throw new IllegalArgumentException("Function is not valid for given type");
            }
        } else {
            throw new IllegalArgumentException("Type is not valid for environment variable");
        }
    }
}
