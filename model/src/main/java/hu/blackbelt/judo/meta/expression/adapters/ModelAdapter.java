package hu.blackbelt.judo.meta.expression.adapters;

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

import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.ReferenceSelector;
import hu.blackbelt.judo.meta.expression.TypeName;
import org.eclipse.emf.common.util.EList;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Expression model adapter. This interface is used to evaluate expressions on specific metamodels.
 *
 * @param <NE>  namespace element
 * @param <P>   primitive
 * @param <E>   enumeration
 * @param <C>   entity type
 * @param <PTE> primitive typed element (ie. attribute)
 * @param <RTE> reference typed element (ie. reference)
 * @param <TO>  transfer object type
 * @param <TA> transfer attribute
 * @param <TR> transfer relation
 * @param <S> sequence
 * @param <M>   measure
 * @param <U>   unit
 */
public interface ModelAdapter<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> {

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

    boolean isPrimitiveType(NE namespaceElement);

    boolean isMeasuredType(P primitiveType);

    Optional<? extends M> getMeasureOfType(P primitiveType);

    Optional<U> getUnitOfType(P primitiveType);

    String getUnitName(U unit);

    UnitFraction getUnitRates(U unit);

    /**
     *  Find the correct ratio between the base unit of the given unit's measure and a duration type.
     *  E.g. when the given unit is minute (ratio is 60/1 regarding to base unit), and targetType is SECOND, 1/1 will be returned.
     *  When given unit is minute and targetType is DAY, 1/86400 will be returned.
     * @param unit The duration unit for which we look for a ratio.
     * @param targetType Must be either SECOND or DAY.
     * @return The ratio between the <i>base unit</i> of the given unit's measure and the given target type.
     */
    UnitFraction getBaseDurationRatio(U unit, DurationType targetType);

    /**
     * Get reference of a given (object) type by name.
     *
     * @param clazz         object type
     * @param referenceName reference name
     * @return reference
     */
    Optional<? extends RTE> getReference(C clazz, String referenceName);

    Optional<? extends TR> getTransferRelation(TO transferObject, String relationName);

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
     * Get parameter of primitive typed element.
     *
     * @param attribute primitive typed element
     * @return parameter type
     */
    Optional<TO> getAttributeParameterType(PTE attribute);

    /**
     * Get parameter of reference typed element.
     *
     * @param reference reference
     * @return parameter type
     */
    Optional<TO> getReferenceParameterType(RTE reference);

    /**
     * Get parameter of transfer attribute.
     *
     * @param attribute transfer attribute
     * @return parameter type
     */
    Optional<TO> getTransferAttributeParameterType(TA attribute);

    /**
     * Get parameter of transfer relation typed element.
     *
     * @param reference transfer relation
     * @return parameter type
     */
    Optional<TO> getTransferRelationParameterType(TR reference);

    /**
     * Get target (object) type of a reference.
     *
     * @param reference reference
     * @return object type
     */
    C getTarget(RTE reference);

    TO getTransferRelationTarget(TR relation);

    /**
     * Get attribute of a given (object) type by name.
     *
     * @param clazz         object type
     * @param attributeName attribute name
     * @return attribute
     */
    Optional<? extends PTE> getAttribute(C clazz, String attributeName);

    Optional<? extends TA> getTransferAttribute(TO transferObject, String attributeName);

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
     * Check if a given mixin transfer object type includes another transfer object type.
     *
     * @param included included transfer object type
     * @param mixin    mixin transfer object type
     * @return <code>true</code> if mixin is valid, <code>false</code> otherwise
     */
    boolean isMixin(TO included, TO mixin);

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
     * Check if a primitive type is time.
     *
     * @param primitive primitive type
     * @return <code>true</code> if type is time, <code>false</code> otherwise
     */
    boolean isTime(P primitive);

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

    EList<P> getAllPrimitiveTypes();

    EList<NE> getAllStaticSequences();

    EList<TO> getAllTransferObjectTypes();

    EList<TO> getAllMappedTransferObjectTypes();

    EList<TO> getAllUnmappedTransferObjectTypes();

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

    boolean isDerivedTransferAttribute(TA attribute);

    Optional<String> getAttributeGetter(PTE attribute);

    Optional<String> getTransferAttributeGetter(TA attribute);

    Optional<String> getAttributeSetter(PTE attribute);

    Optional<String> getAttributeDefault(PTE attribute);

    boolean isDerivedReference(RTE reference);

    boolean isDerivedTransferRelation(TR relation);

    Optional<String> getReferenceGetter(RTE reference);

    Optional<String> getTransferRelationGetter(TR relation);

    Optional<String> getReferenceDefault(RTE reference);

    Optional<String> getReferenceRange(RTE reference);

    Optional<String> getReferenceSetter(RTE reference);

    Optional<String> getTransferRelationSetter(TR relation);

    Optional<String> getFilter(TO transferObjectType);

        /**
         * Get the referenced (object) type. Returns the referenced class in case of referenced typed element, and returns the mapped class in case of transfer relation.
         *
         * @param elementName type name
         * @param referenceName name of referenced typed element or transfer relation
         * @return class
         */
    Optional<C> getEntityTypeOfTransferObjectRelationTarget(TypeName elementName, String referenceName);

    /**
     * Check multiplicity of the reference of the given element.
     *
     * @param elementName type name
     * @param referenceName name of referenced typed element or transfer relation
     * @return class
     */
    boolean isCollectionReference(TypeName elementName, String referenceName);

    Optional<C> getMappedEntityType(TO mappedTransferObjectType);

    String getFqName(Object object);

    Optional<String> getName(Object object);

    Collection<? extends PTE> getAttributes(C clazz);

    Collection<? extends RTE> getReferences(C clazz);

    Collection<? extends TA> getTransferAttributes(TO transferObjectType);

    Collection<? extends TR> getTransferRelations(TO transferObjectType);

    P getTransferAttributeType(TA transferAttribute);

    List<NE> getAllActorTypes();

    TO getPrincipal(NE actorType);

    enum DurationType {
        NANOSECOND(1, 1_000_000_000, 1, 86_400_000_000_000L),
        MICROSECOND(1, 1000_000, 1, 86_400_000_000L),
        MILLISECOND(1, 1000, 1, 86_400_000),
        SECOND(1, 1, 1, 86_400),
        MINUTE(60, 1, 1, 1440),
        HOUR(3600, 1, 1, 24),
        DAY(86400, 1, 1, 1),
        WEEK(604800, 1, 7, 1);

        private final UnitFraction secondUnitFraction;
        private final UnitFraction dayUnitFraction;

        DurationType(long secondDividend, long secondDivisor, long dayDividend, long dayDivisor) {
            secondUnitFraction = new UnitFraction(BigDecimal.valueOf(secondDividend), BigDecimal.valueOf(secondDivisor));
            dayUnitFraction = new UnitFraction(BigDecimal.valueOf(dayDividend), BigDecimal.valueOf(dayDivisor));
        }

        public UnitFraction getSecondUnitFraction() {
            return secondUnitFraction;
        }

        public UnitFraction getDayUnitFraction() {
            return dayUnitFraction;
        }
    }

    class UnitFraction {
        private final BigDecimal dividend;
        private final BigDecimal divisor;

        public UnitFraction(BigDecimal unitDividend, BigDecimal unitDivisor) {
            this.dividend = unitDividend;
            this.divisor = unitDivisor;
        }

        public BigDecimal getDividend() {
            return dividend;
        }

        public BigDecimal getDivisor() {
            return divisor;
        }
    }
}
