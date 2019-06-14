package hu.blackbelt.judo.meta.expression.adapters.asm;

import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureSupport;
import hu.blackbelt.judo.meta.measure.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;

@Slf4j
public class AsmModelAdapter implements ModelAdapter<EClassifier, EDataType, EAttribute, EEnum, EClass, EReference, Measure, Unit> {

    private static final String NAMESPACE_SEPARATOR = ".";

    private final ResourceSet asmResourceSet;
    private final MeasureProvider<Measure, Unit> measureProvider;
    private final MeasureSupport<EClass, Unit> measureSupport;
    private final MeasureAdapter measureAdapter;

    private final AsmUtils asmUtils;

    public AsmModelAdapter(final ResourceSet asmResourceSet, final ResourceSet measureResourceSet) {
        this.asmResourceSet = asmResourceSet;

        asmUtils = new AsmUtils(asmResourceSet);
        measureProvider = new AsmMeasureProvider(measureResourceSet);
        measureSupport = new AsmMeasureSupport(asmResourceSet, measureProvider);

        measureAdapter = new MeasureAdapter<>(this, measureProvider, measureSupport);
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
                .filter(p -> Objects.equals(asmUtils.getPackageFQName(p), elementName.getNamespace().replace(".", NAMESPACE_SEPARATOR)))
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
        return measureAdapter.getUnit(numericExpression);
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
}
