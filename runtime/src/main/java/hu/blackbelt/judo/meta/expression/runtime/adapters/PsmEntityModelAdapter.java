package hu.blackbelt.judo.meta.expression.runtime.adapters;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.NavigationExpression;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.numeric.NumericAttribute;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.data.PrimitiveTypedElement;
import hu.blackbelt.judo.meta.psm.data.ReferenceTypedElement;
import hu.blackbelt.judo.meta.psm.measure.DerivedMeasure;
import hu.blackbelt.judo.meta.psm.measure.DurationType;
import hu.blackbelt.judo.meta.psm.measure.DurationUnit;
import hu.blackbelt.judo.meta.psm.measure.Measure;
import hu.blackbelt.judo.meta.psm.measure.MeasuredType;
import hu.blackbelt.judo.meta.psm.measure.Unit;
import hu.blackbelt.judo.meta.psm.namespace.Model;
import hu.blackbelt.judo.meta.psm.namespace.Namespace;
import hu.blackbelt.judo.meta.psm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.psm.type.EnumerationType;
import hu.blackbelt.judo.meta.psm.type.Primitive;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;

@Slf4j
public class PsmEntityModelAdapter implements ModelAdapter<NamespaceElement, Primitive, PrimitiveTypedElement, EnumerationType, EntityType, ReferenceTypedElement, Measure, Unit> {

    private static final String NAMESPACE_SEPARATOR = ".";

    private final ResourceSet resourceSet;

    private static final List<DurationType> DURATION_UNITS_SUPPORTING_ADDITION = Arrays.asList(DurationType.MILLISECOND, DurationType.SECOND, DurationType.MINUTE, DurationType.HOUR, DurationType.DAY, DurationType.WEEK);

    private final Map<String, Namespace> namespaceCache = new ConcurrentHashMap<>();

    private MeasureAdapter<Measure, Unit> measureAdapter;

    public PsmEntityModelAdapter(final ResourceSet resourceSet) {
        this.resourceSet = resourceSet;

        final Iterable<Notifier> contents = resourceSet::getAllContents;
        StreamSupport.stream(contents.spliterator(), false)
                .filter(e -> e instanceof Model).map(e -> (Model) e)
                .forEach(m -> initNamespace(Collections.emptyList(), m));

        try {
            final Iterable<Notifier> measureContents = resourceSet::getAllContents;
            measureAdapter = new PsmEntityMeasureAdapter(StreamSupport.stream(measureContents.spliterator(), false)
                    .filter(e -> e instanceof Measure).map(e -> (Measure) e)
                    .collect(Collectors.toList()));
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    // TODO - use PSM utils, ensure FQ name is returned
    private static String getFullName(final Namespace namespace) {
        return namespace.toString();
    }

    @Override
    public Optional<TypeName> getTypeName(final NamespaceElement namespaceElement) {
        return namespaceCache.values().stream()
                .filter(ns -> ns.getElements().contains(namespaceElement))
                .map(ns -> newTypeNameBuilder().withNamespace(getFullName(ns)).withName(namespaceElement.getName()).build())
                .findAny();
    }

    @Override
    public Optional<NamespaceElement> get(final TypeName elementName) {
        final Namespace namespace = namespaceCache.get(elementName.getNamespace().replace("::", NAMESPACE_SEPARATOR));
        if (namespace != null) {
            return namespace.getElements().stream().filter(e -> Objects.equals(e.getName(), elementName.getName()))
                    .findAny();
        } else {
            log.warn("Namespace not found: {}", elementName.getNamespace());
            return Optional.empty();
        }
    }

    @Override
    public Optional<? extends Measure> get(final MeasureName measureName) {
        final Namespace namespace = namespaceCache.get(measureName.getNamespace().replace("::", NAMESPACE_SEPARATOR));
        if (namespace != null) {
            return namespace.getElements().stream().filter(e -> Objects.equals(e.getName(), measureName.getName()) && e instanceof Measure)
                    .map(m -> (Measure) m)
                    .findAny();
        } else {
            log.warn("Measure not found: {}", measureName.getNamespace());
            return Optional.empty();
        }
    }

    @Override
    public boolean isObjectType(final NamespaceElement namespaceElement) {
        return namespaceElement instanceof EntityType;
    }

    @Override
    public Optional<? extends ReferenceTypedElement> getReference(final EntityType clazz, final String referenceName) {
        return Optional.ofNullable(clazz.getReference(referenceName));
    }

    @Override
    public boolean isCollection(final NavigationExpression navigationExpression) {
        return ((ReferenceTypedElement) navigationExpression.getReference(this)).isCollection();
    }

    @Override
    public EntityType getTarget(final ReferenceTypedElement reference) {
        return reference.getTarget();
    }

    @Override
    public Optional<? extends PrimitiveTypedElement> getAttribute(final EntityType clazz, final String attributeName) {
        return Optional.ofNullable(clazz.getAttribute(attributeName));
    }

    @Override
    public Optional<? extends Primitive> getAttributeType(final EntityType clazz, final String attributeName) {
        return getAttribute(clazz, attributeName).map(PrimitiveTypedElement::getDataType);
    }

    @Override
    public Collection<? extends EntityType> getSuperTypes(final EntityType clazz) {
        return clazz.getSuperTypes();
    }

    @Override
    public boolean isNumeric(final Primitive primitive) {
        return primitive.isNumeric();
    }

    @Override
    public boolean isInteger(final Primitive primitive) {
        return primitive.isInteger();
    }

    @Override
    public boolean isDecimal(final Primitive primitive) {
        return primitive.isDecimal();
    }

    @Override
    public boolean isBoolean(final Primitive primitive) {
        return primitive.isBoolean();
    }

    @Override
    public boolean isString(final Primitive primitive) {
        return primitive.isString();
    }

    @Override
    public boolean isEnumeration(final Primitive primitive) {
        return primitive.isEnumeration();
    }

    @Override
    public boolean isDate(final Primitive primitive) {
        return primitive.isDate();
    }

    @Override
    public boolean isTimestamp(final Primitive primitive) {
        return primitive.isTimestamp();
    }

    @Override
    public boolean isCustom(final Primitive primitive) {
        return !primitive.isBoolean()
                && !primitive.isNumeric()
                && !primitive.isString()
                && !primitive.isEnumeration()
                && !primitive.isDate()
                && !primitive.isTimestamp();
    }

    @Override
    public boolean isMeasured(final NumericExpression numericExpression) {
        return false;
    }

    @Override
    public boolean contains(final EnumerationType enumeration, final String memberName) {
        return enumeration.contains(memberName);
    }

    @Override
    public Optional<Measure> getDurationMeasure() {
        final Iterable<Notifier> contents = resourceSet::getAllContents;
        return StreamSupport.stream(contents.spliterator(), false)
                .filter(e -> e instanceof Measure).map(e -> (Measure) e)
                .filter(m -> m.getUnits().stream().anyMatch(u -> isSupportingAddition(u)))
                .findAny();
    }

    @Override
    public boolean isSupportingAddition(final Unit unit) {
        if (unit instanceof DurationUnit) {
            return (unit instanceof DurationUnit) && DURATION_UNITS_SUPPORTING_ADDITION.contains(((DurationUnit) unit).getUnitType());
        } else {
            return false;
        }
    }

    @Override
    public Optional<Unit> getUnit(final NumericExpression numericExpression) {
        return measureAdapter.getUnit(numericExpression);
    }

    @Override
    public Optional<Measure> getMeasure(NumericExpression numericExpression) {
        return measureAdapter.getMeasure(numericExpression);
    }

    @Override
    public Optional<Map<Measure, Integer>> getDimension(NumericExpression numericExpression) {
        return measureAdapter.getDimension(numericExpression);
    }

    private void initNamespace(final Iterable<Namespace> path, final Namespace namespace) {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.join(NAMESPACE_SEPARATOR, StreamSupport.stream(path.spliterator(), false).map(p -> p.getName()).collect(Collectors.toList())));
        if (sb.length() > 0) {
            sb.append(NAMESPACE_SEPARATOR);
        }
        sb.append(namespace.getName());

        final String key = sb.toString();

        namespaceCache.put(key, namespace);
        log.debug("Cached namespace: {}", key);

        final Iterable<Namespace> newPath = Iterables.concat(path, Collections.singleton(namespace));
        namespace.getPackages().stream().forEach(p -> initNamespace(newPath, p));
    }

    private Optional<Unit> getUnit(final PrimitiveTypedElement primitiveTypedElement) {
        if (primitiveTypedElement.getDataType().isMeasured()) {
            return Optional.of(((MeasuredType) primitiveTypedElement.getDataType()).getStoreUnit());
        } else {
            return Optional.empty();
        }
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
            final Iterable<Notifier> contents = resourceSet::getAllContents;
            return StreamSupport.stream(contents.spliterator(), false)
                    .filter(e -> e instanceof Unit).map(e -> (Unit) e)
                    .filter(u -> Objects.equals(u.getName(), nameOrSymbol) || Objects.equals(u.getSymbol(), nameOrSymbol))
                    .findAny();
        }
    }

    class PsmEntityMeasureAdapter extends MeasureAdapter<Measure, Unit> {

        PsmEntityMeasureAdapter(final Collection<Measure> measures) {
            super(measures);
        }

        @Override
        String getMeasureNamespace(final Measure measure) {
            return namespaceCache.values().stream()
                    .filter(ns -> ns.getElements().contains(measure))
                    .map(ns -> ns.getName())
                    .findAny().get();
        }

        @Override
        String getMeasureName(final Measure measure) {
            return measure.getName();
        }

        @Override
        Map<Measure, Integer> getBaseMeasures(final Measure measure) {
            if (measure instanceof DerivedMeasure) {
                final DerivedMeasure derivedMeasure = (DerivedMeasure) measure;
                final Map<Measure, Integer> base = new ConcurrentHashMap<>();
                derivedMeasure.getTerms().forEach(t ->
                        getBaseMeasures(getMeasure(t.getUnit())).forEach((m, exponent) -> {
                            final int currentExponent = base.getOrDefault(getMeasure(t.getUnit()), 0);
                            final int calculatedBaseExponent = exponent * t.getExponent();
                            final int newExponent = currentExponent + calculatedBaseExponent;
                            if (newExponent != 0) {
                                base.put(m, newExponent);
                            } else {
                                base.remove(m);
                            }
                        })
                );
                return Collections.unmodifiableMap(base);
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
            return PsmEntityModelAdapter.this.isSupportingAddition(unit);
        }

        @Override
        Optional<Unit> getUnitByNameOrSymbol(final Optional<MeasureName> measureName, final String nameOrSymbol) {
            return PsmEntityModelAdapter.this.getUnitByNameOrSymbol(measureName, nameOrSymbol);
        }

        @Override
        Optional<Unit> getUnit(final NumericAttribute numericAttribute) {

            return getAttribute((EntityType) numericAttribute.getObjectExpression().getObjectType(PsmEntityModelAdapter.this), numericAttribute.getAttributeName())
                    .map(a -> PsmEntityModelAdapter.this.getUnit(a)).get();
        }
    }
}
