package hu.blackbelt.judo.meta.expression.adapters;

import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.ReferenceSelector;
import hu.blackbelt.judo.meta.expression.TypeName;
import org.eclipse.emf.common.util.EList;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Expression model adapter. This interface is used to evaluate expressions on specific metamodels.
 *
 * @param <NE>  namespace element
 * @param <P>   primitive
 * @param <PTE> primitive typed element (ie. attribute)
 * @param <E>   enumeration
 * @param <C>   class
 * @param <AP>  transfer object type
 * @param <RTE> reference typed element (ie. reference)
 * @param <S> sequence
 * @param <M>   measure
 * @param <U>   unit
 */
public interface ModelAdapter<NE, P extends NE, PTE, E extends P, C extends NE, AP extends NE, RTE, S, M, U> {

    /**
     * Get type name (defined by expression metamodel) of a given namespace element (in underlying data model). Type name
     * won't be added to any resource.
     *
     * @param namespaceElement namespace element
     * @return type name
     */
    Optional<TypeName> buildTypeName(NE namespaceElement);

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
     * Check multiplicity of a reference selector.
     *
     * @param referenceSelector reference selector
     * @return <code>true</code> if reference expression is collection, <code>false</code> otherwise
     */
    boolean isCollection(ReferenceSelector referenceSelector);

    /**
     * Check multiplicity of a reference typed element.
     *
     * @param reference reference
     * @return <code>true</code> if reference is collection, <code>false</code> otherwise
     */
    boolean isCollectionReference(RTE reference);

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
     * @param attribute     attribute
     * @return attribute type
     */
    Optional<? extends P> getAttributeType(PTE attribute);

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
     * Get units of a measure.
     * @param measure
     * @return List of units.
     */
    EList<U> getUnits(M measure);

    /**
     * Get dimension (map of base measured with exponents) of a numeric expression. Measure may not defined for it (ie. expression fragments that is not assigned).
     *
     * @param numericExpression numeric expression
     * @return map if base measured with exponents
     */
    Optional<Map<M, Integer>> getDimension(NumericExpression numericExpression);

    /**
     * Get all element names that can be used as container of clazz.
     *
     * @param clazz containment class
     * @return all possible container classes
     */
    EList<C> getContainerTypesOf(C clazz);

    /**
     * Get all classes that can be types in expression.
     *
     * @return list of classes
     */
    EList<C> getAllEntityTypes();

    /** Returns all measures that can be used in expression.
     *
     * @return list of measures
     */
    EList<M> getAllMeasures();

    Optional<MeasureName> buildMeasureName(M measure);

    EList<E> getAllEnums();

    EList<NE> getAllStaticSequences();

    EList<AP> getAllTransferObjectTypes();

    /**
     * Get object sequence by name.
     *
     * @param clazz         object type
     * @param sequenceName attribute name
     * @return the sequence
     */
    Optional<? extends S> getSequence(C clazz, String sequenceName);

    boolean isSequence(NE namespaceElement);

    boolean isDerivedAttribute(PTE attribute);

    Optional<String> getAttributeGetter(PTE attribute);

    boolean isDerivedReference(RTE reference);

    Optional<String> getReferenceGetter(RTE reference);
    
    /**
     * Get the referenced (object) type. Returns the referenced class in case of referenced typed element, and returns the mapped class in case of transfer relation.
     *
     * @param elementName type name
     * @param referenceName name of referenced typed element or transfer relation
     * @return class
     */
    Optional<C> getEntityTypeOfTransferObjectRelationTarget(TypeName transferObjectTypeName, String transferObjectRelationName);
    
    /**
     * Check multiplicity of the reference of the given element.
     *
     * @param elementName type name
     * @param referenceName name of referenced typed element or transfer relation
     * @return class
     */
    boolean isCollectionReference(TypeName elementName, String referenceName);
}
