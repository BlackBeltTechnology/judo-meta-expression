package hu.blackbelt.judo.meta.expression.builder.jql.operation;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.AbstractJqlExpressionTransformer;
import hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionSwitchExpressionBuilder;
import hu.blackbelt.judo.meta.expression.enumeration.util.builder.EnumerationSwitchExpressionBuilder;
import hu.blackbelt.judo.meta.expression.object.util.builder.ObjectSwitchExpressionBuilder;
import hu.blackbelt.judo.meta.expression.util.builder.SwitchCaseBuilder;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.TernaryOperation;

import java.util.*;
import java.util.function.BiFunction;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newCollectionSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.custom.util.builder.CustomBuilders.newCustomSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.enumeration.util.builder.EnumerationBuilders.newEnumerationSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newDateSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampSwitchExpressionBuilder;

public class JqlTernaryOperationTransformer<NE, P, PTE, E extends NE, C extends NE, RTE, S, M, U> extends AbstractJqlExpressionTransformer<TernaryOperation, NE, P, PTE, E, C, RTE, S, M, U> {

    private final Map<Class<? extends Expression>, BiFunction<SwitchCase, Expression, SwitchExpression>> supportedTypes = new HashMap<Class<? extends Expression>, BiFunction<SwitchCase, Expression, SwitchExpression>>() {
        {
            put(IntegerExpression.class, (thenCase, elseExpression) -> newIntegerSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
            put(DecimalExpression.class, (thenCase, elseExpression) -> newDecimalSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
            put(CollectionExpression.class, (thenCase, elseExpression) -> {
                CollectionSwitchExpressionBuilder builder = newCollectionSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression);
                Expression thenExpression = thenCase.getExpression();
                C collectionType = getCommonAncestor((CollectionExpression) thenExpression, (CollectionExpression) elseExpression);
                TypeName typeName = getModelAdapter().getTypeName(collectionType).get();
                builder.withElementName(jqlTransformers.getTypeNameFromResource(typeName.getNamespace(), typeName.getName()));
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
                TypeName typeName = getModelAdapter().getTypeName(collectionType).get();
                builder.withElementName(jqlTransformers.getTypeNameFromResource(typeName.getNamespace(), typeName.getName()));
                return builder.build();
            });
            put(StringExpression.class, (thenCase, elseExpression) -> newStringSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
            put(TimestampExpression.class, (thenCase, elseExpression) -> newTimestampSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
        }
    };

    public JqlTernaryOperationTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);

    }

    @Override
    protected Expression doTransform(TernaryOperation ternaryOperation, List<ObjectVariable> variables) {
        Expression condition = jqlTransformers.transform(ternaryOperation.getCondition(), variables);
        Expression thenExpression = jqlTransformers.transform(ternaryOperation.getThenExpression(), variables);
        Expression elseExpression = jqlTransformers.transform(ternaryOperation.getElseExpression(), variables);
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
        if (isKindOf(thenType, elseType)) {
            return elseType;
        } else if (isKindOf(elseType, thenType)) {
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
                if (isKindOf(commonSuperType, otherType)) {
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

    private boolean isKindOf(C c1, C c2) {
        Set<C> checkedSuperTypes = new LinkedHashSet<>();
        Collection<? extends C> superTypes = getModelAdapter().getSuperTypes(c1);
        checkedSuperTypes.addAll(superTypes);
        for (C c : checkedSuperTypes) {
            if (c.equals(c2)) {
                return true;
            } else {
                checkedSuperTypes.addAll(getModelAdapter().getSuperTypes(c));
            }
        }
        return false;
    }

    private void validateEnumTypes(EnumerationExpression thenExpression, EnumerationExpression elseExpression) {
        Object thenEnum = thenExpression.getEnumeration(getModelAdapter());
        Object elseEnum = elseExpression.getEnumeration(getModelAdapter());
        if (!Objects.equals(thenEnum, elseEnum)) {
            throw new IllegalArgumentException(String.format("THEN and ELSE part different enumerations: %s vs %s", thenEnum, elseEnum));
        }
    }

}
