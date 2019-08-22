package hu.blackbelt.judo.meta.expression.adapters.asm;

import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal;
import hu.blackbelt.judo.meta.expression.constant.MeasuredInteger;
import hu.blackbelt.judo.meta.expression.numeric.NumericAttribute;
import hu.blackbelt.judo.meta.measure.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newMeasureNameBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;

/**
 * Model adapter for ASM models.
 */
@Slf4j
public class AsmModelAdapter implements ModelAdapter<EClassifier, EDataType, EAttribute, EEnum, EClass, EReference, Measure, Unit> {

    private static final String NAMESPACE_SEPARATOR = "::";

    private static Pattern MEASURE_NAME_PATTERN = Pattern.compile("^(.*)\\.([^\\.]+)$");

    private final ResourceSet asmResourceSet;
    private final MeasureProvider<Measure, Unit> measureProvider;
    private final MeasureAdapter measureAdapter;

    private final AsmUtils asmUtils;

    public AsmModelAdapter(final ResourceSet asmResourceSet, final ResourceSet measureResourceSet) {
        this.asmResourceSet = asmResourceSet;

        asmUtils = new AsmUtils(asmResourceSet);
        measureProvider = new AsmMeasureProvider(measureResourceSet);

        measureAdapter = new MeasureAdapter<>(measureProvider, this);
    }

    @Override
    public Optional<TypeName> getTypeName(final EClassifier namespaceElement) {
        return getAsmElement(EPackage.class)
                .filter(ns -> ns.getEClassifiers().contains(namespaceElement))
                .map(ns -> newTypeNameBuilder().withNamespace(asmUtils.getPackageFQName(ns)).withName(namespaceElement.getName()).build())
                .findAny();
    }

    @Override
    public Optional<? extends EClassifier> get(final TypeName elementName) {
        final Optional<EPackage> namespace = getAsmElement(EPackage.class)
                .filter(p -> Objects.equals(AsmUtils.getPackageFQName(p), elementName.getNamespace().replace(NAMESPACE_SEPARATOR, ".")))
                .findAny();

        if (namespace.isPresent()) {
            return namespace.get().getEClassifiers().stream()
                    .filter(e -> Objects.equals(e.getName(), elementName.getName()))
                    .findAny();
        } else {
            log.warn("Namespace not found: {}", elementName.getNamespace());
            return Optional.empty();
        }
    }

    @Override
    public Optional<? extends Measure> get(final MeasureName measureName) {
        return measureProvider.getMeasure(measureName.getNamespace().replace(".", NAMESPACE_SEPARATOR), measureName.getName());
    }

    @Override
    public boolean isObjectType(final EClassifier namespaceElement) {
        return namespaceElement instanceof EClass;
    }

    @Override
    public Optional<? extends EReference> getReference(final EClass clazz, final String referenceName) {
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
    public Optional<? extends EAttribute> getAttribute(final EClass clazz, final String attributeName) {
        return clazz.getEAllAttributes().stream().filter(r -> Objects.equals(r.getName(), attributeName)).findAny();
    }

    @Override
    public Optional<? extends EDataType> getAttributeType(final EClass clazz, final String attributeName) {
        return getAttribute(clazz, attributeName).map(EAttribute::getEAttributeType);
    }

    @Override
    public Collection<? extends EClass> getSuperTypes(final EClass clazz) {
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
    public boolean isDate(EDataType primitive) {
        return asmUtils.isDecimal(primitive);
    }

    @Override
    public boolean isTimestamp(EDataType primitive) {
        return asmUtils.isTimestamp(primitive);
    }

    @Override
    public boolean isCustom(EDataType primitive) {
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
        return measureProvider.isDurationSupportingAddition(unit);
    }

    @Override
    public Optional<Measure> getMeasure(final NumericExpression numericExpression) {
        return measureAdapter.getMeasure(numericExpression);
    }

    @Override
    public Optional<Unit> getUnit(final NumericExpression numericExpression) {
        if (numericExpression instanceof NumericAttribute) {
            final EClass objectType = (EClass) ((NumericAttribute) numericExpression).getObjectExpression().getObjectType(this);
            final String attributeName = ((NumericAttribute) numericExpression).getAttributeName();

            return getUnit(objectType, attributeName);
        } else if (numericExpression instanceof MeasuredDecimal) {
            final MeasuredDecimal measuredDecimal = (MeasuredDecimal) numericExpression;
            return measureAdapter.getUnit(measuredDecimal.getMeasure() != null ? Optional.ofNullable(measuredDecimal.getMeasure().getNamespace()) : Optional.empty(),
                    measuredDecimal.getMeasure() != null ? Optional.ofNullable(measuredDecimal.getMeasure().getName()) : Optional.empty(),
                    measuredDecimal.getUnitName());
        } else if (numericExpression instanceof MeasuredInteger) {
            final MeasuredInteger measuredInteger = (MeasuredInteger) numericExpression;
            return measureAdapter.getUnit(measuredInteger.getMeasure() != null ? Optional.ofNullable(measuredInteger.getMeasure().getNamespace()) : Optional.empty(),
                    measuredInteger.getMeasure() != null ? Optional.ofNullable(measuredInteger.getMeasure().getName()) : Optional.empty(),
                    measuredInteger.getUnitName());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Map<Measure, Integer>> getDimension(final NumericExpression numericExpression) {
        return measureAdapter.getDimension(numericExpression);
    }

    <T> Stream<T> getAsmElement(final Class<T> clazz) {
        final Iterable<Notifier> asmContents = asmResourceSet::getAllContents;
        return StreamSupport.stream(asmContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }

    Optional<Unit> getUnit(final EClass objectType, final String attributeName) {
        final Optional<Optional<Unit>> unit = objectType.getEAllAttributes().stream()
                .filter(a -> Objects.equals(a.getName(), attributeName))
                .map(a -> getUnit(a))
                .findAny();
        if (unit.isPresent()) {
            return unit.get();
        } else {
            log.error("Attribute not found: {}", attributeName);
            return Optional.empty();
        }
    }

    Optional<Unit> getUnit(final EAttribute attribute) {
        if (asmUtils.isNumeric(attribute.getEAttributeType())) {
            final Optional<String> unitNameOrSymbol = asmUtils.getExtensionAnnotationCustomValue(attribute, "constraints", "unit", false);
            final Optional<String> measureFqName = asmUtils.getExtensionAnnotationCustomValue(attribute, "constraints", "measure", false);
            if (unitNameOrSymbol.isPresent()) {
                if (measureFqName.isPresent()) {
                    final Optional<MeasureName> measureName = measureFqName.isPresent() ? parseMeasureName(measureFqName.get()) : Optional.empty();
                    if (!measureName.isPresent()) {
                        log.error("Failed to parse measure name: {}", measureFqName.get());
                        return Optional.empty();
                    }
                    final String replacedMeasureName = measureName.get().getNamespace().replace(".", NAMESPACE_SEPARATOR);
                    final Optional<Measure> measure = measureName.isPresent() ? measureProvider.getMeasure(replacedMeasureName, measureName.get().getName()) : Optional.empty();
                    if (!measure.isPresent() && measureName.isPresent()) {
                        log.error("Measure is defined but not resolved: namespace={}, name={}", replacedMeasureName, measureName.get().getName());
                        return Optional.empty();
                    }
                    return measureProvider.getUnitByNameOrSymbol(measure, unitNameOrSymbol.get());
                } else {
                    return measureProvider.getUnitByNameOrSymbol(Optional.empty(), unitNameOrSymbol.get());
                }
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
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
                return Optional.empty();
            }
        }
    }
}