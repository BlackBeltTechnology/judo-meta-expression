package hu.blackbelt.judo.meta.expression.runtime.adapters;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.SwitchExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.constant.Constant;
import hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal;
import hu.blackbelt.judo.meta.expression.constant.MeasuredInteger;
import hu.blackbelt.judo.meta.expression.numeric.DecimalAggregatedExpression;
import hu.blackbelt.judo.meta.expression.numeric.DecimalAritmeticExpression;
import hu.blackbelt.judo.meta.expression.numeric.DecimalOppositeExpression;
import hu.blackbelt.judo.meta.expression.numeric.DecimalSwitchExpression;
import hu.blackbelt.judo.meta.expression.numeric.IntegerAggregatedExpression;
import hu.blackbelt.judo.meta.expression.numeric.IntegerAritmeticExpression;
import hu.blackbelt.judo.meta.expression.numeric.IntegerOppositeExpression;
import hu.blackbelt.judo.meta.expression.numeric.IntegerSwitchExpression;
import hu.blackbelt.judo.meta.expression.numeric.NumericAttribute;
import hu.blackbelt.judo.meta.expression.numeric.RoundExpression;
import hu.blackbelt.judo.meta.expression.temporal.TimestampDifferenceExpression;
import hu.blackbelt.judo.meta.measure.DerivedMeasure;
import hu.blackbelt.judo.meta.measure.DurationType;
import hu.blackbelt.judo.meta.measure.DurationUnit;
import hu.blackbelt.judo.meta.measure.Measure;
import hu.blackbelt.judo.meta.measure.Unit;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;

@Slf4j
public class AsmModelAdapter implements ModelAdapter<EClassifier, EDataType, EAttribute, EEnum, EClass, EReference, Measure, Unit> {

    private static final String NAMESPACE_SEPARATOR = ".";
    private static final String MEASURE_NAME_KEY = "measure";
    private static final String UNIT_NAME_KEY = "unit";

    private final ResourceSet asmResourceSet;
    private final ResourceSet measureResourceSet;

    private static final List<DurationType> DURATION_UNITS_SUPPORTING_ADDITION = Arrays.asList(DurationType.MILLISECOND, DurationType.SECOND, DurationType.MINUTE, DurationType.HOUR, DurationType.DAY, DurationType.WEEK);

    private final Map<String, EPackage> namespaceCache = new ConcurrentHashMap<>();

    private final Map<Map<Measure, Integer>, Measure> dimensions;

    public AsmModelAdapter(final ResourceSet asmResourceSet, final ResourceSet measureResourceSet) {
        this.asmResourceSet = asmResourceSet;
        this.measureResourceSet = measureResourceSet;

        final Iterable<Notifier> asmContents = asmResourceSet::getAllContents;
        StreamSupport.stream(asmContents.spliterator(), false)
                .filter(e -> e instanceof EPackage).map(e -> (EPackage) e)
                .filter(e -> e.getESuperPackage() == null)
                .forEach(m -> initNamespace(Collections.emptyList(), m));

        final Iterable<Notifier> measureContents = measureResourceSet::getAllContents;
        dimensions = StreamSupport.stream(measureContents.spliterator(), true)
                .filter(e -> e instanceof Measure).map(e -> (Measure) e)
                .collect(Collectors.toMap(m -> getBaseMeasures(m), m -> m));
    }

    private static Optional<MeasureName> parseMeasureName(final String name) {
        if (name == null) {
            return Optional.empty();
        } else {
            final Matcher m = MEASURE_NAME_PATTERN.matcher(name);
            if (m.matches()) {
                return Optional.of(newMeasureNameBuilder()
                        .withNamespace(m.group(1))
                        .withName(m.group(2))
                        .build());
            } else {
                throw new IllegalArgumentException("Invalid measure name " + name);
            }
        }
    }

    @Override
    public Optional<EClassifier> get(final TypeName elementName) {
        final EPackage namespace = namespaceCache.get(elementName.getNamespace().replace(".", NAMESPACE_SEPARATOR));
        if (namespace != null) {
            return namespace.getEClassifiers().stream().filter(e -> Objects.equals(e.getName(), elementName.getName()))
                    .findAny();
        } else {
            log.warn("Namespace not found: {}", elementName.getNamespace());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Measure> get(final MeasureName measureName) {
        final Iterable<Notifier> measureContents = measureResourceSet::getAllContents;
        return StreamSupport.stream(measureContents.spliterator(), false)
                .filter(e -> e instanceof Measure).map(e -> (Measure) e)
                .filter(m -> Objects.equals(m.getNamespace(), measureName.getNamespace()) && Objects.equals(m.getName(), measureName.getName()))
                .findAny();
    }

    @Override
    public boolean isObjectType(final EClassifier namespaceElement) {
        return namespaceElement instanceof EClass;
    }

    @Override
    public Optional<EReference> getReference(final EClass clazz, final String referenceName) {
        return clazz.getEAllReferences().stream().filter(r -> Objects.equals(r.getName(), referenceName)).findAny();
    }

    @Override
    public boolean isCollection(final EReference reference) {
        return reference.isMany();
    }

    @Override
    public EClass getTarget(final EReference reference) {
        return reference.getEReferenceType();
    }

    @Override
    public Optional<EAttribute> getAttribute(final EClass clazz, final String attributeName) {
        return clazz.getEAllAttributes().stream().filter(r -> Objects.equals(r.getName(), attributeName)).findAny();
    }

    @Override
    public Optional<EDataType> getAttributeType(final EClass clazz, final String attributeName) {
        return getAttribute(clazz, attributeName).map(EAttribute::getEAttributeType);
    }

    @Override
    public Collection<EClass> getSuperTypes(final EClass clazz) {
        return clazz.getEAllSuperTypes();
    }

    @Override
    public boolean isNumeric(final EDataType primitive) {
        return AsmUtils.isNumeric(primitive);
    }

    @Override
    public boolean isInteger(final EDataType primitive) {
        return AsmUtils.isInteger(primitive);
    }

    @Override
    public boolean isDecimal(final EDataType primitive) {
        return AsmUtils.isDecimal(primitive);
    }

    @Override
    public boolean isBoolean(final EDataType primitive) {
        return AsmUtils.isBoolean(primitive);
    }

    @Override
    public boolean isString(final EDataType primitive) {
        return AsmUtils.isString(primitive);
    }

    @Override
    public boolean isEnumeration(final EDataType primitive) {
        return AsmUtils.isEnumeration(primitive);
    }

    @Override
    public boolean isDate(final EDataType primitive) {
        return AsmUtils.isDate(primitive);
    }

    @Override
    public boolean isTimestamp(final EDataType primitive) {
        return AsmUtils.isTimestamp(primitive);
    }

    @Override
    public boolean isCustom(final EDataType primitive) {
        return !AsmUtils.isBoolean(primitive)
                && !AsmUtils.isNumeric(primitive)
                && !AsmUtils.isString(primitive)
                && !AsmUtils.isEnumeration(primitive)
                && !AsmUtils.isDate(primitive)
                && !AsmUtils.isTimestamp(primitive);
    }

    @Override
    public boolean isMeasured(final EAttribute attribute) {
        return getMeasure(attribute).isPresent();
    }

    @Override
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

    @Override
    public boolean contains(EEnum enumeration, String memberName) {
        return enumeration.getELiterals().stream().filter(l -> Objects.equals(l.getLiteral(), memberName)).findAny().isPresent();
    }

    @Override
    public Optional<Measure> getDurationMeasure() {
        final Iterable<Notifier> contents = measureResourceSet::getAllContents;
        return StreamSupport.stream(contents.spliterator(), false)
                .filter(e -> e instanceof Measure).map(e -> (Measure) e)
                .filter(m -> m.getUnits().stream().anyMatch(u -> isSupportingAddition(u)))
                .findAny();
    }

    @Override
    public boolean isSupportingAddition(final Unit unit) {
        if (unit instanceof DurationUnit) {
            return (unit instanceof DurationUnit) && DURATION_UNITS_SUPPORTING_ADDITION.contains(((DurationUnit) unit).getType());
        } else {
            return false;
        }
    }

    @Override
    public Optional<Unit> getUnit(final NumericExpression numericExpression) {
        if (numericExpression instanceof NumericAttribute) {
            final NumericAttribute numericAttribute = (NumericAttribute) numericExpression;
            // TODO - change getObjectType call
            return getAttribute((EClass) numericAttribute.getObjectExpression().getObjectType(this), numericAttribute.getAttributeName())
                    .map(a -> getUnit(a)).get();
        } else if (numericExpression instanceof MeasuredDecimal) {
            final MeasuredDecimal measuredDecimal = (MeasuredDecimal) numericExpression;
            return getUnitByNameOrSymbol(Optional.ofNullable(measuredDecimal.getMeasure()), measuredDecimal.getUnitName());
        } else if (numericExpression instanceof MeasuredInteger) {
            final MeasuredInteger measuredInteger = (MeasuredInteger) numericExpression;
            return getUnitByNameOrSymbol(Optional.ofNullable(measuredInteger.getMeasure()), measuredInteger.getUnitName());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Measure> getMeasure(final NumericExpression numericExpression) {
        return getDimension(numericExpression).map(bm -> dimensions.get(bm));
    }

    @Override
    public Optional<Map<Measure, Integer>> getDimension(final NumericExpression numericExpression) {
        if (numericExpression instanceof NumericAttribute) {
            return getUnit(numericExpression)
                    .map(u -> getMeasure(u))
                    .map(m -> getBaseMeasures(m));
        } else if (numericExpression instanceof MeasuredDecimal) {
            // TODO - log if unit not found
            return getUnit(numericExpression)
                    .map(u -> getMeasure(u))
                    .map(m -> getBaseMeasures(m));
        } else if (numericExpression instanceof MeasuredInteger) {
            // TODO - log if unit not found
            return getUnit(numericExpression)
                    .map(u -> getMeasure(u))
                    .map(m -> getBaseMeasures(m));
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
            final Optional<Measure> durationMeasure = getDurationMeasure();
            if (durationMeasure.isPresent()) {
                return Optional.of(ImmutableMap.of(durationMeasure.get(), 1));
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

    private Optional<Map<Measure, Integer>> getDimensionOfIntegerAritmeticExpression(final IntegerAritmeticExpression integerAritmeticExpression) {
        final Optional<Map<Measure, Integer>> left = getDimension(integerAritmeticExpression.getLeft());
        final Optional<Map<Measure, Integer>> right = getDimension(integerAritmeticExpression.getRight());

        switch (integerAritmeticExpression.getOperator()) {
            case ADD:
            case SUBSTRACT:
                if (Objects.equals(left, right)) {
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
                final Map<Measure, Integer> base = new HashMap<>();
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

    private Optional<Map<Measure, Integer>> getDimensionOfDecimalAritmeticExpression(final DecimalAritmeticExpression decimalAritmeticExpression) {
        final Optional<Map<Measure, Integer>> left = getDimension(decimalAritmeticExpression.getLeft());
        final Optional<Map<Measure, Integer>> right = getDimension(decimalAritmeticExpression.getRight());

        switch (decimalAritmeticExpression.getOperator()) {
            case ADD:
            case SUBSTRACT:
                if (Objects.equals(left, right)) {
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
                final Map<Measure, Integer> base = new HashMap<>();
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

    private Optional<Map<Measure, Integer>> getDimensionOfSwitchExpression(final SwitchExpression switchExpression) {
        final Set<Measure> measures = new HashSet<>();

        switchExpression.getCases().stream().map(c -> c.getExpression())
                .forEach(e -> measures.add(getMeasure((NumericExpression) e).orElse(null)));

        if (switchExpression.getDefaultExpression() != null) {
            measures.add(getMeasure((NumericExpression) switchExpression.getDefaultExpression()).orElse(null));
        }

        if (measures.size() == 1) {
            return Optional.ofNullable(getBaseMeasures(measures.iterator().next()));
        } else {
            return Optional.empty();
        }
    }

    private Measure getMeasure(final Unit unit) {
        final Iterable<Notifier> measureContents = measureResourceSet::getAllContents;
        return StreamSupport.stream(measureContents.spliterator(), true)
                .filter(e -> e instanceof Measure).map(e -> (Measure) e)
                .filter(m -> m.getUnits().contains(unit))
                .findAny().get();
    }

    private void initNamespace(final Iterable<EPackage> path, final EPackage namespace) {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.join(NAMESPACE_SEPARATOR, StreamSupport.stream(path.spliterator(), false).map(p -> p.getName()).collect(Collectors.toList())));
        if (sb.length() > 0) {
            sb.append(NAMESPACE_SEPARATOR);
        }
        sb.append(namespace.getName());

        final String key = sb.toString();

        namespaceCache.put(key, namespace);
        log.debug("Cached namespace: {}", key);

        final Iterable<EPackage> newPath = Iterables.concat(path, Collections.singleton(namespace));
        namespace.getESubpackages().parallelStream().forEach(p -> initNamespace(newPath, p));
    }

    private Optional<Unit> getUnit(final EAttribute attribute) {
        if (AsmUtils.isNumeric(attribute.getEAttributeType())) {
            final Optional<EAnnotation> annotation = AsmUtils.getExtensionAnnotation(attribute, false);
            if (annotation.isPresent()) {
                return getUnitByNameOrSymbol(parseMeasureName(annotation.get().getDetails().get(MEASURE_NAME_KEY)), annotation.get().getDetails().get(UNIT_NAME_KEY));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    private Optional<Measure> getMeasure(final EAttribute attribute) {
        return getUnit(attribute).map(u -> getMeasure(u));
    }

    private Optional<Unit> getUnitByNameOrSymbol(final Optional<MeasureName> measureName, final String nameOrSymbol) {
        // TODO - log if unit not found

        if (measureName.isPresent()) {
            return get(measureName.get())
                    .map(m -> m.getUnits().stream()
                            .filter(u -> Objects.equals(u.getName(), nameOrSymbol) || Objects.equals(u.getSymbol(), nameOrSymbol))
                            .findAny().orElse(null));
        } else {
            // TODO - log if unit found in multiple measures
            final Iterable<Notifier> contents = measureResourceSet::getAllContents;
            return StreamSupport.stream(contents.spliterator(), false)
                    .filter(e -> e instanceof Unit).map(e -> (Unit) e)
                    .filter(u -> Objects.equals(u.getName(), nameOrSymbol) || Objects.equals(u.getSymbol(), nameOrSymbol))
                    .findAny();
        }
    }

    private Map<Measure, Integer> getBaseMeasures(final Measure measure) {
        if (measure instanceof DerivedMeasure) {
            final DerivedMeasure derivedMeasure = (DerivedMeasure) measure;
            return derivedMeasure.getTerms().stream().collect(Collectors.toMap(t -> t.getBaseMeasure(), t -> t.getExponent()));
        } else {
            return ImmutableMap.of(measure, 1);
        }
    }
}
