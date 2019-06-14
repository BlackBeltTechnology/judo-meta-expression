package hu.blackbelt.judo.meta.expression.runtime.adapters;

import com.google.common.collect.ImmutableMap;
import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;

@Slf4j
public class AsmModelAdapter implements ModelAdapter<EClassifier, EDataType, EAttribute, EEnum, EClass, EReference, Measure, Unit> {

    private static Pattern MEASURE_NAME_PATTERN = Pattern.compile("^(.*)\\.([^\\.]+)$");

    private static final String NAMESPACE_SEPARATOR = ".";
    private static final String MEASURE_NAME_KEY = "measure";
    private static final String UNIT_NAME_KEY = "unit";

    private final ResourceSet asmResourceSet;
    private final ResourceSet measureResourceSet;

    private static final List<DurationType> DURATION_UNITS_SUPPORTING_ADDITION = Arrays.asList(DurationType.MILLISECOND, DurationType.SECOND, DurationType.MINUTE, DurationType.HOUR, DurationType.DAY, DurationType.WEEK);

    private final Map<String, EPackage> namespaceCache = new ConcurrentHashMap<>();

    private MeasureAdapter<Measure, Unit> measureAdapter;

    private final AsmUtils asmUtils;

    public AsmModelAdapter(final ResourceSet asmResourceSet, final ResourceSet measureResourceSet) {
        this.asmResourceSet = asmResourceSet;
        this.measureResourceSet = measureResourceSet;

        final Iterable<Notifier> asmContents = asmResourceSet::getAllContents;
        StreamSupport.stream(asmContents.spliterator(), false)
                .filter(e -> e instanceof EPackage).map(e -> (EPackage) e)
                .filter(e -> e.getESuperPackage() == null)
                .forEach(m -> initNamespace(Collections.emptyList(), m));

        final Iterable<Notifier> measureContents = measureResourceSet::getAllContents;
        measureAdapter = new AsmMeasureAdapter(StreamSupport.stream(measureContents.spliterator(), false)
                .filter(e -> e instanceof Measure).map(e -> (Measure) e)
                .collect(Collectors.toList()));

        asmUtils = new AsmUtils(asmResourceSet);
    }

    @Override
    public Optional<TypeName> getTypeName(final EClassifier namespaceElement) {
        return namespaceCache.values().stream()
                .filter(ns -> ns.getEClassifiers().contains(namespaceElement))
                .map(ns -> newTypeNameBuilder().withNamespace(asmUtils.getPackageFQName(ns)).withName(namespaceElement.getName()).build())
                .findAny();
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
        return measureAdapter.get(measureName);
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
    public boolean isCollection(final ReferenceExpression referenceExpression) {
        return ((EReference) referenceExpression.getReference(this)).isMany();
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
        return asmUtils.isNumeric(primitive);
    }

    @Override
    public boolean isInteger(final EDataType primitive) {
        return asmUtils.isInteger(primitive);
    }

    @Override
    public boolean isDecimal(final EDataType primitive) {
        return asmUtils.isDecimal(primitive);
    }

    @Override
    public boolean isBoolean(final EDataType primitive) {
        return asmUtils.isBoolean(primitive);
    }

    @Override
    public boolean isString(final EDataType primitive) {
        return asmUtils.isString(primitive);
    }

    @Override
    public boolean isEnumeration(final EDataType primitive) {
        return asmUtils.isEnumeration(primitive);
    }

    @Override
    public boolean isDate(final EDataType primitive) {
        return asmUtils.isDate(primitive);
    }

    @Override
    public boolean isTimestamp(final EDataType primitive) {
        return asmUtils.isTimestamp(primitive);
    }

    @Override
    public boolean isCustom(final EDataType primitive) {
        return !asmUtils.isBoolean(primitive)
                && !asmUtils.isNumeric(primitive)
                && !asmUtils.isString(primitive)
                && !asmUtils.isEnumeration(primitive)
                && !asmUtils.isDate(primitive)
                && !asmUtils.isTimestamp(primitive);
    }

    @Override
    public boolean isMeasured(final NumericExpression numericExpression) {
        return measureAdapter.isMeasured(numericExpression);
    }

    @Override
    public boolean contains(final EEnum enumeration, final String memberName) {
        return enumeration.getELiterals().stream().filter(l -> Objects.equals(l.getLiteral(), memberName)).findAny().isPresent();
    }

    @Override
    public Optional<Measure> getDurationMeasure() {
        return measureAdapter.getDurationMeasure();
    }

    @Override
    public boolean isDurationSupportingAddition(final Unit unit) {
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

    private void initNamespace(final List<EPackage> path, final EPackage namespace) {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.join(NAMESPACE_SEPARATOR, StreamSupport.stream(path.spliterator(), false).map(p -> p.getName()).collect(Collectors.toList())));
        if (sb.length() > 0) {
            sb.append(NAMESPACE_SEPARATOR);
        }
        sb.append(namespace.getName());

        final String key = sb.toString();

        namespaceCache.put(key, namespace);
        log.debug("Cached namespace: {}", key);

        final List<EPackage> newPath = new ArrayList<>(path);
        newPath.add(namespace);
        namespace.getESubpackages().stream().forEach(p -> initNamespace(newPath, p));
    }

    private Optional<Unit> getUnit(final EAttribute attribute) {
        if (asmUtils.isNumeric(attribute.getEAttributeType())) {
            final Optional<EAnnotation> annotation = asmUtils.getExtensionAnnotation(attribute, false);
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
                return derivedMeasure.getTerms().stream().collect(Collectors.toMap(t -> t.getBaseMeasure(), t -> t.getExponent()));
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
            return AsmModelAdapter.this.isDurationSupportingAddition(unit);
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
