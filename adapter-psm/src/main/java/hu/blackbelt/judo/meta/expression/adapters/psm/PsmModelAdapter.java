package hu.blackbelt.judo.meta.expression.adapters.psm;

import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.data.PrimitiveTypedElement;
import hu.blackbelt.judo.meta.psm.data.ReferenceTypedElement;
import hu.blackbelt.judo.meta.psm.measure.Measure;
import hu.blackbelt.judo.meta.psm.measure.Unit;
import hu.blackbelt.judo.meta.psm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.psm.type.EnumerationType;
import hu.blackbelt.judo.meta.psm.type.Primitive;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Model adapter for PSM models.
 */
@Slf4j
public class PsmModelAdapter implements ModelAdapter<NamespaceElement, Primitive, PrimitiveTypedElement, EnumerationType, EntityType, ReferenceTypedElement, Measure, Unit> {

    private static final String NAMESPACE_SEPARATOR = ".";

    private final ResourceSet psmResourceSet;
    private final MeasureProvider<Measure, Unit> measureProvider;
    private final MeasureAdapter measureAdapter;

    public PsmModelAdapter(final ResourceSet psmResourceSet, final ResourceSet measureResourceSet) {
        this.psmResourceSet = psmResourceSet;

        measureProvider = new PsmMeasureProvider(measureResourceSet);

        measureAdapter = new MeasureAdapter<>(measureProvider, this);
    }

    @Override
    public Optional<TypeName> getTypeName(NamespaceElement namespaceElement) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<? extends NamespaceElement> get(TypeName elementName) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<? extends Measure> get(MeasureName measureName) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isObjectType(NamespaceElement namespaceElement) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<? extends ReferenceTypedElement> getReference(EntityType clazz, String referenceName) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isCollection(ReferenceExpression referenceExpression) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public EntityType getTarget(ReferenceTypedElement reference) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<? extends PrimitiveTypedElement> getAttribute(EntityType clazz, String attributeName) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<? extends Primitive> getAttributeType(EntityType clazz, String attributeName) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Collection<? extends EntityType> getSuperTypes(EntityType clazz) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isNumeric(Primitive primitive) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isInteger(Primitive primitive) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isDecimal(Primitive primitive) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isBoolean(Primitive primitive) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isString(Primitive primitive) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isEnumeration(Primitive primitive) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isDate(Primitive primitive) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isTimestamp(Primitive primitive) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isCustom(Primitive primitive) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isMeasured(NumericExpression numericExpression) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean contains(EnumerationType enumeration, String memberName) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Measure> getDurationMeasure() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isDurationSupportingAddition(Unit unit) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Measure> getMeasure(NumericExpression numericExpression) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Unit> getUnit(NumericExpression numericExpression) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Map<Measure, Integer>> getDimension(NumericExpression numericExpression) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    <T> Stream<T> getPsmElement(final Class<T> clazz) {
        final Iterable<Notifier> psmContents = psmResourceSet::getAllContents;
        return StreamSupport.stream(psmContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }
}
