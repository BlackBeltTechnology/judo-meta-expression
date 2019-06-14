package hu.blackbelt.judo.meta.expression.adapters.measure;

import java.util.Optional;

/**
 * Measure support for metamodels of data structures (ie. PSM or ASM).
 *
 * @param <T> object type that supports numeric attributes
 * @param <U> unit
 */
public interface MeasureSupport<T, U> {

    /**
     * Get unit of a given numeric attribute.
     *
     * @param type          type
     * @param attributeName attribute name
     * @return unit
     */
    Optional<U> getUnit(T type, String attributeName);
}
