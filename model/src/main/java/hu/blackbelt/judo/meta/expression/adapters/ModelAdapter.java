package hu.blackbelt.judo.meta.expression.adapters;

import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.NavigationExpression;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.TypeName;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public interface ModelAdapter<NE, P, PTE, E, C, RTE, M, U> {

    Pattern MEASURE_NAME_PATTERN = Pattern.compile("^(.*)\\.([^\\.]+)$");

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

    boolean isCollection(NavigationExpression navigationExpression);

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

    boolean isMeasured(NumericExpression numericExpression);

    boolean contains(E enumeration, String memberName);

    Optional<M> getDurationMeasure();

    boolean isSupportingAddition(U unit);

    Optional<M> getMeasure(NumericExpression numericExpression);

    Optional<U> getUnit(NumericExpression numericExpression);

    Optional<Map<M, Integer>> getDimension(NumericExpression numericExpression);
}
