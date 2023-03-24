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

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;

public abstract class AbstractJqlExpressionTransformer<T extends JqlExpression, NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> implements JqlExpressionTransformerFunction {

    protected final JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers;

    protected AbstractJqlExpressionTransformer(JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers) {
        this.jqlTransformers = jqlTransformers;
    }

    @SuppressWarnings("unchecked")
    public final Expression apply(JqlExpression jqlExpression, ExpressionBuildingVariableResolver context) {
        return doTransform((T) jqlExpression, context);
    }

    protected abstract Expression doTransform(T jqlExpression, ExpressionBuildingVariableResolver context);

    protected ModelAdapter<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> getModelAdapter() {
        return jqlTransformers.getModelAdapter();
    }


}
