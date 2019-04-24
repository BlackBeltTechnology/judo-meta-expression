package hu.blackbelt.judo.meta.expression.runtime.adapters;

import hu.blackbelt.judo.meta.expression.ElementName;

import java.util.Collection;
import java.util.Optional;

public interface ModelAdapter<NE, P, PTE, E, C, RTE> {

    /**
     * Get a namespace element by element name.
     *
     * @param elementName element name
     * @return namespace element
     */
    Optional<? extends NE> get(ElementName elementName);

    Optional<? extends RTE> getReference(C clazz, String referenceName);

    boolean isCollection(RTE reference);

    C getTarget(RTE reference);

    Optional<? extends PTE> getAttribute(C clazz, String attributeName);

    Optional<? extends P> getAttributeType(C clazz, String attributeName);

    Collection<? extends C> getSuperTypes(C clazz);

    boolean isNumeric(P primitive);

    boolean isInteger(P primitive);

    boolean isDecimal(P primitive);

    boolean isBoolean(P primitive);

    boolean isString(P primitive);

    boolean isEnumeration(P primitive);

    boolean isDate(P primitive);

    boolean isTimestamp(P primitive);

    /**
     * Check if a primitive type is custom.
     *
     * @param primitive primitive
     * @return primitive type has no builtin support
     */
    boolean isCustom(P primitive);

    /**
     * Check if a primitive typed element (attribute, derived data property) is measured.
     *
     * @param primitiveTypedElement pritimive typed element
     * @return element is measured
     */
    boolean isMeasured(PTE primitiveTypedElement);

    boolean contains(E enumeration, String memberName);
}
