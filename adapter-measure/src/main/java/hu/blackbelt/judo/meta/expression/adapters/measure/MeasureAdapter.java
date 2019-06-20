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
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EMap;

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
@Slf4j
public class MeasureAdapter<M, U, T> {

    /**
     * Measure adapter that is used to resolve (object) types of numeric expressions.
     */
    private final ModelAdapter modelAdapter;

    /**
     * Measure provider.
     */
    private final MeasureProvider<M, U> measureProvider;

    /**
     * Measure support of underlying data model. It can be used to resolve unit of an attribute.
     */
    private final MeasureSupport<T, U> measureSupport;

    /**
     * Cache of dimensions (map of base measured with exponents).
     */
    final Map<EMap<M, Integer>, M> dimensions = new ConcurrentHashMap<>();

    public MeasureAdapter(@NonNull final MeasureProvider<M, U> measureProvider, @NonNull final MeasureSupport<T, U> measureSupport, @NonNull final ModelAdapter modelAdapter) {
        this.measureProvider = measureProvider;
        this.measureSupport = measureSupport;
        this.modelAdapter = modelAdapter;

        measureProvider.setMeasureChangeHandler(new MeasureChangeAdapter());

        dimensions.putAll(measureProvider.getMeasures()
                .collect(Collectors.toMap(m -> getBaseMeasures(m), m -> m)));
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
     * Get duration measure that supports adding system duration units (ie. ms, s, min, etc.).
     *
     * @return duration measure
     */
    public Optional<M> getDurationMeasure() {
        return measureProvider.getMeasures().filter(m -> measureProvider.getUnits(m).stream().anyMatch(u -> measureProvider.isDurationSupportingAddition(u)))
                .findAny();
    }

    /**
     * Get unit of a numeric expression.
     *
     * @param numericExpression numeric expression
     * @return unit
     */
    public Optional<U> getUnit(final NumericExpression numericExpression) {
        if (numericExpression instanceof NumericAttribute) {
            return measureSupport.getUnit((T) (((NumericAttribute) numericExpression).getObjectExpression().getObjectType(modelAdapter)), ((NumericAttribute) numericExpression).getAttributeName());
        } else if (numericExpression instanceof MeasuredDecimal) {
            final MeasuredDecimal measuredDecimal = (MeasuredDecimal) numericExpression;
            final Optional<M> measure;
            if (measuredDecimal.getMeasure() != null) {
                measure = measureProvider.getMeasure(measuredDecimal.getMeasure().getNamespace(), measuredDecimal.getMeasure().getName());
            } else {
                measure = Optional.empty();
            }
            if (measuredDecimal.getMeasure() != null && !measure.isPresent()) {
                log.error("Invalid measure: {}::{}", measuredDecimal.getMeasure().getNamespace(), measuredDecimal.getMeasure().getName());
                return Optional.empty();
            }
            final Optional<U> unit = measureProvider.getUnitByNameOrSymbol(measure, measuredDecimal.getUnitName());
            if (!unit.isPresent()) {
                log.error("Invalid unit: {}", measuredDecimal.getUnitName());
            }
            return unit;
        } else if (numericExpression instanceof MeasuredInteger) {
            final MeasuredInteger measuredInteger = (MeasuredInteger) numericExpression;
            final Optional<M> measure;
            if (measuredInteger.getMeasure() != null) {
                measure = measureProvider.getMeasure(measuredInteger.getMeasure().getNamespace(), measuredInteger.getMeasure().getName());
            } else {
                measure = Optional.empty();
            }
            if (measuredInteger.getMeasure() != null && !measure.isPresent()) {
                log.error("Invalid measure: {}::{}", measuredInteger.getMeasure().getNamespace(), measuredInteger.getMeasure().getName());
                return Optional.empty();
            }
            final Optional<U> unit = measureProvider.getUnitByNameOrSymbol(measure, measuredInteger.getUnitName());
            if (!unit.isPresent()) {
                log.error("Invalid unit: {}", measuredInteger.getUnitName());
            }
            return unit;
        } else {
            return Optional.empty();
        }
    }

    /**
     * Get measure of a numeric expression.
     *
     * @param numericExpression numeric expression
     * @return measure
     */
    public Optional<M> getMeasure(final NumericExpression numericExpression) {
        return getDimension(numericExpression).map(bm -> dimensions.get(bm));
    }

    /**
     * Get dimension (map of base measures with exponents) of a numeric expression.
     *
     * @param numericExpression numeric expression
     * @return map of base measures with exponents
     */
    public Optional<EMap<M, Integer>> getDimension(final NumericExpression numericExpression) {
        if (numericExpression instanceof NumericAttribute) {
            return getUnit(numericExpression)
                    .map(u -> getMeasure(u))
                    .map(m -> getDimensionOfMeasure(m))
                    .filter(d -> d.isPresent()).map(d -> d.get());
        } else if (numericExpression instanceof MeasuredDecimal) {
            return getUnit(numericExpression)
                    .map(u -> getMeasure(u))
                    .map(m -> getDimensionOfMeasure(m))
                    .filter(d -> d.isPresent()).map(d -> d.get());
        } else if (numericExpression instanceof MeasuredInteger) {
            return getUnit(numericExpression)
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
                return Optional.of(ECollections.singletonEMap(durationMeasure.get(), 1));
            } else {
                log.error("No base measure is defined for temporal expressions");
                return Optional.empty();
            }
        } else if (numericExpression instanceof Constant) {
            return Optional.of(ECollections.emptyEMap());
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

    private Optional<EMap<M, Integer>> getDimensionOfIntegerAritmeticExpression(final IntegerAritmeticExpression integerAritmeticExpression) {
        final Optional<EMap<M, Integer>> left = getDimension(integerAritmeticExpression.getLeft());
        final Optional<EMap<M, Integer>> right = getDimension(integerAritmeticExpression.getRight());

        switch (integerAritmeticExpression.getOperator()) {
            case ADD:
            case SUBSTRACT:
            case MODULO:
                if (Objects.equals(left.orElse(ECollections.emptyEMap()), right.orElse(ECollections.emptyEMap()))) {
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
                final EMap<M, Integer> base = ECollections.asEMap(new HashMap<>());
                if (left.isPresent()) {
                    base.putAll(left.get());
                }
                if (right.isPresent()) {
                    right.get().forEach(e -> {
                        final M measure = e.getKey();
                        final Integer exponent = e.getValue();
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

    private Optional<EMap<M, Integer>> getDimensionOfDecimalAritmeticExpression(final DecimalAritmeticExpression decimalAritmeticExpression) {
        final Optional<EMap<M, Integer>> left = getDimension(decimalAritmeticExpression.getLeft());
        final Optional<EMap<M, Integer>> right = getDimension(decimalAritmeticExpression.getRight());

        switch (decimalAritmeticExpression.getOperator()) {
            case ADD:
            case SUBSTRACT:
                if (Objects.equals(left.orElse(ECollections.emptyEMap()), right.orElse(ECollections.emptyEMap()))) {
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
                final EMap<M, Integer> base = ECollections.asEMap(new HashMap<>());
                if (left.isPresent()) {
                    base.putAll(left.get());
                }
                if (right.isPresent()) {
                    right.get().forEach(e -> {
                        final M measure = e.getKey();
                        final Integer exponent = e.getValue();
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

    private Optional<EMap<M, Integer>> getDimensionOfSwitchExpression(final SwitchExpression switchExpression) {
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

    private class MeasureChangeAdapter implements MeasureChangedHandler<M> {

        @Override
        public void measureAdded(final M measure) {
            final EMap<M, Integer> key = getBaseMeasures(measure);
            if (dimensions.containsKey(key)) {
                log.error("Unable to add measure {}, dimension is already defined: {}", measure, key);
                throw new IllegalArgumentException("Dimension is already defined");
            }
            dimensions.put(key, measure);
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
            if (measureProvider.isBaseMeasure(measure)) {
                removed = dimensions.entrySet().removeIf(e -> measureProvider.equals(e.getValue(), measure));
            } else {
                removed = dimensions.entrySet().removeIf(e -> measureProvider.equals(e.getValue(), measure));
            }
            if (!removed) {
                log.warn("Measure is not removed: {}", measure);
            } else {
                log.trace("Measure removed: {}", measure);
            }
            prettyPrintDimensions(dimensions);
        }

    }

    void prettyPrintDimensions(final Map<EMap<M, Integer>, M> dimensions) {
        if (log.isTraceEnabled()) {
            log.trace("Dimensions:\n{}", dimensions.entrySet().stream().map(e -> "  - " + e.getValue() + ":\n" +
                    e.getKey().entrySet().stream().map(k -> "    * " + k.getKey() + " ^ " + k.getValue()).collect(Collectors.joining("\n"))
            ).collect(Collectors.joining("\n")));
        }
    }

    private EMap<M, Integer> getBaseMeasures(final M measure) {
        if (measureProvider.isBaseMeasure(measure)) {
            return ECollections.singletonEMap(measure, 1);
        } else {
            return ECollections.asEMap(resolveBaseMeasures(measure)
                    .entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().intValue())));
        }
    }

    private EMap<M, AtomicInteger> resolveBaseMeasures(final M measure) {
        final EMap<M, AtomicInteger> baseMeasures = new BasicEMap<>();

        measureProvider.getBaseMeasures(measure).forEach((base, exponent) -> {
            if (measureProvider.isBaseMeasure(base)) {
                if (baseMeasures.containsKey(base)) {
                    baseMeasures.get(base).addAndGet(exponent);
                } else {
                    baseMeasures.put(base, new AtomicInteger(exponent));
                }
            } else {
                resolveBaseMeasures(base).forEach(derived -> {
                    final M derivedBase = derived.getKey();
                    final AtomicInteger derivedExponent = derived.getValue();

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

    private Optional<EMap<M, Integer>> getDimensionOfMeasure(final M measure) {
        return dimensions.entrySet().parallelStream()
                .filter(e -> measureProvider.equals(e.getValue(), measure))
                .map(e -> e.getKey())
                .findAny();
    }
}
