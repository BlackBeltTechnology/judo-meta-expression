package hu.blackbelt.judo.meta.expression.adapters.asm;

import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.asm.runtime.AsmUtils.OperationBehaviour;
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
import hu.blackbelt.judo.meta.expression.variable.MeasuredDecimalEnvironmentVariable;
import hu.blackbelt.judo.meta.expression.variable.MeasuredIntegerEnvironmentVariable;
import hu.blackbelt.judo.meta.measure.Measure;
import hu.blackbelt.judo.meta.measure.Unit;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.slf4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newMeasureNameBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;
import static java.util.stream.Collectors.toList;

/**
 * Model adapter for ASM models.
 */
public class AsmModelAdapter implements
		ModelAdapter<EClassifier, EDataType, EEnum, EClass, EAttribute, EReference, EClass, EAttribute, EReference, EClassifier, Measure, Unit> {

	protected static final String NAMESPACE_SEPARATOR = "::";
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(AsmModelAdapter.class);

	private static Pattern MEASURE_NAME_PATTERN = Pattern.compile("^(.*)\\.([^\\.]+)$");

	private final MeasureProvider<Measure, Unit> measureProvider;
	private final MeasureAdapter<Measure, Unit> measureAdapter;

	private final AsmUtils asmUtils;

	public AsmModelAdapter(final ResourceSet asmResourceSet, final ResourceSet measureResourceSet) {
		asmUtils = new AsmUtils(asmResourceSet);
		measureProvider = new AsmMeasureProvider(measureResourceSet);

		measureAdapter = new MeasureAdapter<>(measureProvider, this);
	}

	private static Optional<MeasureName> parseMeasureName(final String name) {
		if (name == null) {
			return Optional.empty();
		} else {
			final Matcher m = MEASURE_NAME_PATTERN.matcher(name);
			if (m.matches()) {
				return Optional.of(newMeasureNameBuilder().withNamespace(m.group(1)).withName(m.group(2)).build());
			} else {
				return Optional.empty();
			}
		}
	}

	@Override
	public Optional<TypeName> buildTypeName(final EClassifier namespaceElement) {
		return getAsmElement(EPackage.class).filter(ns -> ns.getEClassifiers().contains(namespaceElement))
				.map(ns -> newTypeNameBuilder()
						.withNamespace(AsmUtils.getPackageFQName(ns).replace(".", NAMESPACE_SEPARATOR))
						.withName(namespaceElement.getName()).build())
				.findAny();
	}

	@Override
	public Optional<MeasureName> buildMeasureName(Measure measure) {
		return measureProvider.getMeasures()
				.filter(mn -> Objects.equals(mn.getNamespace(), measure.getNamespace())
						&& Objects.equals(mn.getName(), measure.getName()))
				.findAny()
				.map(m -> newMeasureNameBuilder().withName(m.getName()).withNamespace(m.getNamespace()).build());
	}

	@Override
	public Optional<? extends EClassifier> get(final TypeName elementName) {
		final Optional<EPackage> namespace = getAsmElement(EPackage.class).filter(p -> Objects
				.equals(AsmUtils.getPackageFQName(p), elementName.getNamespace().replace(NAMESPACE_SEPARATOR, ".")))
				.findAny();

		if (namespace.isPresent()) {
			return namespace.get().getEClassifiers().stream()
					.filter(e -> Objects.equals(e.getName(), elementName.getName())).findAny();
		} else {
			log.warn("Namespace not found: {}", elementName.getNamespace());
			return Optional.empty();
		}
	}

	@Override
	public Optional<? extends Measure> get(final MeasureName measureName) {
		return measureProvider.getMeasure(measureName.getNamespace().replace(".", NAMESPACE_SEPARATOR),
				measureName.getName());
	}

	@Override
	public boolean isObjectType(final EClassifier namespaceElement) {
		return namespaceElement instanceof EClass;
	}

	@Override
	public boolean isPrimitiveType(EClassifier namespaceElement) {
		return namespaceElement instanceof EDataType;
	}

	@Override
	public boolean isMeasuredType(EDataType primitiveType) {
		return getMeasureOfType(primitiveType).isPresent();
	}

	@Override
	public Optional<? extends Measure> getMeasureOfType(EDataType primitiveType) {
		Optional<? extends Measure> measure = AsmUtils.getExtensionAnnotationCustomValue(primitiveType, "measured", "measure", false)
				.flatMap(AsmModelAdapter::parseMeasureName)
				.flatMap(this::get);
		return measure;
	}

	@Override
	public Optional<Unit> getUnitOfType(EDataType primitiveType) {
		Optional<Unit> unit = getMeasureOfType(primitiveType)
				.flatMap(measure -> AsmUtils.getExtensionAnnotationCustomValue(primitiveType, "measured", "unit", false)
						.flatMap(unitName -> measureAdapter.getUnit(Optional.of(measure.getNamespace()), Optional.of(measure.getName()), unitName)
						));
		return unit;
	}

	@Override
	public String getUnitName(Unit unit) {
		return unit.getName();
	}

	@Override
	public Optional<? extends EReference> getReference(final EClass clazz, final String referenceName) {
		return clazz.getEAllReferences().stream().filter(r -> Objects.equals(r.getName(), referenceName)).findAny();
	}

	@Override
	public boolean isCollection(final ReferenceSelector referenceSelector) {
		return ((EReference) referenceSelector.getReference(this)).isMany();
	}

	@Override
	public boolean isCollectionReference(EReference reference) {
		return reference.isMany();
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
	public Optional<? extends EDataType> getAttributeType(EAttribute attribute) {
		return Optional.ofNullable(attribute.getEAttributeType());
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
	public boolean isDate(EDataType primitive) {
		return AsmUtils.isDate(primitive);
	}

	@Override
	public boolean isTimestamp(EDataType primitive) {
		return AsmUtils.isTimestamp(primitive);
	}

	@Override
	public boolean isCustom(EDataType primitive) {
		return !AsmUtils.isBoolean(primitive) && !AsmUtils.isNumeric(primitive) && !AsmUtils.isString(primitive)
				&& !AsmUtils.isEnumeration(primitive) && !AsmUtils.isDate(primitive)
				&& !AsmUtils.isTimestamp(primitive);
	}

	@Override
	public boolean isMeasured(final NumericExpression numericExpression) {
		return measureAdapter.isMeasured(numericExpression);
	}

	@Override
	public boolean contains(final EEnum enumeration, final String memberName) {
		return enumeration.getELiterals().stream().filter(l -> Objects.equals(l.getLiteral(), memberName)).findAny()
				.isPresent();
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
			final EClass objectType = (EClass) ((NumericAttribute) numericExpression).getObjectExpression()
					.getObjectType(this);
			final String attributeName = ((NumericAttribute) numericExpression).getAttributeName();

			return getUnit(objectType, attributeName);
		} else if (numericExpression instanceof MeasuredDecimal) {
			final MeasuredDecimal measuredDecimal = (MeasuredDecimal) numericExpression;
			return measureAdapter.getUnit(
					measuredDecimal.getMeasure() != null
							? Optional.ofNullable(measuredDecimal.getMeasure().getNamespace())
							: Optional.empty(),
					measuredDecimal.getMeasure() != null ? Optional.ofNullable(measuredDecimal.getMeasure().getName())
							: Optional.empty(),
					measuredDecimal.getUnitName());
		} else if (numericExpression instanceof MeasuredInteger) {
			final MeasuredInteger measuredInteger = (MeasuredInteger) numericExpression;
			return measureAdapter.getUnit(
					measuredInteger.getMeasure() != null
							? Optional.ofNullable(measuredInteger.getMeasure().getNamespace())
							: Optional.empty(),
					measuredInteger.getMeasure() != null ? Optional.ofNullable(measuredInteger.getMeasure().getName())
							: Optional.empty(),
					measuredInteger.getUnitName());
		} else if (numericExpression instanceof MeasuredDecimalEnvironmentVariable) {
			final MeasuredDecimalEnvironmentVariable measuredDecimal = (MeasuredDecimalEnvironmentVariable) numericExpression;
			return measureAdapter.getUnit(
					measuredDecimal.getMeasure() != null
							? Optional.ofNullable(measuredDecimal.getMeasure().getNamespace())
							: Optional.empty(),
					measuredDecimal.getMeasure() != null ? Optional.ofNullable(measuredDecimal.getMeasure().getName())
							: Optional.empty(),
					measuredDecimal.getUnitName());
		} else if (numericExpression instanceof MeasuredIntegerEnvironmentVariable) {
			final MeasuredIntegerEnvironmentVariable measuredInteger = (MeasuredIntegerEnvironmentVariable) numericExpression;
			return measureAdapter.getUnit(
					measuredInteger.getMeasure() != null
							? Optional.ofNullable(measuredInteger.getMeasure().getNamespace())
							: Optional.empty(),
					measuredInteger.getMeasure() != null ? Optional.ofNullable(measuredInteger.getMeasure().getName())
							: Optional.empty(),
					measuredInteger.getUnitName());
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
	public EList<EClass> getAllEntityTypes() {
		return ECollections
				.asEList(getAsmElement(EClass.class).filter(c -> AsmUtils.isEntityType(c)).collect(toList()));
	}

	@Override
	public EList<EEnum> getAllEnums() {
		return ECollections
				.asEList(getAsmElement(EEnum.class).filter(e -> AsmUtils.isEnumeration(e)).collect(toList()));
	}

	@Override
	public EList<EDataType> getAllPrimitiveTypes() {
		return ECollections.asEList(getAsmElement(EDataType.class).collect(toList()));
	}

	@Override
	public EList<EClassifier> getAllStaticSequences() {
		// TODO
		return ECollections.emptyEList();
	}

	@Override
	public Optional<? extends EClassifier> getSequence(EClass clazz, String sequenceName) {
		// TODO
		return Optional.empty();
	}

	@Override
	public boolean isSequence(EClassifier namespaceElement) {
		// TODO
		return false;
	}

	@Override
	public boolean isDerivedAttribute(EAttribute attribute) {
		return attribute.isDerived();
	}

	@Override
	public boolean isDerivedTransferAttribute(EAttribute attribute) {
		return isDerivedAttribute(attribute);
	}

	@Override
	public Optional<String> getAttributeGetter(EAttribute attribute) {
		return AsmUtils.getExtensionAnnotationCustomValue(attribute, "expression", "getter", false);
	}

	@Override
	public Optional<String> getTransferAttributeGetter(EAttribute attribute) {
		return getAttributeGetter(attribute);
	}

	@Override
	public Optional<String> getAttributeSetter(EAttribute attribute) {
		return AsmUtils.getExtensionAnnotationCustomValue(attribute, "expression", "setter", false);
	}

	@Override
	public Optional<String> getAttributeDefault(EAttribute attribute) {
		return AsmUtils.getExtensionAnnotationCustomValue(attribute, "expression", "default", false);
	}

	@Override
	public boolean isDerivedReference(EReference reference) {
		return reference.isDerived();
	}

	@Override
	public boolean isDerivedTransferRelation(EReference relation) {
		return relation.isDerived();
	}

	@Override
	public Optional<String> getReferenceGetter(EReference reference) {
		return AsmUtils.getExtensionAnnotationCustomValue(reference, "expression", "getter", false);
	}

	@Override
	public Optional<String> getTransferRelationGetter(EReference relation) {
		return getReferenceGetter(relation);
	}

	@Override
	public Optional<String> getReferenceDefault(EReference reference) {
		return AsmUtils.getExtensionAnnotationCustomValue(reference, "expression", "default", false);
	}

	@Override
	public Optional<String> getReferenceRange(EReference reference) {
		return AsmUtils.getExtensionAnnotationCustomValue(reference, "expression", "range", false);
	}

	@Override
	public Optional<String> getFilter(EClass clazz) {
		return AsmUtils.getExtensionAnnotationCustomValue(clazz, "mappedEntityType", "filter", false);
	}

	@Override
	public Optional<String> getReferenceSetter(EReference reference) {
		return AsmUtils.getExtensionAnnotationCustomValue(reference, "expression", "setter", false);
	}

	@Override
	public Optional<String> getTransferRelationSetter(EReference relation) {
		return getReferenceSetter(relation);
	}

	@Override
	public EList<Measure> getAllMeasures() {
		return ECollections.asEList(measureProvider.getMeasures().collect(toList()));
	}

	protected <T> Stream<T> getAsmElement(final Class<T> clazz) {
		return asmUtils.all(clazz);
	}

	Optional<Unit> getUnit(final EClass objectType, final String attributeName) {
		final Optional<Optional<Unit>> unit = objectType.getEAllAttributes().stream()
				.filter(a -> Objects.equals(a.getName(), attributeName)).map(a -> getUnit(a)).findAny();
		if (unit.isPresent()) {
			return unit.get();
		} else {
			log.error("Attribute not found: {}", attributeName);
			return Optional.empty();
		}
	}

	public Optional<Unit> getUnit(final EAttribute attribute) {
		if (AsmUtils.isNumeric(attribute.getEAttributeType())) {
			final Optional<String> unitNameOrSymbol = AsmUtils.getExtensionAnnotationCustomValue(attribute,
					"constraints", "unit", false);
			final Optional<String> measureFqName = AsmUtils.getExtensionAnnotationCustomValue(attribute, "constraints",
					"measure", false);
			if (unitNameOrSymbol.isPresent()) {
				if (measureFqName.isPresent()) {
					final Optional<MeasureName> measureName = measureFqName.isPresent()
							? parseMeasureName(measureFqName.get())
							: Optional.empty();
					if (!measureName.isPresent()) {
						log.error("Failed to parse measure name: {}", measureFqName.get());
						return Optional.empty();
					}
					final String replacedMeasureName = measureName.get().getNamespace().replace(".",
							NAMESPACE_SEPARATOR);
					final Optional<Measure> measure = measureName.isPresent()
							? measureProvider.getMeasure(replacedMeasureName, measureName.get().getName())
							: Optional.empty();
					if (!measure.isPresent() && measureName.isPresent()) {
						log.error("Measure is defined but not resolved: namespace={}, name={}", replacedMeasureName,
								measureName.get().getName());
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

	@Override
	public EList<EClass> getContainerTypesOf(final EClass clazz) {
		return ECollections.asEList(asmUtils.all(EClass.class)
				.filter(container -> container.getEAllContainments().stream()
						.anyMatch(c -> EcoreUtil.equals(c.getEReferenceType(), clazz)))
				.flatMap(container -> Stream.concat(container.getEAllSuperTypes().stream(),
						Collections.singleton(container).stream()))
				.collect(toList()));
	}

	@Override
	public EList<EClass> getAllTransferObjectTypes() {
		return ECollections.asEList(getAsmElement(EClass.class)
				.filter(c -> !AsmUtils.isEntityType(c) || asmUtils.isMappedTransferObjectType(c)).collect(toList()));
	}

	@Override
	public EList<EClass> getAllMappedTransferObjectTypes() {
		return ECollections
				.asEList(getAsmElement(EClass.class).filter(asmUtils::isMappedTransferObjectType).collect(toList()));
	}

	@Override
	public EList<EClass> getAllUnmappedTransferObjectTypes() {
		return ECollections.asEList(getAsmElement(EClass.class)
				.filter(c -> !AsmUtils.isEntityType(c) && !asmUtils.isMappedTransferObjectType(c)).collect(toList()));
	}

	public AsmUtils getAsmUtils() {
		return asmUtils;
	}

	@Override
	public Optional<EClass> getEntityTypeOfTransferObjectRelationTarget(TypeName transferObjectTypeName,
			String transferObjectRelationName) {

		Optional<? extends EClassifier> transferObjectType = this.get(transferObjectTypeName);

		if (!transferObjectType.isPresent()) {

			return Optional.empty();

		} else if (transferObjectType.get() instanceof EClass) {

			Optional<? extends EReference> eReference = this.getReference((EClass) transferObjectType.get(),
					transferObjectRelationName);

			if (!eReference.isPresent()) {
				return Optional.empty();
			} else {
				EClass referenceTarget = getTarget(eReference.get());

				if (AsmUtils.isEntityType(referenceTarget)) {

					return Optional.of(referenceTarget);

				} else if (asmUtils.isMappedTransferObjectType(referenceTarget)) {

					return asmUtils.getMappedEntityType(referenceTarget);

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
		return this.get(elementName).filter(c -> c instanceof EClass)
				.flatMap(c -> getReference((EClass) c, referenceName)).map(this::isCollectionReference)
				.filter(Boolean::booleanValue).isPresent();
	}

	@Override
	public Optional<EClass> getMappedEntityType(EClass mappedTransferObjectType) {
		return asmUtils.getMappedEntityType(mappedTransferObjectType);
	}

	@Override
	public String getFqName(Object object) {
		if (object instanceof EClassifier) {
			return AsmUtils.getClassifierFQName((EClassifier) object);
		} else if (object instanceof EReference) {
			return AsmUtils.getReferenceFQName((EReference) object);
		} else if (object instanceof EAttribute) {
			return AsmUtils.getAttributeFQName((EAttribute) object);
		} else
			return null;
	}

	@Override
	public Optional<String> getName(Object object) {
		if (object instanceof ENamedElement) {
			return Optional.of(((ENamedElement) object).getName());
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Collection<EAttribute> getAttributes(EClass clazz) {
		return clazz.getEAttributes();
	}

	@Override
	public Collection<EReference> getReferences(EClass clazz) {
		return clazz.getEReferences();
	}

	@Override
	public Collection<EAttribute> getTransferAttributes(EClass transferObjectType) {
		return transferObjectType.getEAttributes();
	}

	@Override
	public Collection<EReference> getTransferRelations(EClass transferObjectType) {
		return transferObjectType.getEReferences();
	}

	@Override
	public EDataType getTransferAttributeType(EAttribute transferAttribute) {
		return transferAttribute.getEAttributeType();
	}

	@Override
	public List<EClassifier> getAllActorTypes() {
		return new ArrayList<>(asmUtils.getAllActorTypes());
	}

	@Override
	public EClass getPrincipal(EClassifier actorType) {
		EClass principal = null;
		if (actorType instanceof EClass) {
			EList<EOperation> eOperations = ((EClass) actorType).getEOperations();
			for (EOperation operation : eOperations) {
				Optional<OperationBehaviour> behaviour = AsmUtils.getBehaviour(operation)
						.filter(b -> b.equals(OperationBehaviour.GET_PRINCIPAL));
				if (behaviour.isPresent()) {
					EClassifier eType = operation.getEType();
					if (eType instanceof EClass) {
						principal = (EClass) eType;
					}
				}
			}
		}
		if (principal != null) {
			return principal;
		} else {
			throw new IllegalArgumentException(String.format("%s principal not found", actorType));
		}
	}

}
