package hu.blackbelt.judo.meta.expression.adapters.esm;

import hu.blackbelt.judo.meta.esm.accesspoint.ActorType;
import hu.blackbelt.judo.meta.esm.measure.Measure;
import hu.blackbelt.judo.meta.esm.measure.MeasuredType;
import hu.blackbelt.judo.meta.esm.measure.Unit;
import hu.blackbelt.judo.meta.esm.namespace.Model;
import hu.blackbelt.judo.meta.esm.namespace.NamedElement;
import hu.blackbelt.judo.meta.esm.namespace.Namespace;
import hu.blackbelt.judo.meta.esm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.esm.runtime.EsmUtils;
import hu.blackbelt.judo.meta.esm.structure.*;
import hu.blackbelt.judo.meta.esm.type.*;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.ReferenceSelector;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal;
import hu.blackbelt.judo.meta.expression.numeric.NumericAttribute;
import hu.blackbelt.judo.meta.expression.variable.MeasuredDecimalEnvironmentVariable;
import hu.blackbelt.judo.meta.esm.measure.DurationUnit;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.slf4j.Logger;

import java.lang.Class;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newMeasureNameBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;
import static java.util.stream.Collectors.toList;

/**
 * Model adapter for ESM models.
 */
public class EsmModelAdapter implements ModelAdapter<NamespaceElement, Primitive, EnumerationType, hu.blackbelt.judo.meta.esm.structure.Class, PrimitiveTypedElement, ReferenceTypedElement, TransferObjectType, PrimitiveTypedElement, ReferenceTypedElement, Sequence, Measure, Unit> {

    private static final String NAMESPACE_SEPARATOR = "::";
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(EsmModelAdapter.class);

    private static Pattern MEASURE_NAME_PATTERN = Pattern.compile("^(.*)\\.([^\\.]+)$");

    private final ResourceSet esmResourceSet;
    private final MeasureProvider<Measure,Unit> measureProvider;
    private final MeasureAdapter<NamespaceElement, Primitive, EnumerationType, hu.blackbelt.judo.meta.esm.structure.Class, PrimitiveTypedElement, ReferenceTypedElement, TransferObjectType, PrimitiveTypedElement, ReferenceTypedElement, Sequence, Measure, Unit> measureAdapter;

    public EsmModelAdapter(final ResourceSet esmResourceSet, final ResourceSet measureResourceSet) {
        this.esmResourceSet = esmResourceSet;
        measureProvider = new EsmMeasureProvider(measureResourceSet);
        measureAdapter = new MeasureAdapter<>(measureProvider, this);
    }

    @Override
    public Optional<TypeName> buildTypeName(final NamespaceElement namespaceElement) {
        return getEsmElement(Namespace.class)
                .filter(ns -> ns.getElements().contains(namespaceElement))
                .map(ns -> newTypeNameBuilder().withNamespace(getNamespaceFQName(ns)).withName(namespaceElement.getName()).build())
                .findAny();
    }

    @Override
    public Optional<MeasureName> buildMeasureName(Measure measure) {
        return measureProvider.getMeasures()
                .filter(mn -> Objects.equals(measureProvider.getMeasureNamespace(mn), measureProvider.getMeasureNamespace(measure)) && Objects.equals(mn.getName(), measure.getName()))
                .findAny().map(m -> newMeasureNameBuilder().withName(m.getName()).withNamespace(measureProvider.getMeasureNamespace(measure)).build());
    }

    @Override
    public Optional<? extends NamespaceElement> get(final TypeName elementName) {
        final Optional<Namespace> namespace = getEsmElement(Namespace.class)
                .filter(p -> Objects.equals(getNamespaceFQName(p),
                        elementName.getNamespace() != null ? elementName.getNamespace().replace(".", NAMESPACE_SEPARATOR) : ""))
                .findAny();

        if (namespace.isPresent()) {
            return namespace.get().getElements().stream()
                    .filter(e -> Objects.equals(e.getName(), elementName.getName()))
                    .findAny();
        } else {
            log.warn("Namespace not found: {}", elementName.getNamespace());
            return Optional.empty();
        }
    }

    @Override
    public Optional<? extends Measure> get(MeasureName measureName) {
        return measureProvider.getMeasure(measureName.getNamespace().replace(".", NAMESPACE_SEPARATOR), measureName.getName());
    }

    @Override
    public boolean isObjectType(NamespaceElement namespaceElement) {
        return namespaceElement instanceof hu.blackbelt.judo.meta.esm.structure.Class;
    }

    @Override
    public boolean isPrimitiveType(NamespaceElement namespaceElement) {
        return namespaceElement instanceof Primitive;
    }

    @Override
    public boolean isMeasuredType(Primitive primitiveType) {
        return primitiveType instanceof MeasuredType;
    }

    @Override
    public Optional<Measure> getMeasureOfType(Primitive primitiveType) {
        return getUnitOfType(primitiveType).map(measureAdapter::getMeasure);
    }

    @Override
    public Optional<Unit> getUnitOfType(Primitive primitiveType) {
        if (isMeasuredType(primitiveType)) {
            return Optional.ofNullable(((MeasuredType) primitiveType).getStoreUnit());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String getUnitName(Unit unit) {
        return unit.getName();
    }

    @Override
    public UnitFraction getUnitRates(Unit unit) {
        return new UnitFraction(BigDecimal.valueOf(unit.getRateDividend()), BigDecimal.valueOf(unit.getRateDivisor()));
    }

    @Override
    public UnitFraction getBaseDurationRatio(Unit unit, DurationType targetType) {
        if (!(unit instanceof DurationUnit)) {
            throw new IllegalArgumentException("Unit must be duration");
        };
        DurationUnit durationUnit = (DurationUnit) unit;
        // examples in comments when unit is day, the base unit is minute
        // example2 unit is millisecond, the base unit is minute
        BigDecimal dividendToBase = BigDecimal.valueOf(durationUnit.getRateDividend()); // 1440 |---| 1
        BigDecimal divisorToBase = BigDecimal.valueOf(durationUnit.getRateDivisor());  // 1 |---| 60000
        DurationType target;
        if (hu.blackbelt.judo.meta.esm.measure.DurationType.NANOSECOND.equals(durationUnit.getUnitType())) {
            target = DurationType.NANOSECOND;
        } else if (hu.blackbelt.judo.meta.esm.measure.DurationType.MICROSECOND.equals(durationUnit.getUnitType())) {
            target = DurationType.MICROSECOND;
        } else if (hu.blackbelt.judo.meta.esm.measure.DurationType.MILLISECOND.equals(durationUnit.getUnitType())) {
            target = DurationType.MILLISECOND;
        } else if (hu.blackbelt.judo.meta.esm.measure.DurationType.SECOND.equals(durationUnit.getUnitType())) {
            target = DurationType.SECOND;
        } else if (hu.blackbelt.judo.meta.esm.measure.DurationType.MINUTE.equals(durationUnit.getUnitType())) {
            target = DurationType.MINUTE;
        } else if (hu.blackbelt.judo.meta.esm.measure.DurationType.HOUR.equals(durationUnit.getUnitType())) {
            target = DurationType.HOUR;
        } else if (hu.blackbelt.judo.meta.esm.measure.DurationType.DAY.equals(durationUnit.getUnitType())) {
            target = DurationType.DAY;
        } else if (hu.blackbelt.judo.meta.esm.measure.DurationType.WEEK.equals(durationUnit.getUnitType())) {
            target = DurationType.WEEK;
        } else {
            throw new IllegalArgumentException("No duration ration is valid for month and year.");
        }
        if (targetType.equals(DurationType.SECOND)) {
            UnitFraction secondFraction = target.getSecondUnitFraction(); // 86400/1 |---| 1/1000
            // however, we need to return the ratio calculated in the base unit. The base unit is minute, so the result must be 60/1
            // so the result is: secondFraction * divisorToBase/dividendToBase, that is
            BigDecimal newDividend = secondFraction.getDividend().multiply(divisorToBase);
            BigDecimal newDivisor = secondFraction.getDivisor().multiply(dividendToBase);
            return new UnitFraction(newDividend, newDivisor);
        } else if (targetType.equals(DurationType.DAY)) {
            UnitFraction dayFraction = target.getDayUnitFraction(); // 86400/1 |---| 1/1000
            // however, we need to return the ratio calculated in the base unit. The base unit is minute, so the result must be 60/1
            // so the result is: dayFraction * divisorToBase/dividendToBase, that is
            BigDecimal newDividend = dayFraction.getDividend().multiply(divisorToBase);
            BigDecimal newDivisor = dayFraction.getDivisor().multiply(dividendToBase);
            return new UnitFraction(newDividend, newDivisor);
        } else {
            throw new IllegalArgumentException("Only second and day duration type is supported.");
        }

    }

    @Override
    public Optional<? extends ReferenceTypedElement> getReference(hu.blackbelt.judo.meta.esm.structure.Class clazz, String referenceName) {
        // collect all classes that can have the given reference, ie. itself and all its parents
        EList<Generalization> generalizations = getAllGeneralizations(clazz);
        List<hu.blackbelt.judo.meta.esm.structure.Class> base = generalizations.stream().map(Generalization::getTarget).collect(toList());
        base.add(clazz);
        Optional<Optional<RelationFeature>> relation = base.stream()
                .filter(e -> e.getRelations().stream().anyMatch(r -> Objects.equals(referenceName, r.getName())))
                .map(e -> e.getRelations().stream().filter(r -> Objects.equals(referenceName, r.getName())).findFirst())
                .findFirst();
        return relation.orElse(Optional.empty());
    }

    private EList<Generalization> getAllGeneralizations(hu.blackbelt.judo.meta.esm.structure.Class type) {
        EList<Generalization> generalizations = new UniqueEList<>();
        generalizations.addAll(type.getGeneralizations());
        int index = 0;
        while (index < generalizations.size()) {
            for (Generalization parent : generalizations.get(index).getTarget().getGeneralizations()) {
                if (!generalizations.contains(parent)) {
                    generalizations.add(parent);
                }
            }
            index++;
        }
        return generalizations;
    }

    @Override
    public Optional<? extends PrimitiveTypedElement> getAttribute(hu.blackbelt.judo.meta.esm.structure.Class clazz, String attributeName) {
        EList<Generalization> generalizations = getAllGeneralizations(clazz);
        List<hu.blackbelt.judo.meta.esm.structure.Class> base = generalizations.stream().map(Generalization::getTarget).collect(toList());
        base.add(clazz);
        Optional<Optional<DataFeature>> attribute = base.stream()
                .filter(e -> e.getAttributes().stream().anyMatch(a -> Objects.equals(attributeName, a.getName()))) // classes having the attribute
                .map(e -> e.getAttributes().stream().filter(a -> Objects.equals(attributeName, a.getName())).findFirst()) // get the first matching attribute
                .findFirst();
        return attribute.orElse(Optional.empty());
    }

    @Override
    public Optional<? extends Primitive> getAttributeType(PrimitiveTypedElement attribute) {
        return Optional.ofNullable(attribute.getDataType());
    }

    @Override
    public Optional<? extends Primitive> getAttributeType(hu.blackbelt.judo.meta.esm.structure.Class clazz, String attributeName) {
        return getAttribute(clazz, attributeName).map(PrimitiveTypedElement::getDataType);
    }

    @Override
    public boolean isCollection(ReferenceSelector referenceSelector) {
        ReferenceTypedElement reference = (ReferenceTypedElement) referenceSelector.getReference(this);
        return isCollectionReference(reference);
    }

    @Override
    public boolean isCollectionReference(ReferenceTypedElement reference) {
        return reference.getUpper() == -1 || reference.getUpper() > 1;
    }

    @Override
    public hu.blackbelt.judo.meta.esm.structure.Class getTarget(ReferenceTypedElement reference) {
        return reference.getTarget();
    }

    @Override
    public Collection<? extends hu.blackbelt.judo.meta.esm.structure.Class> getSuperTypes(hu.blackbelt.judo.meta.esm.structure.Class clazz) {
        return getAllGeneralizations(clazz).stream().map(Generalization::getTarget).collect(Collectors.toList());
    }

    @Override
    public boolean isNumeric(Primitive primitive) {
        return primitive instanceof NumericType;
    }

    @Override
    public boolean isInteger(Primitive primitive) {
        return isNumeric(primitive) && ((NumericType) primitive).getScale() == 0;
    }

    @Override
    public boolean isDecimal(Primitive primitive) {
        return isNumeric(primitive) && ((NumericType) primitive).getScale() > 0;
    }

    @Override
    public boolean isBoolean(Primitive primitive) {
        return primitive instanceof BooleanType;
    }

    @Override
    public boolean isString(Primitive primitive) {
        return primitive instanceof StringType;
    }

    @Override
    public boolean isEnumeration(Primitive primitive) {
        return primitive instanceof EnumerationType;
    }

    @Override
    public boolean isDate(Primitive primitive) {
        return primitive instanceof DateType;
    }

    @Override
    public boolean isTimestamp(Primitive primitive) {
        return primitive instanceof TimestampType;
    }

    @Override
    public boolean isCustom(Primitive primitive) {
        return !isBoolean(primitive)
                && !isNumeric(primitive)
                && !isString(primitive)
                && !isEnumeration(primitive)
                && !isDate(primitive)
                && !isTimestamp(primitive);
    }

    @Override
    public boolean isMeasured(NumericExpression numericExpression) {
        return measureAdapter.isMeasured(numericExpression);
    }

    @Override
    public boolean contains(EnumerationType enumeration, String memberName) {
        return enumeration.getMembers().stream().filter(member -> Objects.equals(member.getName(), memberName)).findAny().isPresent();
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
    public Optional<Unit> getUnit(NumericExpression numericExpression) {
        if (numericExpression instanceof NumericAttribute) {
            hu.blackbelt.judo.meta.esm.structure.Class objectType = (hu.blackbelt.judo.meta.esm.structure.Class) ((NumericAttribute) numericExpression).getObjectExpression().getObjectType(this);
            String attributeName = ((NumericAttribute) numericExpression).getAttributeName();

            return getUnit(objectType, attributeName);
        } else if (numericExpression instanceof MeasuredDecimal) {
            final MeasuredDecimal measuredDecimal = (MeasuredDecimal) numericExpression;
            return measureAdapter.getUnit(measuredDecimal.getMeasure() != null ? Optional.ofNullable(measuredDecimal.getMeasure().getNamespace()) : Optional.empty(),
                    measuredDecimal.getMeasure() != null ? Optional.ofNullable(measuredDecimal.getMeasure().getName()) : Optional.empty(),
                    measuredDecimal.getUnitName());
        } else if (numericExpression instanceof MeasuredDecimalEnvironmentVariable) {
            final MeasuredDecimalEnvironmentVariable measuredDecimal = (MeasuredDecimalEnvironmentVariable) numericExpression;
            return measureAdapter.getUnit(measuredDecimal.getMeasure() != null ? Optional.ofNullable(measuredDecimal.getMeasure().getNamespace()) : Optional.empty(),
                    measuredDecimal.getMeasure() != null ? Optional.ofNullable(measuredDecimal.getMeasure().getName()) : Optional.empty(),
                    measuredDecimal.getUnitName());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public EList<Unit> getUnits(Measure measure) {
        return measureProvider.getUnits(measure);
    }

    @Override
    public Optional<Map<Measure, Integer>> getDimension(final NumericExpression numericExpression) {
        return measureAdapter.getDimension(numericExpression).map(dimensions -> {
            Map<Measure, Integer> measureMap = new HashMap<>();
            dimensions.entrySet().stream().forEach(entry -> {
                MeasureAdapter.MeasureId measureId = entry.getKey();
                Optional<Measure> measure = measureProvider.getMeasure(measureId.getNamespace(), measureId.getName());
                measure.ifPresent(m -> measureMap.put(m, entry.getValue()));
            });
            return measureMap;
        });
    }

    @Override
    public EList<hu.blackbelt.judo.meta.esm.structure.Class> getAllEntityTypes() {
        return ECollections.asEList(getEsmElement(hu.blackbelt.judo.meta.esm.structure.EntityType.class).collect(toList()));
    }

    @Override
    public EList<EnumerationType> getAllEnums() {
        return ECollections.asEList(getEsmElement(EnumerationType.class).collect(toList()));
    }

    @Override
    public EList<Primitive> getAllPrimitiveTypes() {
        return ECollections.asEList(getEsmElement(Primitive.class).collect(toList()));
    }

    @Override
    public EList<NamespaceElement> getAllStaticSequences() {
        return ECollections.asEList(getEsmElement(NamespaceSequence.class).collect(toList()));
    }

    @Override
    public Optional<? extends Sequence> getSequence(hu.blackbelt.judo.meta.esm.structure.Class clazz, String sequenceName) {
        if (clazz instanceof EntityType) {
            return ((EntityType) clazz).getSequences().stream().filter(s -> Objects.equals(s.getName(), sequenceName)).findAny();
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean isSequence(NamespaceElement namespaceElement) {
        return namespaceElement instanceof Sequence;
    }

    @Override
    public boolean isDerivedAttribute(PrimitiveTypedElement attribute) {
        return attribute instanceof DataMember && ((DataMember) attribute).getMemberType() == MemberType.DERIVED;
    }

    @Override
    public boolean isDerivedTransferAttribute(PrimitiveTypedElement attribute) {
        return isDerivedAttribute(attribute);
    }

    @Override
    public Optional<String> getAttributeGetter(PrimitiveTypedElement attribute) {
        Optional<String> result = Optional.empty();
        if (isDerivedAttribute(attribute)) {
            result = Optional.of(((DataMember) attribute).getGetterExpression());
        }
        return result;
    }

    @Override
    public Optional<String> getTransferAttributeGetter(PrimitiveTypedElement attribute) {
        return getAttributeGetter(attribute);
    }

    @Override
    public Optional<String> getAttributeSetter(PrimitiveTypedElement attribute) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getAttributeDefault(PrimitiveTypedElement attribute) {
        return Optional.empty();
    }

    @Override
    public boolean isDerivedReference(ReferenceTypedElement reference) {
        if (reference instanceof OneWayRelationMember) {
            return ((OneWayRelationMember) reference).getMemberType() == MemberType.DERIVED;
        } else {
            return false;
        }
    }

    @Override
    public boolean isDerivedTransferRelation(ReferenceTypedElement relation) {
        return isDerivedReference(relation);
    }

    @Override
    public Optional<String> getReferenceGetter(ReferenceTypedElement reference) {
        Optional<String> result = Optional.empty();
        if (isDerivedReference(reference) && reference instanceof OneWayRelationMember) {
            result = Optional.of(((OneWayRelationMember) reference).getGetterExpression());
        } else if (reference instanceof StaticNavigation) {
            StaticNavigation staticNavigation = (StaticNavigation) reference;
            if (staticNavigation.getGetterExpression() != null) {
                result = Optional.of(staticNavigation.getGetterExpression());
            } else {
                result = Optional.empty();
            }
        }
        return result;
    }

    @Override
    public Optional<String> getTransferRelationGetter(ReferenceTypedElement relation) {
        return getReferenceGetter(relation);
    }

    @Override
    public Optional<String> getReferenceDefault(ReferenceTypedElement reference) {
        return Optional.of(reference)
                .filter(ref -> ref instanceof RelationFeature)
                .map(ref -> ((RelationFeature) ref).getDefaultExpression());
    }

    @Override
    public Optional<String> getReferenceRange(ReferenceTypedElement reference) {
        return Optional.of(reference)
                .filter(ref -> ref instanceof RelationFeature)
                .map(ref -> ((RelationFeature) ref).getRangeExpression());
    }

    @Override
    public Optional<String> getReferenceSetter(ReferenceTypedElement reference) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getTransferRelationSetter(ReferenceTypedElement relation) {
        return getReferenceSetter(relation);
    }

    @Override
    public Optional<String> getFilter(TransferObjectType transferObjectType) {
        return Optional.of(transferObjectType)
                .filter(TransferObjectType::isMapped)
                .map(TransferObjectType::getMapping)
                .map(Mapping::getFilter);
    }

    @Override
    public EList<Measure> getAllMeasures() {
        return ECollections.asEList(measureProvider.getMeasures().collect(toList()));
    }

    public <T> Stream<T> getEsmElement(final Class<T> clazz) {
        final Iterable<Notifier> esmContents = esmResourceSet::getAllContents;
        return StreamSupport.stream(esmContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }

    Optional<Unit> getUnit(final hu.blackbelt.judo.meta.esm.structure.Class objectType, final String attributeName) {
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

    Optional<Unit> getUnit(final DataFeature attribute) {
        if (attribute.getDataType() instanceof MeasuredType) {
            return Optional.ofNullable(((MeasuredType) attribute.getDataType()).getStoreUnit());
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

    @Override
    public EList<hu.blackbelt.judo.meta.esm.structure.Class> getContainerTypesOf(hu.blackbelt.judo.meta.esm.structure.Class clazz) {
        return ECollections.asEList(getEsmElement(hu.blackbelt.judo.meta.esm.structure.Class.class)
                .filter(
                        container -> container.getRelations().stream()
                                .anyMatch(relation -> (relation instanceof OneWayRelationMember)
                                        && ((OneWayRelationMember) relation).isComposition()
                                        && EcoreUtil.equals(relation.getTarget(), clazz)))
                .flatMap(
                        container -> Stream.concat(
                                getAllGeneralizations(container).stream().map(Generalization::getTarget),
                                Collections.singleton(container).stream()))
                .collect(toList()));
    }

    public String getNamespaceFQName(Namespace namespace) {
        if (namespace instanceof NamespaceElement) {
            return EsmUtils.getNamespaceElementFQName((NamespaceElement) namespace);
        } else if (namespace instanceof Model) {
            return ((Model) namespace).getName();
        } else {
            return String.join(NAMESPACE_SEPARATOR, namespace.getElements().stream().map(NamedElement::getName).collect(toList()));
        }
    }

    @Override
    public EList<TransferObjectType> getAllTransferObjectTypes() {
        return ECollections.asEList(getEsmElement(TransferObjectType.class).collect(toList()));
    }

    @Override
    public EList<TransferObjectType> getAllMappedTransferObjectTypes() {
        return ECollections.asEList(
                getEsmElement(TransferObjectType.class)
                        .filter(TransferObjectType::isMapped)
                        .collect(toList()));
    }

    @Override
    public EList<TransferObjectType> getAllUnmappedTransferObjectTypes() {
        return ECollections.asEList(
                getEsmElement(TransferObjectType.class)
                        .filter(to -> !(to instanceof EntityType) && !to.isMapped())
                        .collect(toList()));
    }

    @Override
    public Optional<hu.blackbelt.judo.meta.esm.structure.Class> getEntityTypeOfTransferObjectRelationTarget(TypeName transferObjectTypeName,
                                                                                                            String transferObjectRelationName) {

        Optional<? extends NamespaceElement> transferObjectType = get(transferObjectTypeName);

        if (!transferObjectType.isPresent()) {
            return Optional.empty();
        } else if (transferObjectType.get() instanceof hu.blackbelt.judo.meta.esm.structure.Class) {

            Optional<? extends ReferenceTypedElement> transferObjectRelation = getReference((hu.blackbelt.judo.meta.esm.structure.Class) transferObjectType.get(), transferObjectRelationName);

            if (!transferObjectRelation.isPresent()) {
                return Optional.empty();
            } else {
                TransferObjectType transferObjectRelationTarget = (TransferObjectType) getTarget(transferObjectRelation.get());
                if (transferObjectRelationTarget.isMapped()) {
                    return Optional.of(transferObjectRelationTarget.getMapping().getTarget());
                } else {
                    return Optional.empty();
                }
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean isCollectionReference(TypeName elementName, String referenceName) {
        Optional<? extends NamespaceElement> element = this.get(elementName);

        if (element.isPresent()) {
            if (element.get() instanceof hu.blackbelt.judo.meta.esm.structure.Class) {
                Optional<? extends ReferenceTypedElement> reference = getReference((hu.blackbelt.judo.meta.esm.structure.Class) element.get(), referenceName);
                if (reference.isPresent()) {
                    return isCollectionReference(reference.get());
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public Optional<hu.blackbelt.judo.meta.esm.structure.Class> getMappedEntityType(TransferObjectType mappedTransferObjectType) {
        return Optional.of(mappedTransferObjectType)
                .filter(TransferObjectType::isMapped)
                .map(TransferObjectType::getMapping)
                .map(Mapping::getTarget);
    }

    @Override
    public String getFqName(Object object) {
        if (object instanceof OneWayRelationMember) {
            return EsmUtils.getOneWayRelationFQName((OneWayRelationMember) object);
        } else if (object instanceof DataMember) {
            return EsmUtils.getDataMemberFQName((DataMember) object);
        } else if (object instanceof NamespaceElement) {
            return EsmUtils.getNamespaceElementFQName((NamespaceElement) object);
        } else {
            return null;
        }
    }

    @Override
    public Optional<String> getName(Object object) {
        return Optional.ofNullable(object)
                .filter(o -> object instanceof NamedElement)
                .map(o -> ((NamedElement) o).getName());
    }

    @Override
    public Collection<? extends PrimitiveTypedElement> getAttributes(hu.blackbelt.judo.meta.esm.structure.Class clazz) {
        return clazz.getAttributes();
    }

    @Override
    public Collection<? extends ReferenceTypedElement> getReferences(hu.blackbelt.judo.meta.esm.structure.Class clazz) {
        return clazz.getRelations();
    }

    @Override
    public Collection<? extends PrimitiveTypedElement> getTransferAttributes(TransferObjectType transferObjectType) {
        return transferObjectType.getAttributes();
    }

    @Override
    public Collection<? extends ReferenceTypedElement> getTransferRelations(TransferObjectType transferObjectType) {
        return transferObjectType.getRelations();
    }

    @Override
    public Primitive getTransferAttributeType(PrimitiveTypedElement transferAttribute) {
        return transferAttribute.getDataType();
    }

    @Override
	public List<NamespaceElement> getAllActorTypes() {
		return ECollections.asEList(getEsmElement(ActorType.class).collect(toList()));
	}

	@Override
	public TransferObjectType getPrincipal(NamespaceElement actorType) {
		if (actorType instanceof ActorType) {
			return ((ActorType)actorType).getPrincipal();
		} else {
			throw new IllegalArgumentException(String.format("Not an actor type: %s", actorType));
		}
	}
}
