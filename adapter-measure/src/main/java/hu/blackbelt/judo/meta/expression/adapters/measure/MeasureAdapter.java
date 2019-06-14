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
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class MeasureAdapter<M, U, T> {

    private final ModelAdapter modelAdapter;
    private final MeasureProvider<M, U> measureProvider;
    private final MeasureSupport<T, U> measureSupport;

    private final Map<Map<M, Integer>, M> dimensions = new ConcurrentHashMap<>();

    public MeasureAdapter(final ModelAdapter modelAdapter, final MeasureProvider<M, U> measureProvider, final MeasureSupport<T, U> measureSupport) {
        this.modelAdapter = modelAdapter;
        this.measureProvider = measureProvider;
        this.measureSupport = measureSupport;

        refreshDimensions();
    }

    public void refreshDimensions() {
        dimensions.clear();

        dimensions.putAll(measureProvider.getMeasures()
                .collect(Collectors.toMap(m -> measureProvider.getBaseMeasures(m), m -> m)));
    }

    public Optional<M> get(final MeasureName measureName) {
        return measureProvider.getMeasures()
                .filter(m -> Objects.equals(measureProvider.getMeasureNamespace(m), measureName.getNamespace()) && Objects.equals(measureProvider.getMeasureName(m), measureName.getName()))
                .findAny();
    }

    public boolean isMeasured(final NumericExpression numericExpression) {
        if (numericExpression instanceof MeasuredInteger) {
            return true;
        } else if (numericExpression instanceof MeasuredDecimal) {
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
            if (integerSwitchExpression.getCases().stream().anyMatch(c -> isMeasured((NumericExpression) c.getExpression()))) {
                return true;
            } else if (integerSwitchExpression.getDefaultExpression() != null) {
                return isMeasured((NumericExpression) integerSwitchExpression.getDefaultExpression());
            } else {
                return false;
            }
        } else if (numericExpression instanceof DecimalSwitchExpression) {
            final DecimalSwitchExpression decimalSwitchExpression = (DecimalSwitchExpression) numericExpression;
            if (decimalSwitchExpression.getCases().stream().anyMatch(c -> isMeasured((NumericExpression) c.getExpression()))) {
                return true;
            } else if (decimalSwitchExpression.getDefaultExpression() != null) {
                return isMeasured((NumericExpression) decimalSwitchExpression.getDefaultExpression());
            } else {
                return false;
            }
        } else if (numericExpression instanceof TimestampDifferenceExpression) {
            return true;
        } else {
            return false;
        }
    }

    public Optional<M> getDurationMeasure() {
        return measureProvider.getMeasures().filter(m -> measureProvider.getUnits(m).stream().anyMatch(u -> measureProvider.isDurationSupportingAddition(u)))
                .findAny();
    }

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
            return measureProvider.getUnitByNameOrSymbol(measure, measuredDecimal.getUnitName());
        } else if (numericExpression instanceof MeasuredInteger) {
            final MeasuredInteger measuredInteger = (MeasuredInteger) numericExpression;
            final Optional<M> measure;
            if (measuredInteger.getMeasure() != null) {
                measure = measureProvider.getMeasure(measuredInteger.getMeasure().getNamespace(), measuredInteger.getMeasure().getName());
            } else {
                measure = Optional.empty();
            }
            return measureProvider.getUnitByNameOrSymbol(measure, measuredInteger.getUnitName());
        } else {
            return Optional.empty();
        }
    }

    public Optional<M> getMeasure(final NumericExpression numericExpression) {
        return getDimension(numericExpression).map(bm -> dimensions.get(bm));
    }

    public Optional<Map<M, Integer>> getDimension(final NumericExpression numericExpression) {
        if (numericExpression instanceof NumericAttribute) {
            return getUnit(numericExpression)
                    .map(u -> getMeasure(u))
                    .map(m -> measureProvider.getBaseMeasures(m));
        } else if (numericExpression instanceof MeasuredDecimal) {
            // TODO - log if unit not found
            return getUnit(numericExpression)
                    .map(u -> getMeasure(u))
                    .map(m -> measureProvider.getBaseMeasures(m));
        } else if (numericExpression instanceof MeasuredInteger) {
            // TODO - log if unit not found
            return getUnit(numericExpression)
                    .map(u -> getMeasure(u))
                    .map(m -> measureProvider.getBaseMeasures(m));
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
                return Optional.of(Collections.singletonMap(durationMeasure.get(), 1));
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

    M getMeasure(final U unit) {
        return measureProvider.getMeasures()
                .filter(m -> measureProvider.getUnits(m).contains(unit))
                .findAny().get();
    }

    private Optional<Map<M, Integer>> getDimensionOfIntegerAritmeticExpression(final IntegerAritmeticExpression integerAritmeticExpression) {
        final Optional<Map<M, Integer>> left = getDimension(integerAritmeticExpression.getLeft());
        final Optional<Map<M, Integer>> right = getDimension(integerAritmeticExpression.getRight());

        switch (integerAritmeticExpression.getOperator()) {
            case ADD:
            case SUBSTRACT:
                if (Objects.equals(left.orElse(Collections.emptyMap()), right.orElse(Collections.emptyMap()))) {
                    return left;
                } else if (!left.isPresent() || !right.isPresent()) {
                    log.warn("Addition of scalar and measured values is not allowed");
                    return Optional.empty();
                } else {
                    log.warn("Additional operands must match");
                    return Optional.empty();
                }
            case MULTIPLY:
            case DIVIDE:
                final Map<M, Integer> base = new HashMap<>();
                if (left.isPresent()) {
                    base.putAll(left.get());
                }
                if (right.isPresent()) {
                    right.get().forEach((measure, exponent) -> {
                        final int currentExponent = base.getOrDefault(measure, 0);
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
            case MODULO:
                return left;
        }

        throw new IllegalArgumentException("Unsupported operation");
    }

    private Optional<Map<M, Integer>> getDimensionOfDecimalAritmeticExpression(final DecimalAritmeticExpression decimalAritmeticExpression) {
        final Optional<Map<M, Integer>> left = getDimension(decimalAritmeticExpression.getLeft());
        final Optional<Map<M, Integer>> right = getDimension(decimalAritmeticExpression.getRight());

        switch (decimalAritmeticExpression.getOperator()) {
            case ADD:
            case SUBSTRACT:
                if (Objects.equals(left.orElse(Collections.emptyMap()), right.orElse(Collections.emptyMap()))) {
                    return left;
                } else if (!left.isPresent() || !right.isPresent()) {
                    log.warn("Addition of scalar and measured values is not allowed");
                    return Optional.empty();
                } else {
                    log.warn("Additional operands must match");
                    return Optional.empty();
                }
            case MULTIPLY:
            case DIVIDE:
                final Map<M, Integer> base = new HashMap<>();
                if (left.isPresent()) {
                    base.putAll(left.get());
                }
                if (right.isPresent()) {
                    right.get().forEach((measure, exponent) -> {
                        final int currentExponent = base.getOrDefault(measure, 0);
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

    private Optional<Map<M, Integer>> getDimensionOfSwitchExpression(final SwitchExpression switchExpression) {
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
            return Optional.ofNullable(measureProvider.getBaseMeasures(measures.iterator().next()));
        } else {
            return Optional.empty();
        }
    }
}
