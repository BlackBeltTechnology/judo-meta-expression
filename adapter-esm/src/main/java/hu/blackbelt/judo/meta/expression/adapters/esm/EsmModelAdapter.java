package hu.blackbelt.judo.meta.expression.adapters.esm;

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
import hu.blackbelt.judo.meta.expression.constant.MeasuredInteger;
import hu.blackbelt.judo.meta.expression.numeric.NumericAttribute;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.slf4j.Logger;

import java.lang.Class;
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
public class EsmModelAdapter implements ModelAdapter<NamespaceElement, Primitive, PrimitiveTypedElement, EnumerationType, hu.blackbelt.judo.meta.esm.structure.Class, ReferenceTypedElement, Sequence, Measure, Unit> {

    private static final String NAMESPACE_SEPARATOR = "::";
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(EsmModelAdapter.class);

    private static Pattern MEASURE_NAME_PATTERN = Pattern.compile("^(.*)\\.([^\\.]+)$");

    private final ResourceSet esmResourceSet;
    private final MeasureProvider<Measure, Unit> measureProvider;
    private final MeasureAdapter<Measure, Unit> measureAdapter;
    private final EsmUtils esmUtils;


    public EsmModelAdapter(final ResourceSet esmResourceSet, final ResourceSet measureResourceSet) {
        this.esmResourceSet = esmResourceSet;

        esmUtils = new EsmUtils(esmResourceSet);
        measureProvider = new EsmMeasureProvider(measureResourceSet);

        measureAdapter = new MeasureAdapter<>(measureProvider, this);
    }

    @Override
    public Optional<TypeName> getTypeName(final NamespaceElement namespaceElement) {
        return getEsmElement(Namespace.class)
                .filter(ns -> ns.getElements().contains(namespaceElement))
                .map(ns -> newTypeNameBuilder().withNamespace(getNamespaceFQName(ns)).withName(namespaceElement.getName()).build())
                .findAny();
    }

    @Override
    public Optional<TypeName> getEnumerationTypeName(EnumerationType enumeration) {
        return getTypeName(enumeration);
    }

    @Override
    public Optional<MeasureName> getMeasureName(Measure measure) {
        return measureProvider.getMeasures()
                .filter(mn -> Objects.equals(measureProvider.getMeasureNamespace(mn), measureProvider.getMeasureNamespace(measure)) && Objects.equals(mn.getName(), measure.getName()))
                .findAny().map(m -> newMeasureNameBuilder().withName(m.getName()).withNamespace(measureProvider.getMeasureNamespace(measure)).build());
    }

    @Override
    public Optional<? extends NamespaceElement> get(final TypeName elementName) {
        final Optional<Namespace> namespace = getEsmElement(Namespace.class)
                .filter(p -> Objects.equals(getNamespaceFQName(p), elementName.getNamespace().replace(".", NAMESPACE_SEPARATOR)))
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
        return namespaceElement instanceof EntityType;
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
        return isNumeric(primitive) && ((NumericType)primitive).getScale() == 0;
    }

    @Override
    public boolean isDecimal(Primitive primitive) {
        return isNumeric(primitive) && ((NumericType)primitive).getScale() > 0;
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
        return primitive instanceof  DateType;
    }

    @Override
    public boolean isTimestamp(Primitive primitive) {
        return primitive instanceof TimestampType;
    }

    @Override
    public boolean isCustom(Primitive primitive) {
        return isBoolean(primitive)
                && isNumeric(primitive)
                && isString(primitive)
                && isEnumeration(primitive)
                && isDate(primitive)
                && isTimestamp(primitive);
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
        return measureAdapter.getDimension(numericExpression).map( dimensions -> {
            Map<Measure, Integer> measureMap = new HashMap<>();
            dimensions.entrySet().stream().forEach( entry -> {
                MeasureAdapter.MeasureId measureId = entry.getKey();
                Optional<Measure> measure = measureProvider.getMeasure(measureId.getNamespace(), measureId.getName());
                measure.ifPresent(m -> measureMap.put(m, entry.getValue()));
            });
            return measureMap;
        });
    }

    @Override
    public EList<hu.blackbelt.judo.meta.esm.structure.Class> getAllClasses() {
        return ECollections.asEList(getEsmElement(hu.blackbelt.judo.meta.esm.structure.Class.class).collect(toList()));
    }

    @Override
    public EList<EnumerationType> getAllEnums() {
        return ECollections.asEList(getEsmElement(EnumerationType.class).collect(toList()));
    }

    @Override
    public EList<NamespaceElement> getAllStaticSequences() {
        return ECollections.asEList(getEsmElement(NamespaceSequence.class).collect(toList()));
    }

    @Override
    public Optional<? extends Sequence> getSequence(hu.blackbelt.judo.meta.esm.structure.Class clazz, String sequenceName) {
        return ((EntityType)clazz).getSequences().stream().filter(s -> Objects.equals(s.getName(), sequenceName)).findAny();
    }

    @Override
    public boolean isSequence(NamespaceElement namespaceElement) {
        return namespaceElement instanceof Sequence;
    }

    @Override
    public boolean isDerivedAttribute(PrimitiveTypedElement attribute) {
        return attribute instanceof DataMember && ((DataMember)attribute).isProperty();
    }

    @Override
    public Optional<String> getAttributeGetter(PrimitiveTypedElement attribute) {
        Optional<String> result = Optional.empty();
        if (isDerivedAttribute(attribute)) {
                result = Optional.of(((DataMember)attribute).getGetterExpression().getExpression());
        }
        return result;
    }

    @Override
    public boolean isDerivedReference(ReferenceTypedElement reference) {
        if (reference instanceof ReferenceAccessor && reference instanceof OneWayRelationMember) {
            return ((OneWayRelationMember)reference).isProperty();
        } else {
            return false;
        }
    }

    @Override
    public Optional<String> getReferenceGetter(ReferenceTypedElement reference) {
        Optional<String> result = Optional.empty();
        if (isDerivedReference(reference)) {
            result = Optional.of(((ReferenceAccessor)reference).getGetterExpression().getExpression());
        }
        return result;
    }

    @Override
    public EList<Measure> getAllMeasures() {
        return ECollections.asEList(measureProvider.getMeasures().collect(toList()));
    }

    <T> Stream<T> getEsmElement(final Class<T> clazz) {
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
        if (attribute.getDataType() instanceof  MeasuredType) {
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
                                        && ((OneWayRelationMember)relation).isContainment()
                                        && EcoreUtil.equals(relation.getTarget(), clazz)))
                .flatMap(
                        container -> Stream.concat(
                                getAllGeneralizations(container).stream().map(Generalization::getTarget),
                                Collections.singleton(container).stream()))
                .collect(toList()));
    }

    public String getNamespaceFQName(Namespace namespace) {
        if (namespace instanceof NamespaceElement) {
            return EsmUtils.getNamespaceElementFQName((NamespaceElement)namespace);
        } else if (namespace instanceof Model) {
            return ((Model)namespace).getName();
        } else {
            return String.join(NAMESPACE_SEPARATOR, namespace.getElements().stream().map(NamedElement::getName).collect(toList()));
        }
    }

}
