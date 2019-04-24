package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableSet;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.data.PrimitiveTypedElement;
import hu.blackbelt.judo.meta.psm.data.ReferenceTypedElement;
import hu.blackbelt.judo.meta.psm.data.Relation;
import hu.blackbelt.judo.meta.psm.derived.DataProperty;
import hu.blackbelt.judo.meta.psm.derived.NavigationProperty;
import hu.blackbelt.judo.meta.psm.namespace.NamedElement;
import hu.blackbelt.judo.meta.psm.type.EnumerationType;
import hu.blackbelt.judo.meta.psm.type.Primitive;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PsmExpressionUtils {

    public static Optional<? extends ReferenceTypedElement> getReferenceType(final EntityType entityType, final String name) {
        final Optional<Optional<Relation>> relation = getEntityAndSuperType(entityType).stream()
                .filter(e -> e.getRelations().stream().anyMatch(r -> Objects.equals(name, r.getName()))) // get all types containing reference named name
                .map(e -> e.getRelations().stream().filter(r -> Objects.equals(name, r.getName())).findFirst()) // get references of types
                .findFirst(); // redefining references is not supported
        final Optional<Optional<NavigationProperty>> navigationProperty = getEntityAndSuperType(entityType).stream()
                .filter(e -> e.getNavigationProperties().stream().anyMatch(n -> Objects.equals(name, n.getName()))) // get all types containing navigation property named name
                .map(e -> e.getNavigationProperties().stream().filter(n -> Objects.equals(name, n.getName())).findFirst()) // get navigation properties of types
                .findFirst(); // redefining navigation properties is not supported

        if (relation.isPresent()) {
            return relation.get();
        } else if (navigationProperty.isPresent()) {
            return navigationProperty.get();
        } else {
            return Optional.empty();
        }
    }

    public static boolean isCollection(final ReferenceTypedElement referenceTypedElement) {
        if (referenceTypedElement.getCardinality() == null) {
            return false;
        } else {
            return referenceTypedElement.getCardinality().getUpper() == -1 || referenceTypedElement.getCardinality().getUpper() > 1;
        }
    }

    public static EntityType getTarget(final ReferenceTypedElement referenceTypedElement) {
        return referenceTypedElement.getTarget();
    }

    public static Optional<? extends PrimitiveTypedElement> getAttribute(final EntityType entityType, final String name) {
        final Optional<Optional<Attribute>> attribute = getEntityAndSuperType(entityType).stream()
                .filter(e -> e.getAttributes().stream().anyMatch(a -> Objects.equals(name, a.getName()))) // get all types containing attribute named name
                .map(e -> e.getAttributes().stream().filter(a -> Objects.equals(name, a.getName())).findFirst()) // get attributes of types
                .findFirst(); // redefining attributes is not supported
        final Optional<Optional<DataProperty>> dataProperty = getEntityAndSuperType(entityType).stream()
                .filter(e -> e.getDataProperties().stream().anyMatch(d -> Objects.equals(name, d.getName()))) // get all types containing data property named name
                .map(e -> e.getDataProperties().stream().filter(d -> Objects.equals(name, d.getName())).findFirst()) // get data properties of types
                .findFirst(); // redefining data properties is not supported

        if (attribute.isPresent()) {
            return attribute.get();
        } else if (dataProperty.isPresent()) {
            return dataProperty.get();
        } else {
            return Optional.empty();
        }
    }

    public static Optional<? extends Primitive> getAttributeType(final EntityType entityType, final String attributeName) {
        final Optional<? extends PrimitiveTypedElement> attribute = getAttribute(entityType, attributeName);

        if (attribute.isPresent()) {
            return Optional.of(attribute.get().getDataType());
        } else {
            return Optional.empty();
        }
    }

    public static Set<EntityType> getEntityAndSuperType(final EntityType entityType) {
        final Set<EntityType> result = new HashSet<>(getSuperTypes(entityType));
        result.add(entityType);
        return ImmutableSet.copyOf(result);
    }

    public static Set<EntityType> getSuperTypes(final EntityType entityType) {
        final Set<EntityType> result = new HashSet<>();
        addSuperTypes(entityType, result);
        return ImmutableSet.copyOf(result);
    }

    static void addSuperTypes(final EntityType entityType, final Set<EntityType> foundSuperTypes) {
        final Set<EntityType> newSuperTypes = entityType.getSuperEntityTypes().stream().filter(s -> !foundSuperTypes.contains(s)).collect(Collectors.toSet());
        foundSuperTypes.addAll(newSuperTypes);

        newSuperTypes.forEach(s -> addSuperTypes(s, foundSuperTypes));
    }

//    public static boolean isCustom(final DataType dataType) {
//        // TODO
//    }

    public static boolean contains(final EnumerationType enumerationType, final String name) {
        return enumerationType.getMembers().stream().filter(m -> Objects.equals(name, m.getName())).count() > 0;
    }

    public static String getName(final NamedElement namedElement) {
        return namedElement.getName();
    }
}
