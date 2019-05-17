package hu.blackbelt.judo.meta.expression.runtime.adapters;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.NavigationExpression;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.numeric.NumericAttribute;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    private MeasureAdapter<Measure, Unit> measureAdapter;

    public AsmModelAdapter(final ResourceSet asmResourceSet, final ResourceSet measureResourceSet) {
        this.asmResourceSet = asmResourceSet;
        this.measureResourceSet = measureResourceSet;

        final Iterable<Notifier> asmContents = asmResourceSet::getAllContents;
        StreamSupport.stream(asmContents.spliterator(), true)
                .filter(e -> e instanceof EPackage).map(e -> (EPackage) e)
                .filter(e -> e.getESuperPackage() == null)
                .forEach(m -> initNamespace(Collections.emptyList(), m));

        final Iterable<Notifier> measureContents = measureResourceSet::getAllContents;
        measureAdapter = new AsmMeasureAdapter(StreamSupport.stream(measureContents.spliterator(), true)
                .filter(e -> e instanceof Measure).map(e -> (Measure) e)
                .collect(Collectors.toList()));
    }

    @Override
    public Optional<TypeName> getTypeName(final EClassifier namespaceElement) {
        return namespaceCache.values().parallelStream()
                .filter(ns -> ns.getEClassifiers().contains(namespaceElement))
                .map(ns -> newTypeNameBuilder().withNamespace(AsmUtils.getFullName(ns)).withName(namespaceElement.getName()).build())
                .findAny();
    }

    @Override
    public Optional<EClassifier> get(final TypeName elementName) {
        final EPackage namespace = namespaceCache.get(elementName.getNamespace().replace(".", NAMESPACE_SEPARATOR));
        if (namespace != null) {
            return namespace.getEClassifiers().parallelStream().filter(e -> Objects.equals(e.getName(), elementName.getName()))
                    .findAny();
        } else {
            log.warn("Namespace not found: {}", elementName.getNamespace());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Measure> get(final MeasureName measureName) {
        return measureAdapter.get(measureName);
    }

    @Override
    public boolean isObjectType(final EClassifier namespaceElement) {
        return namespaceElement instanceof EClass;
    }

    @Override
    public Optional<EReference> getReference(final EClass clazz, final String referenceName) {
        return clazz.getEAllReferences().parallelStream().filter(r -> Objects.equals(r.getName(), referenceName)).findAny();
    }

    @Override
    public boolean isCollection(final NavigationExpression navigationExpression) {
        return ((EReference) navigationExpression.getReference(this)).isMany();
    }

    @Override
    public EClass getTarget(final EReference reference) {
        return reference.getEReferenceType();
    }

    @Override
    public Optional<EAttribute> getAttribute(final EClass clazz, final String attributeName) {
        return clazz.getEAllAttributes().parallelStream().filter(r -> Objects.equals(r.getName(), attributeName)).findAny();
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
    public boolean isMeasured(final NumericExpression numericExpression) {
        return measureAdapter.isMeasured(numericExpression);
    }

    @Override
    public boolean contains(final EEnum enumeration, final String memberName) {
        return enumeration.getELiterals().parallelStream().filter(l -> Objects.equals(l.getLiteral(), memberName)).findAny().isPresent();
    }

    @Override
    public Optional<Measure> getDurationMeasure() {
        return measureAdapter.getDurationMeasure();
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
        return measureAdapter.getUnit(numericExpression);
    }

    @Override
    public Optional<Measure> getMeasure(final NumericExpression numericExpression) {
        return measureAdapter.getMeasure(numericExpression);
    }

    @Override
    public Optional<Map<Measure, Integer>> getDimension(final NumericExpression numericExpression) {
        return measureAdapter.getDimension(numericExpression);
    }

    private void initNamespace(final Iterable<EPackage> path, final EPackage namespace) {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.join(NAMESPACE_SEPARATOR, StreamSupport.stream(path.spliterator(), true).map(p -> p.getName()).collect(Collectors.toList())));
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
        return getUnit(attribute).map(u -> measureAdapter.getMeasure(u));
    }

    private Optional<Unit> getUnitByNameOrSymbol(final Optional<MeasureName> measureName, final String nameOrSymbol) {
        // TODO - log if unit not found

        if (measureName.isPresent()) {
            return get(measureName.get())
                    .map(m -> m.getUnits().parallelStream()
                            .filter(u -> Objects.equals(u.getName(), nameOrSymbol) || Objects.equals(u.getSymbol(), nameOrSymbol))
                            .findAny().orElse(null));
        } else {
            // TODO - log if unit found in multiple measures
            final Iterable<Notifier> contents = measureResourceSet::getAllContents;
            return StreamSupport.stream(contents.spliterator(), true)
                    .filter(e -> e instanceof Unit).map(e -> (Unit) e)
                    .filter(u -> Objects.equals(u.getName(), nameOrSymbol) || Objects.equals(u.getSymbol(), nameOrSymbol))
                    .findAny();
        }
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

    class AsmMeasureAdapter extends MeasureAdapter<Measure, Unit> {

        AsmMeasureAdapter(final Collection<Measure> measures) {
            super(measures);
        }

        @Override
        String getMeasureNamespace(final Measure measure) {
            return measure.getNamespace();
        }

        @Override
        String getMeasureName(final Measure measure) {
            return measure.getName();
        }

        @Override
        Map<Measure, Integer> getBaseMeasures(final Measure measure) {
            if (measure instanceof DerivedMeasure) {
                final DerivedMeasure derivedMeasure = (DerivedMeasure) measure;
                return derivedMeasure.getTerms().parallelStream().collect(Collectors.toMap(t -> t.getBaseMeasure(), t -> t.getExponent()));
            } else {
                return ImmutableMap.of(measure, 1);
            }
        }

        @Override
        Collection<Unit> getUnits(final Measure measure) {
            return measure.getUnits();
        }

        @Override
        boolean isSupportingAddition(final Unit unit) {
            return AsmModelAdapter.this.isSupportingAddition(unit);
        }

        @Override
        Optional<Unit> getUnitByNameOrSymbol(final Optional<MeasureName> measureName, final String nameOrSymbol) {
            return AsmModelAdapter.this.getUnitByNameOrSymbol(measureName, nameOrSymbol);
        }

        @Override
        Optional<Unit> getUnit(final NumericAttribute numericAttribute) {
            // TODO - change getObjectType call
            return getAttribute((EClass) numericAttribute.getObjectExpression().getObjectType(AsmModelAdapter.this), numericAttribute.getAttributeName())
                    .map(a -> AsmModelAdapter.this.getUnit(a)).get();
        }
    }
}
