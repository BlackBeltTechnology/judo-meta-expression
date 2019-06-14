package hu.blackbelt.judo.meta.expression.adapters;

import hu.blackbelt.judo.meta.expression.*;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Expression model adapter.
 *
 * @param <NE>  namespace element
 * @param <P>   primitive
 * @param <PTE> primitive typed element (ie. attribute)
 * @param <E>   enumeration
 * @param <C>   class
 * @param <RTE> reference typed element (ie. reference)
 * @param <M>   measure
 * @param <U>   unit
 */
public interface ModelAdapter<NE, P, PTE, E, C, RTE, M, U> {

    Optional<TypeName> getTypeName(NE namespaceElement);

    /**
     * Get a namespace element by element name.
     *
     * @param elementName element name
     * @return namespace element
     */
    Optional<? extends NE> get(TypeName elementName);

    Optional<? extends M> get(MeasureName measureName);

    boolean isObjectType(NE namespaceElement);

    Optional<? extends RTE> getReference(C clazz, String referenceName);

    boolean isCollection(ReferenceExpression referenceExpression);

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

    boolean isMeasured(NumericExpression numericExpression);

    boolean contains(E enumeration, String memberName);

    Optional<M> getDurationMeasure();

    boolean isDurationSupportingAddition(U unit);

    Optional<M> getMeasure(NumericExpression numericExpression);

    Optional<U> getUnit(NumericExpression numericExpression);

    Optional<Map<M, Integer>> getDimension(NumericExpression numericExpression);
}
