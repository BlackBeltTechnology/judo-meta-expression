package hu.blackbelt.judo.meta.expression.adapters.measure;

import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.SwitchExpression;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.constant.Constant;
import hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal;
import hu.blackbelt.judo.meta.expression.constant.MeasuredInteger;
import hu.blackbelt.judo.meta.expression.numeric.*;
import hu.blackbelt.judo.meta.expression.temporal.TimestampDifferenceExpression;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Measure adapter for expressions.
 *
 * @param <M> measure type (in metamodel)
 * @param <U> unit type (in metamodel)
 * @param <T> object type (in metamodel)
 */
public class MeasureAdapter<M, U, T> {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(MeasureAdapter.class);
    /**
     * Measure adapter that is used to resolve (object) types of numeric expressions.
     */
    private final ModelAdapter<?, ?, ?, ?, ?, ?, M, U> modelAdapter;

    /**
     * Measure provider.
     */
    private final MeasureProvider<M, U> measureProvider;

    /**
     * Cache of dimensions (map of base measured with exponents).
     */
    final Map<Map<MeasureId, Integer>, MeasureId> dimensions = new ConcurrentHashMap<>();

    public MeasureAdapter(final MeasureProvider<M, U> measureProvider, final ModelAdapter modelAdapter) {
        this.measureProvider = measureProvider;
        this.modelAdapter = modelAdapter;

        measureProvider.setMeasureChangeHandler(new MeasureChangeAdapter());

        dimensions.putAll(measureProvider.getMeasures()
                .collect(Collectors.toMap(m -> getBaseMeasures(m), m -> MeasureId.fromMeasure(measureProvider, m))));
    }

    /**
     * Get measure by measure name (namespace + name).
     *
     * @param measureName measure name (defined by expression metamodel)
     * @return measure
     */
    public Optional<M> get(final MeasureName measureName) {
        return measureProvider.getMeasures()
                .filter(m -> Objects.equals(measureProvider.getMeasureNamespace(m), measureName.getNamespace()) && Objects.equals(measureProvider.getMeasureName(m), measureName.getName()))
                .findAny();
    }

    /**
     * Check if a numeric expression is measured.
     *
     * @param numericExpression numeric expression
     * @return <code>true</code> if numeric expression is measured, <code>false</code> otherwise
     */
    public boolean isMeasured(final NumericExpression numericExpression) {
        if (numericExpression instanceof MeasuredInteger) {
            // measured integer (constant) is always measured
            return true;
        } else if (numericExpression instanceof MeasuredDecimal) {
            // measured decimal (constant) is always measured
            return true;
        } else if (numericExpression instanceof NumericAttribute) {
            return getMeasure(numericExpression).isPresent();
        } else if (numericExpression instanceof IntegerAritmeticExpression) {
            final IntegerAritmeticExpression integerAritmeticExpression = (IntegerAritmeticExpression) numericExpression;
            return getDimension(integerAritmeticExpression).map(d -> !d.isEmpty()).orElse(false);
        } else if (numericExpression instanceof DecimalAritmeticExpression) {
            final DecimalAritmeticExpression decimalAritmeticExpression = (DecimalAritmeticExpression) numericExpression;
            return getDimension(decimalAritmeticExpression).map(d -> !d.isEmpty()).orElse(false);
        } else if (numericExpression instanceof IntegerOppositeExpression) {
            return isMeasured(((IntegerOppositeExpression) numericExpression).getExpression());
        } else if (numericExpression instanceof DecimalOppositeExpression) {
            return isMeasured(((DecimalOppositeExpression) numericExpression).getExpression());
        } else if (numericExpression instanceof IntegerAggregatedExpression) {
            return isMeasured(((IntegerAggregatedExpression) numericExpression).getExpression());
        } else if (numericExpression instanceof DecimalAggregatedExpression) {
            return isMeasured(((DecimalAggregatedExpression) numericExpression).getExpression());
        } else if (numericExpression instanceof RoundExpression) {
            return isMeasured(((RoundExpression) numericExpression).getExpression());
        } else if (numericExpression instanceof IntegerSwitchExpression) {
            final IntegerSwitchExpression integerSwitchExpression = (IntegerSwitchExpression) numericExpression;
            // an integer switch expression is measured if any case (including default) is measured
            if (integerSwitchExpression.getCases().stream().anyMatch(c -> isMeasured((NumericExpression) c.getExpression()))) {
                return true;
            } else if (integerSwitchExpression.getDefaultExpression() != null) {
                return isMeasured((NumericExpression) integerSwitchExpression.getDefaultExpression());
            } else {
                return false;
            }
        } else if (numericExpression instanceof DecimalSwitchExpression) {
            final DecimalSwitchExpression decimalSwitchExpression = (DecimalSwitchExpression) numericExpression;
            // a decimal switch expression is measured if any case (including default) is measured
            if (decimalSwitchExpression.getCases().stream().anyMatch(c -> isMeasured((NumericExpression) c.getExpression()))) {
                return true;
            } else if (decimalSwitchExpression.getDefaultExpression() != null) {
                return isMeasured((NumericExpression) decimalSwitchExpression.getDefaultExpression());
            } else {
                return false;
            }
        } else if (numericExpression instanceof TimestampDifferenceExpression) {
            // timestamp difference expression is measured (duration)
            return true;
        } else {
            // numeric type that not supports measures by architecture
            return false;
        }
    }

    /**
     * Get duration measure that supports addition to timestamp (ie. ms, s, min, etc.).
     *
     * @return duration measure
     */
    public Optional<M> getDurationMeasure() {
        return measureProvider.getMeasures().filter(m -> measureProvider.getUnits(m).stream().anyMatch(u -> measureProvider.isDurationSupportingAddition(u)))
                .findAny();
    }

    /**
     * Get unit by name or symbol. Unit of a given measure returned if measure is specified.
     *
     * @param measureNamespace measure namespace
     * @param measureName      measure name
     * @param unitNameOrSymbol unit name or symbol
     * @return unit
     */
    public Optional<U> getUnit(final Optional<String> measureNamespace, final Optional<String> measureName, final String unitNameOrSymbol) {
        final Optional<M> measure;
        if (measureName.isPresent()) {
            measure = measureProvider.getMeasure(measureNamespace.orElse(null), measureName.get());
        } else {
            measure = Optional.empty();
        }
        if (measureName.isPresent() && !measure.isPresent()) {
            log.error("Invalid measure: {}::{}", measureNamespace.orElse(null), measureName.get());
            return Optional.empty();
        }
        final Optional<U> unit = measureProvider.getUnitByNameOrSymbol(measure, unitNameOrSymbol);
        if (!unit.isPresent()) {
            log.error("Invalid unit: {}", unitNameOrSymbol);
        }
        return unit;
    }

    /**
     * Get measure of a numeric expression.
     *
     * @param numericExpression numeric expression
     * @return measure
     */
    public Optional<M> getMeasure(final NumericExpression numericExpression) {
        return getDimension(numericExpression).map(bm -> measureProvider.getMeasure(dimensions.get(bm).getNamespace(), dimensions.get(bm).getName()))
                .filter(m -> m.isPresent()).map(m -> m.get());
    }

    /**
     * Get dimension (map of base measures with exponents) of a numeric expression.
     *
     * @param numericExpression numeric expression
     * @return map of base measures with exponents
     */
    public Optional<Map<MeasureId, Integer>> getDimension(final NumericExpression numericExpression) {
        if (numericExpression instanceof NumericAttribute) {
            return modelAdapter.getUnit(numericExpression)
                    .map(u -> getMeasure(u))
                    .map(m -> getDimensionOfMeasure(m))
                    .filter(d -> d.isPresent()).map(d -> d.get());
        } else if (numericExpression instanceof MeasuredDecimal) {
            return modelAdapter.getUnit(numericExpression)
                    .map(u -> getMeasure(u))
                    .map(m -> getDimensionOfMeasure(m))
                    .filter(d -> d.isPresent()).map(d -> d.get());
        } else if (numericExpression instanceof MeasuredInteger) {
            return modelAdapter.getUnit(numericExpression)
                    .map(u -> getMeasure(u))
                    .map(m -> getDimensionOfMeasure(m))
                    .filter(d -> d.isPresent()).map(d -> d.get());
        } else if (numericExpression instanceof DecimalAritmeticExpression) {
            return getDimensionOfDecimalAritmeticExpression((DecimalAritmeticExpression) numericExpression);
        } else if (numericExpression instanceof IntegerAritmeticExpression) {
            return getDimensionOfIntegerAritmeticExpression((IntegerAritmeticExpression) numericExpression);
        } else if (numericExpression instanceof IntegerOppositeExpression) {
            return getDimension(((IntegerOppositeExpression) numericExpression).getExpression());
        } else if (numericExpression instanceof DecimalOppositeExpression) {
            return getDimension(((DecimalOppositeExpression) numericExpression).getExpression());
        } else if (numericExpression instanceof IntegerAggregatedExpression) {
            return getDimension(((IntegerAggregatedExpression) numericExpression).getExpression());
        } else if (numericExpression instanceof DecimalAggregatedExpression) {
            return getDimension(((DecimalAggregatedExpression) numericExpression).getExpression());
        } else if (numericExpression instanceof RoundExpression) {
            return getDimension(((RoundExpression) numericExpression).getExpression());
        } else if (numericExpression instanceof IntegerSwitchExpression) {
            return getDimensionOfSwitchExpression((IntegerSwitchExpression) numericExpression);
        } else if (numericExpression instanceof DecimalSwitchExpression) {
            return getDimensionOfSwitchExpression((DecimalSwitchExpression) numericExpression);
        } else if (numericExpression instanceof TimestampDifferenceExpression) {
            final Optional<M> durationMeasure = getDurationMeasure();
            if (durationMeasure.isPresent()) {
                return Optional.of(Collections.singletonMap(MeasureId.fromMeasure(measureProvider, durationMeasure.get()), 1));
            } else {
                log.error("No base measure is defined for temporal expressions");
                return Optional.empty();
            }
        } else if (numericExpression instanceof Constant) {
            return Optional.of(Collections.emptyMap());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Get measure of a unit.
     *
     * @param unit unit
     * @return measure
     */
    M getMeasure(final U unit) {
        return measureProvider.getMeasures()
                .filter(m -> measureProvider.getUnits(m).contains(unit))
                .findAny()
                .orElse(null);
    }

    private Optional<Map<MeasureId, Integer>> getDimensionOfIntegerAritmeticExpression(final IntegerAritmeticExpression integerAritmeticExpression) {
        final Optional<Map<MeasureId, Integer>> left = getDimension(integerAritmeticExpression.getLeft());
        final Optional<Map<MeasureId, Integer>> right = getDimension(integerAritmeticExpression.getRight());

        switch (integerAritmeticExpression.getOperator()) {
            case ADD:
            case SUBSTRACT:
            case MODULO:
                if (Objects.equals(left.orElse(Collections.emptyMap()), right.orElse(Collections.emptyMap()))) {
                    return left;
                } else if (!left.isPresent() || !right.isPresent()) {
                    log.error("Addition of scalar and measured values is not allowed");
                    return Optional.empty();
                } else {
                    log.error("Additional operands must match");
                    return Optional.empty();
                }
            case MULTIPLY:
            case DIVIDE:
                final Map<MeasureId, Integer> base = new HashMap<>();
                if (left.isPresent()) {
                    base.putAll(left.get());
                }
                if (right.isPresent()) {
                    right.get().forEach((measure, exponent) -> {
                        final int currentExponent;
                        if (base.containsKey(measure)) {
                            currentExponent = base.get(measure);
                        } else {
                            currentExponent = 0;
                        }
                        final int newExponent;
                        switch (integerAritmeticExpression.getOperator()) {
                            case MULTIPLY:
                                newExponent = currentExponent + exponent;
                                break;
                            case DIVIDE:
                                newExponent = currentExponent - exponent;
                                break;
                            default:
                                throw new IllegalStateException("Unsupported operation");
                        }
                        if (newExponent != 0) {
                            base.put(measure, newExponent);
                        } else {
                            base.remove(measure);
                        }
                    });
                }
                return Optional.of(base);
        }

        throw new IllegalArgumentException("Unsupported operation");
    }

    private Optional<Map<MeasureId, Integer>> getDimensionOfDecimalAritmeticExpression(final DecimalAritmeticExpression decimalAritmeticExpression) {
        final Optional<Map<MeasureId, Integer>> left = getDimension(decimalAritmeticExpression.getLeft());
        final Optional<Map<MeasureId, Integer>> right = getDimension(decimalAritmeticExpression.getRight());

        switch (decimalAritmeticExpression.getOperator()) {
            case ADD:
            case SUBSTRACT:
                if (Objects.equals(left.orElse(Collections.emptyMap()), right.orElse(Collections.emptyMap()))) {
                    return left;
                } else if (!left.isPresent() || !right.isPresent()) {
                    log.error("Addition of scalar and measured values is not allowed");
                    return Optional.empty();
                } else {
                    log.error("Additional operands must match");
                    return Optional.empty();
                }
            case MULTIPLY:
            case DIVIDE:
                final Map<MeasureId, Integer> base = new HashMap<>();
                if (left.isPresent()) {
                    base.putAll(left.get());
                }
                if (right.isPresent()) {
                    right.get().forEach((measure, exponent) -> {
                        final int currentExponent;
                        if (base.containsKey(measure)) {
                            currentExponent = base.get(measure);
                        } else {
                            currentExponent = 0;
                        }
                        final int newExponent;
                        switch (decimalAritmeticExpression.getOperator()) {
                            case MULTIPLY:
                                newExponent = currentExponent + exponent;
                                break;
                            case DIVIDE:
                                newExponent = currentExponent - exponent;
                                break;
                            default:
                                throw new IllegalStateException("Unsupported operation");
                        }
                        if (newExponent != 0) {
                            base.put(measure, newExponent);
                        } else {
                            base.remove(measure);
                        }
                    });
                }
                return Optional.of(base);
        }

        throw new IllegalArgumentException("Unsupported operation");
    }

    private Optional<Map<MeasureId, Integer>> getDimensionOfSwitchExpression(final SwitchExpression switchExpression) {
        final Set<M> measures = new HashSet<>();

        switchExpression.getCases().stream().map(c -> c.getExpression())
                .forEach(e -> {
                    final Optional<M> measure = getMeasure((NumericExpression) e);
                    if (measure.isPresent()) {
                        measures.add(measure.get());
                    }
                });

        if (switchExpression.getDefaultExpression() != null) {
            final Optional<M> measure = getMeasure((NumericExpression) switchExpression.getDefaultExpression());
            if (measure.isPresent()) {
                measures.add(measure.get());
            }
        }

        if (measures.size() == 1) {
            return getDimensionOfMeasure(measures.iterator().next());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Internal measure change handler to keeps dimension cache in sync with measure provider.
     */
    private class MeasureChangeAdapter implements MeasureChangedHandler<M> {

        @Override
        public void measureAdded(final M measure) {
            final Map<MeasureId, Integer> key = getBaseMeasures(measure);
            if (dimensions.containsKey(key)) {
                log.error("Unable to add measure {}, dimension is already defined: {}", measure, key);
                throw new IllegalArgumentException("Dimension is already defined");
            }
            dimensions.put(key, MeasureId.fromMeasure(measureProvider, measure));
            log.trace("Measure added: {}", measure);
            prettyPrintDimensions(dimensions);
        }

        @Override
        public void measureChanged(final M measure) {
            if (!measureProvider.isBaseMeasure(measure)) {
                measureRemoved(measure);
                measureAdded(measure);
                log.trace("Derived measure changed: {}", measure);
                prettyPrintDimensions(dimensions);
            }
        }

        @Override
        public void measureRemoved(final M measure) {
            final boolean removed;
            final MeasureId measureId = MeasureId.fromMeasure(measureProvider, measure);
            if (measureProvider.isBaseMeasure(measure)) {
                removed = dimensions.entrySet().removeIf(e -> Objects.equals(e.getValue(), measureId));
            } else {
                removed = dimensions.entrySet().removeIf(e -> Objects.equals(e.getValue(), measureId));
            }
            if (!removed) {
                log.warn("Measure is not removed: {}", measure);
            } else {
                log.trace("Measure removed: {}", measure);
            }
            prettyPrintDimensions(dimensions);
        }

    }

    void prettyPrintDimensions(final Map<Map<MeasureId, Integer>, MeasureId> dimensions) {
        if (log.isTraceEnabled()) {
            log.trace("Dimensions:\n{}", dimensions.entrySet().stream().map(e -> "  - " + e.getValue() + ":\n" +
                    e.getKey().entrySet().stream().map(k -> "    * " + k.getKey() + " ^ " + k.getValue()).collect(Collectors.joining("\n"))
            ).collect(Collectors.joining("\n")));
        }
    }

    private Map<MeasureId, Integer> getBaseMeasures(final M measure) {
        final MeasureId measureId = MeasureId.fromMeasure(measureProvider, measure);

        if (measureProvider.isBaseMeasure(measure)) {
            return Collections.singletonMap(measureId, 1);
        } else {
            return resolveBaseMeasures(measure)
                    .entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().intValue()));
        }
    }

    private Map<MeasureId, AtomicInteger> resolveBaseMeasures(final M measure) {
        final Map<MeasureId, AtomicInteger> baseMeasures = new HashMap<>();

        measureProvider.getBaseMeasures(measure).forEach((base, exponent) -> {
            if (measureProvider.isBaseMeasure(base)) {
                final MeasureId baseId = MeasureId.fromMeasure(measureProvider, base);

                if (baseMeasures.containsKey(baseId)) {
                    baseMeasures.get(baseId).addAndGet(exponent);
                } else {
                    baseMeasures.put(baseId, new AtomicInteger(exponent));
                }
            } else {
                resolveBaseMeasures(base).forEach((derivedBase, derivedExponent) -> {
                    if (baseMeasures.containsKey(derivedBase)) {
                        baseMeasures.get(derivedBase).addAndGet(exponent * derivedExponent.intValue());
                    } else {
                        baseMeasures.put(derivedBase, new AtomicInteger(exponent * derivedExponent.intValue()));
                    }
                });
            }
        });

        if (log.isTraceEnabled()) {
            log.trace("Dimension of {}:\n{}", measure, baseMeasures.entrySet().stream().map(e -> "  - " + e.getKey() + " ^ " + e.getValue().intValue()).collect(Collectors.joining("\n")));
        }

        return baseMeasures;
    }

    private Optional<Map<MeasureId, Integer>> getDimensionOfMeasure(final M measure) {
        final MeasureId measureId = MeasureId.fromMeasure(measureProvider, measure);
        return dimensions.entrySet().parallelStream()
                .filter(e -> Objects.equals(e.getValue(), measureId))
                .map(e -> e.getKey())
                .findAny();
    }

    /**
     * Measure ID used internally.
     */
    public static class MeasureId {

        /**
         * Namespace of measure.
         */
        private String namespace;

        /**
         * Name of measure.
         */
        private String name;

        public MeasureId(String namespace, String name) {
            this.namespace = namespace;
            this.name = name;
        }

        public static <M, U> MeasureId fromMeasure(final MeasureProvider<M, U> measureProvider, M measure) {
            return new MeasureId(measureProvider.getMeasureNamespace(measure), measureProvider.getMeasureName(measure));
        }

        public String toString() {
            return namespace + "::" + name;
        }

        public String getNamespace() {
            return this.namespace;
        }

        public String getName() {
            return this.name;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof MeasureAdapter.MeasureId)) return false;
            final MeasureId other = (MeasureId) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$namespace = this.getNamespace();
            final Object other$namespace = other.getNamespace();
            if (this$namespace == null ? other$namespace != null : !this$namespace.equals(other$namespace))
                return false;
            final Object this$name = this.getName();
            final Object other$name = other.getName();
            if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof MeasureAdapter.MeasureId;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $namespace = this.getNamespace();
            result = result * PRIME + ($namespace == null ? 43 : $namespace.hashCode());
            final Object $name = this.getName();
            result = result * PRIME + ($name == null ? 43 : $name.hashCode());
            return result;
        }
    }
}
