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

    /**
     * Get type name (defined by expression metamodel) of a given namespace element (in underlying data model).
     *
     * @param namespaceElement namespace element
     * @return type name
     */
    Optional<TypeName> getTypeName(NE namespaceElement);

    /**
     * Get a namespace element by element name.
     *
     * @param elementName element name
     * @return namespace element
     */
    Optional<? extends NE> get(TypeName elementName);

    /**
     * Get measure by name as defined in expression metamodel.
     *
     * @param measureName measure name
     * @return measure
     */
    Optional<? extends M> get(MeasureName measureName);

    /**
     * Check of a given namespace element represents an object (in expression).
     *
     * @param namespaceElement namespace element
     * @return <code>true</code> if namespace element is an object (ie. entity type), <code>false</code> otherwise
     */
    boolean isObjectType(NE namespaceElement);

    /**
     * Get reference of a given (object) type by name.
     *
     * @param clazz         object type
     * @param referenceName reference name
     * @return reference
     */
    Optional<? extends RTE> getReference(C clazz, String referenceName);

    /**
     * Check multiplicity of a reference expression.
     *
     * @param referenceExpression reference expression
     * @return <code>true</code> if reference expression is collection, <code>false</code> otherwise
     */
    boolean isCollection(ReferenceExpression referenceExpression);

    /**
     * Get target (object) type of a reference.
     *
     * @param reference reference
     * @return object type
     */
    C getTarget(RTE reference);

    /**
     * Get attribute of a given (object) type by name.
     *
     * @param clazz         object type
     * @param attributeName attribute name
     * @return attribute
     */
    Optional<? extends PTE> getAttribute(C clazz, String attributeName);

    /**
     * Get (primitive) type of an attribute.
     *
     * @param clazz         object type
     * @param attributeName attribute name
     * @return attribute type
     */
    Optional<? extends P> getAttributeType(C clazz, String attributeName);

    /**
     * Get super types of a(n object) type. Empty collection is returned in case of no supertypes.
     *
     * @param clazz object type
     * @return super types
     */
    Collection<? extends C> getSuperTypes(C clazz);

    /**
     * Check if a primitive type is numeric.
     *
     * @param primitive primitive type
     * @return <code>true</code> if type is numeric, <code>false</code> otherwise
     */
    boolean isNumeric(P primitive);

    /**
     * Check if a primitive type is integer.
     *
     * @param primitive primitive type
     * @return <code>true</code> if type is integer, <code>false</code> otherwise
     */
    boolean isInteger(P primitive);

    /**
     * Check if a primitive type is decimal.
     *
     * @param primitive primitive type
     * @return <code>true</code> if type is decimal, <code>false</code> otherwise
     */
    boolean isDecimal(P primitive);

    /**
     * Check if a primitive type is boolean.
     *
     * @param primitive primitive type
     * @return <code>true</code> if type is boolean, <code>false</code> otherwise
     */
    boolean isBoolean(P primitive);

    /**
     * Check if a primitive type is string.
     *
     * @param primitive primitive type
     * @return <code>true</code> if type is string, <code>false</code> otherwise
     */
    boolean isString(P primitive);

    /**
     * Check if a primitive type is enumeration.
     *
     * @param primitive primitive type
     * @return <code>true</code> if type is enumeration, <code>false</code> otherwise
     */
    boolean isEnumeration(P primitive);

    /**
     * Check if a primitive type is date.
     *
     * @param primitive primitive type
     * @return <code>true</code> if type is date, <code>false</code> otherwise
     */
    boolean isDate(P primitive);

    /**
     * Check if a primitive type is timestamp.
     *
     * @param primitive primitive type
     * @return <code>true</code> if type is timestamp, <code>false</code> otherwise
     */
    boolean isTimestamp(P primitive);

    /**
     * Check if a primitive type is custom (has no build-in support by expressions).
     *
     * @param primitive primitive
     * @return <code>true</code> if type is custom, <code>false</code> otherwise
     */
    boolean isCustom(P primitive);

    /**
     * Check if a numeric expression is measured.
     *
     * @param numericExpression numeric expression
     * @return <code>true</code> if type is measured, <code>false</code> otherwise
     */
    boolean isMeasured(NumericExpression numericExpression);

    /**
     * Check if an enumeration contains a member (by name)
     * @param enumeration enumeration
     * @param memberName  member name
     * @return <code>true</code> if enumeration contains member, <code>false</code> otherwise
     */
    boolean contains(E enumeration, String memberName);

    /**
     * Get duration measure that can be used by architecture (ie. adding ms, s, etc. units is supported).
     *
     * @return duration measure
     */
    Optional<M> getDurationMeasure();

    /**
     * Check if a given unit is duration that supports addition (ie. adding ms, s, etc. to it).
     *
     * @param unit unit
     * @return <code>true</code> if unit is duration and supports addition of system units, <code>false</code> otherwise.
     */
    boolean isDurationSupportingAddition(U unit);

    /**
     * Get measure of a numeric expression.
     *
     * @param numericExpression numeric expression
     * @return measure
     */
    Optional<M> getMeasure(NumericExpression numericExpression);

    /**
     * Get unit of a numeric expression.
     *
     * @param numericExpression numeric expression
     * @return unit
     */
    Optional<U> getUnit(NumericExpression numericExpression);

    /**
     * Get dimension (map of base measured with exponents) of a numeric expression. Measure may not defined for it (ie. expression fragments that is not assigned).
     *
     * @param numericExpression numeric expression
     * @return map if base measured with exponents
     */
    Optional<Map<M, Integer>> getDimension(NumericExpression numericExpression);
}
