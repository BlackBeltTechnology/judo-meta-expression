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

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.AbstractJqlExpressionTransformer;
import hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionSwitchExpressionBuilder;
import hu.blackbelt.judo.meta.expression.enumeration.util.builder.EnumerationSwitchExpressionBuilder;
import hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders;
import hu.blackbelt.judo.meta.expression.object.util.builder.ObjectSwitchExpressionBuilder;
import hu.blackbelt.judo.meta.expression.util.builder.SwitchCaseBuilder;
import hu.blackbelt.judo.meta.jql.jqldsl.TernaryOperation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.util.*;
import java.util.function.BiFunction;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newCollectionSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.custom.util.builder.CustomBuilders.newCustomSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.enumeration.util.builder.EnumerationBuilders.newEnumerationSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newLogicalSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.*;

public class JqlTernaryOperationTransformer<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> extends AbstractJqlExpressionTransformer<TernaryOperation, NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> {

    private final Map<Class<? extends Expression>, BiFunction<SwitchCase, Expression, SwitchExpression>> supportedTypes = new HashMap<Class<? extends Expression>, BiFunction<SwitchCase, Expression, SwitchExpression>>() {
        {
            put(IntegerExpression.class, (thenCase, elseExpression) -> newIntegerSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
            put(DecimalExpression.class, (thenCase, elseExpression) -> newDecimalSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
            put(CollectionExpression.class, (thenCase, elseExpression) -> {
                CollectionSwitchExpressionBuilder builder = newCollectionSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression);
                Expression thenExpression = thenCase.getExpression();
                C collectionType = getCommonAncestor((CollectionExpression) thenExpression, (CollectionExpression) elseExpression);
                TypeName typeName = getModelAdapter().buildTypeName(collectionType).get();
                builder.withElementName(typeName);
                return builder.build();

            });
            put(CustomExpression.class, (thenCase, elseExpression) -> newCustomSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
            put(EnumerationExpression.class, (thenCase, elseExpression) -> {
                EnumerationSwitchExpressionBuilder builder = newEnumerationSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression);
                Object enumeration = ((EnumerationExpression) elseExpression).getEnumeration(getModelAdapter());
                return builder.withElementName((TypeName) enumeration).build();
            });
            put(DateExpression.class, (thenCase, elseExpression) -> newDateSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
            put(ObjectExpression.class, (thenCase, elseExpression) -> {
                ObjectSwitchExpressionBuilder builder = newObjectSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression);
                Expression thenExpression = thenCase.getExpression();
                C collectionType = getCommonAncestor((ObjectExpression) thenExpression, (ObjectExpression) elseExpression);
                TypeName typeName = getModelAdapter().buildTypeName(collectionType).get();
                builder.withElementName(typeName);
                return builder.build();
            });
            put(StringExpression.class, (thenCase, elseExpression) -> newStringSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
            put(TimestampExpression.class, (thenCase, elseExpression) -> newTimestampSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
            put(TimeExpression.class, (thenCase, elseExpression) -> newTimeSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
            put(LogicalExpression.class, (thenCase, elseExpression) -> newLogicalSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
        }
    };

    public JqlTernaryOperationTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    protected Expression doTransform(TernaryOperation ternaryOperation, ExpressionBuildingVariableResolver context) {
        Expression condition = jqlTransformers.transform(ternaryOperation.getCondition(), context);
        Expression thenExpression = jqlTransformers.transform(ternaryOperation.getThenExpression(), context);
        Expression elseExpression = jqlTransformers.transform(ternaryOperation.getElseExpression(), context);
        validateExpressionTypes(condition);
        SwitchCase thenCase = SwitchCaseBuilder.create().withCondition((LogicalExpression) condition).withExpression(thenExpression).build();
        BiFunction<SwitchCase, Expression, SwitchExpression> resultBuilder = findResultType(thenExpression, elseExpression);
        return resultBuilder.apply(thenCase, elseExpression);
    }

    private void validateExpressionTypes(Expression condition) {
        if (!(condition instanceof LogicalExpression)) {
            throw new IllegalArgumentException("Not a logical expression: " + condition);
        }
    }

    private BiFunction<SwitchCase, Expression, SwitchExpression> findResultType(Expression thenExpression, Expression elseExpression) {
        if (thenExpression instanceof NumericExpression && elseExpression instanceof NumericExpression && (thenExpression instanceof DecimalExpression || elseExpression instanceof DecimalExpression)) {
            return supportedTypes.get(DecimalExpression.class);
        }
        for (Map.Entry<Class<? extends Expression>, BiFunction<SwitchCase, Expression, SwitchExpression>> supportedTypeEntry : supportedTypes.entrySet()) {
            if (supportedTypeEntry.getKey().isAssignableFrom(thenExpression.getClass())) {
                if (!supportedTypeEntry.getKey().isAssignableFrom(elseExpression.getClass())) {
                    throw new IllegalArgumentException(String.format("THEN and ELSE part type differs: %s vs %s", thenExpression.getClass().getName(), elseExpression.getClass().getName()));
                } else {
                    if (supportedTypeEntry.getKey().isAssignableFrom(EnumerationExpression.class)) {
                        validateEnumTypes((EnumerationExpression) thenExpression, (EnumerationExpression) elseExpression);
                    }
                    return supportedTypeEntry.getValue();
                }
            }
        }
        throw new IllegalArgumentException("Not supported result type for ternary operation: " + thenExpression.getClass());
    }

    private C getCommonAncestor(ReferenceExpression thenExpression, ReferenceExpression elseExpression) {
        C thenType = (C) thenExpression.getObjectType(getModelAdapter());
        C elseType = (C) elseExpression.getObjectType(getModelAdapter());
        if (thenType.equals(elseType)) {
            return thenType;
        }
        if (jqlTransformers.getExpressionBuilder().isKindOf(thenType, elseType)) {
            return elseType;
        } else if (jqlTransformers.getExpressionBuilder().isKindOf(elseType, thenType)) {
            return thenType;
        }
        // Determine least upper bound, ie. a single shared supertype that is more specific than any other shared supertype
        List<C> thenSupertypes = new LinkedList<>();
        thenSupertypes.addAll(getModelAdapter().getSuperTypes(thenType));
        int index = 0;
        while (index < thenSupertypes.size()) {
            for (C superType : getModelAdapter().getSuperTypes(thenSupertypes.get(index))) {
                if (!thenSupertypes.contains(superType)) {
                    thenSupertypes.add(superType);
                }
            }
            index++;
        }
        List<C> elseSupertypes = new LinkedList<>();
        elseSupertypes.addAll(getModelAdapter().getSuperTypes(elseType));
        index = 0;
        while (index < elseSupertypes.size()) {
            for (C superType : getModelAdapter().getSuperTypes(elseSupertypes.get(index))) {
                if (!elseSupertypes.contains(superType)) {
                    elseSupertypes.add(superType);
                }
            }
            index++;
        }
        Set<C> commonSupertypes = new HashSet<>(thenSupertypes);
        commonSupertypes.retainAll(elseSupertypes);
        // find those supertypes which are supertypes of others
        Set<C> alreadySupertypes = new LinkedHashSet<>();
        for (C commonSuperType : commonSupertypes) {
            Set<C> otherTypes = new LinkedHashSet<>(commonSupertypes);
            otherTypes.remove(commonSuperType);
            for (C otherType : otherTypes) {
                if (jqlTransformers.getExpressionBuilder().isKindOf(commonSuperType, otherType)) {
                    alreadySupertypes.add(otherType);
                }
            }
        }
        commonSupertypes.removeAll(alreadySupertypes);
        if (commonSupertypes.size() > 1) {
            throw new IllegalArgumentException("More than one common supertypes: " + commonSupertypes);
        } else if (commonSupertypes.size() == 0) {
            throw new IllegalArgumentException(String.format("No common supertype for %s, %s", thenExpression, elseExpression));
        } else {
            return commonSupertypes.stream().findAny().get();
        }
    }

    private void validateEnumTypes(EnumerationExpression thenExpression, EnumerationExpression elseExpression) {
        Object thenEnum = thenExpression.getEnumeration(getModelAdapter());
        Object elseEnum = elseExpression.getEnumeration(getModelAdapter());
        if (!EcoreUtil.equals((EObject) thenEnum, (EObject) elseEnum)) {
            throw new IllegalArgumentException(String.format("THEN and ELSE part different enumerations: %s vs %s", thenEnum, elseEnum));
        }
    }

}
