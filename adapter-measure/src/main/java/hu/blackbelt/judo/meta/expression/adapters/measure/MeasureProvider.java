package hu.blackbelt.judo.meta.expression.adapters.measure;

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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;

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
    EMap<M, Integer> getBaseMeasures(M measure);

    /**
     * Get units of a given measure.
     *
     * @param measure measure
     * @return collection of units
     */
    EList<U> getUnits(M measure);

    /**
     * Check if a given unit is duration supporting addition to timestamp (ie. second, day, etc.).
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
     * Get stream of all units.
     *
     * @return all units
     */
    Stream<U> getUnits();

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
