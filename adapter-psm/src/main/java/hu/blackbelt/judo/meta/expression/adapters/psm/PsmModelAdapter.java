package hu.blackbelt.judo.meta.expression.adapters.psm;

import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.ReferenceSelector;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal;
import hu.blackbelt.judo.meta.expression.constant.MeasuredInteger;
import hu.blackbelt.judo.meta.expression.numeric.NumericAttribute;
import hu.blackbelt.judo.meta.psm.PsmUtils;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.data.Containment;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.data.PrimitiveTypedElement;
import hu.blackbelt.judo.meta.psm.data.ReferenceTypedElement;
import hu.blackbelt.judo.meta.psm.measure.Measure;
import hu.blackbelt.judo.meta.psm.measure.MeasuredType;
import hu.blackbelt.judo.meta.psm.measure.Unit;
import hu.blackbelt.judo.meta.psm.namespace.Namespace;
import hu.blackbelt.judo.meta.psm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.type.EnumerationType;
import hu.blackbelt.judo.meta.psm.type.Primitive;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newMeasureNameBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;
import static java.util.stream.Collectors.toList;

/**
 * Model adapter for PSM models.
 */
public class PsmModelAdapter implements ModelAdapter<NamespaceElement, Primitive, PrimitiveTypedElement, EnumerationType, EntityType, ReferenceTypedElement, Measure, Unit> {

    private static final String NAMESPACE_SEPARATOR = "::";
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PsmModelAdapter.class);
    private final ResourceSet psmResourceSet;
    private final MeasureProvider<Measure, Unit> measureProvider;
    private final MeasureAdapter measureAdapter;

    /**
     * Create PSM model adapter for expressions.
     *
     * @param psmResourceSet     PSM resource set
     * @param measureResourceSet PSM resource set containing measures (must be the one that PSM resource is referencing)
     */
    public PsmModelAdapter(final ResourceSet psmResourceSet, final ResourceSet measureResourceSet) {
        this.psmResourceSet = psmResourceSet;

        measureProvider = new PsmMeasureProvider(measureResourceSet);

        measureAdapter = new MeasureAdapter<>(measureProvider, this);
    }

    @Override
    public Optional<TypeName> getTypeName(final NamespaceElement namespaceElement) {
        return getPsmElement(Namespace.class)
                .filter(ns -> ns.getElements().contains(namespaceElement))
                .map(ns -> newTypeNameBuilder().withNamespace(getNamespaceFQName(ns)).withName(namespaceElement.getName()).build())
                .findAny();
    }

    @Override
    public Optional<TypeName> getEnumerationTypeName(EnumerationType enumeration) {
        return getTypeName(enumeration);
    }

    @Override
    public Optional<? extends NamespaceElement> get(final TypeName elementName) {
        final Optional<Namespace> namespace = getPsmElement(Namespace.class)
                .filter(ns -> Objects.equals(getNamespaceFQName(ns), elementName.getNamespace().replace(".", NAMESPACE_SEPARATOR)))
                .findAny();
        if (namespace.isPresent()) {
            return namespace.get().getElements().stream()
                    .filter(e -> Objects.equals(e.getName(), elementName.getName()))
                    .findAny();
        } else {
            log.warn("Namespace not found: {}", elementName.getNamespace());
            return null;
        }
    }

    @Override
    public Optional<? extends Measure> get(final MeasureName measureName) {
        return measureProvider.getMeasure(measureName.getNamespace().replace(".", NAMESPACE_SEPARATOR), measureName.getName());
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
    public boolean isCollection(ReferenceSelector referenceSelector) {
        return ((ReferenceTypedElement) referenceSelector.getReference(this)).isCollection();
    }

    @Override
    public boolean isCollectionReference(ReferenceTypedElement reference) {
        return reference.isCollection();
    }

    @Override
    public EntityType getTarget(ReferenceTypedElement reference) {
        return reference.getTarget();
    }

    @Override
    public Optional<? extends PrimitiveTypedElement> getAttribute(EntityType clazz, String attributeName) {
        return Optional.ofNullable(clazz.getAttribute(attributeName));
    }

    @Override
    public Optional<? extends Primitive> getAttributeType(PrimitiveTypedElement attribute) {
        return Optional.ofNullable(attribute.getDataType());
    }

    @Override
    public Optional<? extends Primitive> getAttributeType(EntityType clazz, String attributeName) {
        return getAttribute(clazz, attributeName).map(PrimitiveTypedElement::getDataType);
    }

    @Override
    public Collection<? extends EntityType> getSuperTypes(EntityType clazz) {
        return PsmUtils.getAllSuperEntityTypes(clazz);
    }

    @Override
    public boolean isNumeric(Primitive primitive) {
        return primitive.isNumeric();
    }

    @Override
    public boolean isInteger(Primitive primitive) {
        return primitive.isInteger();
    }

    @Override
    public boolean isDecimal(Primitive primitive) {
        return primitive.isDecimal();
    }

    @Override
    public boolean isBoolean(Primitive primitive) {
        return primitive.isBoolean();
    }

    @Override
    public boolean isString(Primitive primitive) {
        return primitive.isString();
    }

    @Override
    public boolean isEnumeration(Primitive primitive) {
        return primitive.isEnumeration();
    }

    @Override
    public boolean isDate(Primitive primitive) {
        return primitive.isDate();
    }

    @Override
    public boolean isTimestamp(Primitive primitive) {
        return primitive.isTimestamp();
    }

    @Override
    public boolean isCustom(Primitive primitive) {
        return !primitive.isBoolean()
                && !primitive.isNumeric()
                && !primitive.isString()
                && !primitive.isEnumeration()
                && !primitive.isDate()
                && !primitive.isTimestamp();
    }

    @Override
    public boolean isMeasured(NumericExpression numericExpression) {
        return measureAdapter.isMeasured(numericExpression);
    }

    @Override
    public boolean contains(EnumerationType enumeration, String memberName) {
        return enumeration.contains(memberName);
    }

    @Override
    public Optional<Measure> getDurationMeasure() {
        return measureAdapter.getDurationMeasure();
    }

    @Override
    public boolean isDurationSupportingAddition(Unit unit) {
        return measureProvider.isDurationSupportingAddition(unit);
    }

    @Override
    public Optional<Measure> getMeasure(NumericExpression numericExpression) {
        return measureAdapter.getMeasure(numericExpression);
    }

    @Override
    public Optional<Unit> getUnit(NumericExpression numericExpression) {
        if (numericExpression instanceof NumericAttribute) {
            final EntityType objectType = (EntityType) ((NumericAttribute) numericExpression).getObjectExpression().getObjectType(this);
            final String attributeName = ((NumericAttribute) numericExpression).getAttributeName();

            return getUnit(objectType, attributeName);
            //-------------------
        } else if (numericExpression instanceof MeasuredDecimal) {
            final MeasuredDecimal measuredDecimal = (MeasuredDecimal) numericExpression;
            return measureAdapter.getUnit(
                    measuredDecimal.getMeasure() != null ? Optional.ofNullable(measuredDecimal.getMeasure().getNamespace()) : Optional.empty(),

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

    Optional<Unit> getUnit(final EntityType objectType, final String attributeName) {
        final Optional<Optional<Unit>> unit = objectType.getAttributes().stream()
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

    Optional<Unit> getUnit(final Attribute attribute) {
        if (attribute.getDataType().isMeasured()) {
            return Optional.of(((MeasuredType) attribute.getDataType()).getStoreUnit());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Map<Measure, Integer>> getDimension(NumericExpression numericExpression) {
        return measureAdapter.getDimension(numericExpression);
    }

    @Override
    public EList<EntityType> getAllClasses() {
        return ECollections.asEList(getPsmElement(EntityType.class).collect(toList()));
    }

    @Override
    public EList<EnumerationType> getAllEnums() {
        return ECollections.asEList(getPsmElement(EnumerationType.class).collect(toList()));
    }

    @Override
    public EList<Measure> getAllMeasures() {
        return ECollections.asEList(measureProvider.getMeasures().collect(toList()));
    }

    <T> Stream<T> getPsmElement(final Class<T> clazz) {
        final Iterable<Notifier> psmContents = psmResourceSet::getAllContents;
        return StreamSupport.stream(psmContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }

    /**
     * Get fully qualified name of a given namespace.
     *
     * @param namespace namespace
     * @return FQ name
     */
    private String getNamespaceFQName(final Namespace namespace) {
        final Optional<Namespace> containerNamespace;
        if (namespace instanceof Package) {
            containerNamespace = PsmUtils.getNamespaceOfPackage((Package) namespace);
        } else {
            containerNamespace = Optional.empty();
        }

        return (containerNamespace.isPresent() ? getNamespaceFQName(containerNamespace.get()) + NAMESPACE_SEPARATOR : "") + namespace.getName();
    }

    @Override
    public EList<EntityType> getContainerTypesOf(final EntityType entityType) {
        // TODO - change getRelations to getAllRelations
        return ECollections.asEList(getPsmElement(EntityType.class)
                .filter(container -> container.getRelations().stream().anyMatch(c -> (c instanceof Containment) && EcoreUtil.equals(c.getTarget(), entityType)))
                .flatMap(container -> Stream.concat(PsmUtils.getAllSuperEntityTypes(container).stream(), Collections.singleton(container).stream()))
                .collect(toList()));
    }

    @Override
    public Optional<MeasureName> getMeasureName(Measure measure) {
        return measureProvider.getMeasures()
                .filter(mn -> Objects.equals(mn.getNamespace(), measure.getNamespace()) && Objects.equals(mn.getName(), measure.getName()))
                .findAny().map(m -> newMeasureNameBuilder().withName(m.getName()).withNamespace(m.getSymbol()).build());
    }


}