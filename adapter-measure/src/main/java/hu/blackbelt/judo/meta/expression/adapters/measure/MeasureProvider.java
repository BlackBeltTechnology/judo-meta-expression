package hu.blackbelt.judo.meta.expression.adapters.measure;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Measure provider of underlying (ie. PSM or measure) metamodel.
 *
 * @param <M> measure
 * @param <U> unit
 */
public interface MeasureProvider<M, U> {

    /**
     * Get namespace of a given measure.
     *
     * @param measure measure
     * @return measure namespace
     */
    String getMeasureNamespace(M measure);

    /**
     * Get name of a given measure.
     *
     * @param measure measure
     * @return measure name
     */
    String getMeasureName(M measure);

    /**
     * Get measure with name in a given namespace.
     *
     * @param namespace measure namespace
     * @param name      measure name
     * @return measure
     */
    Optional<M> getMeasure(String namespace, String name);

    /**
     * Get base of a given measure.
     * <p>
     * Base is a map in which keys are base measures and values are exponents.
     *
     * @param measure measure
     * @return base measures with exponents
     */
    Map<M, Integer> getBaseMeasures(M measure);

    /**
     * Get units of a given measure.
     *
     * @param measure measure
     * @return collection of units
     */
    Collection<U> getUnits(M measure);

    /**
     * Check if a given unit is duration supporting addition (ie. second, day, etc.).
     * <p>
     * Month and year is not additive!
     *
     * @param unit unit
     * @return <code>true</code> if unit is an additive duration, <code>false</code> otherwise
     */
    boolean isDurationSupportingAddition(U unit);

    /**
     * Get unit by name or symbol.
     *
     * @param measure      measure
     * @param nameOrSymbol unit name or symbol
     * @return unit
     */
    Optional<U> getUnitByNameOrSymbol(Optional<M> measure, String nameOrSymbol);

    /**
     * Get stream of all measures.
     *
     * @return all measures
     */
    Stream<M> getMeasures();

    /**
     * Check if a measure is base (or derived) measure.
     *
     * @param measure measure
     * @return <code>true</code> if argument is base measure, <code>false</code> otherwise
     */
    boolean isBaseMeasure(M measure);

    /**
     * Set handler of measure changes. Measure provider can keep state in sync via this object.
     *
     * @param measureChangeHandler measure change handler
     */
    void setMeasureChangeHandler(MeasureChangedHandler measureChangeHandler);
}
