package hu.blackbelt.judo.meta.expression.adapters.asm;

import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
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
import hu.blackbelt.judo.meta.measure.Measure;
import hu.blackbelt.judo.meta.measure.Unit;
import org.eclipse.emf.common.notify.Notifier;
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
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newMeasureNameBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;
import static java.util.stream.Collectors.toList;

/**
 * Model adapter for ASM models.
 */
public class AsmModelAdapter implements ModelAdapter<EClassifier, EDataType, EAttribute, EEnum, EClass, EClass, EReference, EClassifier, Measure, Unit> {

    protected static final String NAMESPACE_SEPARATOR = "::";
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AsmModelAdapter.class);

    private static Pattern MEASURE_NAME_PATTERN = Pattern.compile("^(.*)\\.([^\\.]+)$");

    private final ResourceSet asmResourceSet;
    private final MeasureProvider<Measure, Unit> measureProvider;
    private final MeasureAdapter<Measure, Unit> measureAdapter;

    private final AsmUtils asmUtils;

    public AsmModelAdapter(final ResourceSet asmResourceSet, final ResourceSet measureResourceSet) {
        this.asmResourceSet = asmResourceSet;

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
    public Optional<TypeName> getTypeName(final EClassifier namespaceElement) {
        return getAsmElement(EPackage.class)
                .filter(ns -> ns.getEClassifiers().contains(namespaceElement))
                .map(ns -> newTypeNameBuilder().withNamespace(AsmUtils.getPackageFQName(ns).replace(".", NAMESPACE_SEPARATOR)).withName(namespaceElement.getName()).build())
                .findAny();
    }

    @Override
    public Optional<TypeName> getEnumerationTypeName(EEnum enumeration) {
        return getTypeName(enumeration);
    }

    @Override
    public Optional<MeasureName> getMeasureName(Measure measure) {
        return measureProvider.getMeasures()
                .filter(mn -> Objects.equals(mn.getNamespace(), measure.getNamespace()) && Objects.equals(mn.getName(), measure.getName()))
                .findAny().map(m -> newMeasureNameBuilder().withName(m.getName()).withNamespace(m.getNamespace()).build());
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
        return enumeration.getELiterals().stream().filter(l -> Objects.equals(l.getLiteral(), memberName)).findAny().isPresent();
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
                }
        );
    }

    @Override
    public EList<EClass> getAllClasses() {
        return ECollections.asEList(getAsmElement(EClass.class)
                .filter(c -> AsmUtils.isEntityType(c))
                .collect(toList()));
    }

    @Override
    public EList<EEnum> getAllEnums() {
        return ECollections.asEList(getAsmElement(EEnum.class)
                .filter(e -> AsmUtils.isEnumeration(e))
                .collect(toList()));
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
    public Optional<String> getAttributeGetter(EAttribute attribute) {
        return AsmUtils.getExtensionAnnotationCustomValue(attribute, "expression", "getter", false);
    }

    @Override
    public boolean isDerivedReference(EReference reference) {
        return reference.isDerived();
    }

    @Override
    public Optional<String> getReferenceGetter(EReference reference) {
        return AsmUtils.getExtensionAnnotationCustomValue(reference, "expression", "getter", false);
    }

    @Override
    public EList<Measure> getAllMeasures() {
        return ECollections.asEList(measureProvider.getMeasures().collect(toList()));
    }

    protected <T> Stream<T> getAsmElement(final Class<T> clazz) {
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

    public Optional<Unit> getUnit(final EAttribute attribute) {
        if (AsmUtils.isNumeric(attribute.getEAttributeType())) {
            final Optional<String> unitNameOrSymbol = AsmUtils.getExtensionAnnotationCustomValue(attribute, "constraints", "unit", false);
            final Optional<String> measureFqName = AsmUtils.getExtensionAnnotationCustomValue(attribute, "constraints", "measure", false);
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

    @Override
    public EList<EClass> getContainerTypesOf(final EClass clazz) {
        return ECollections.asEList(asmUtils.all(EClass.class)
                .filter(container -> container.getEAllContainments().stream().anyMatch(c -> EcoreUtil.equals(c.getEReferenceType(), clazz)))
                .flatMap(container -> Stream.concat(container.getEAllSuperTypes().stream(), Collections.singleton(container).stream()))
                .collect(toList()));
    }

    @Override
    public EList<EClass> getAllAccessPoints() {
        return ECollections.asEList(getAsmElement(EClass.class)
                .filter(c -> AsmUtils.isAccessPoint(c))
                .collect(toList()));
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

				if (asmUtils.isEntityType(referenceTarget)) {
					
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
		
		Optional<? extends EClassifier> element = this.get(elementName);

		if (element.isPresent() && element.get() instanceof EClass) {
			
			Optional<? extends EReference> reference = getReference((EClass) element.get(), referenceName);
			
			if (reference.isPresent()) {
				return isCollectionReference(reference.get());
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
