package hu.blackbelt.judo.meta.expression.runtime.adapters;

import com.google.common.collect.Iterables;
import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.measure.Measure;
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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class AsmModelAdapter implements ModelAdapter<EClassifier, EDataType, EAttribute, EEnum, EClass, EReference, Measure> {

    private static final String NAMESPACE_SEPARATOR = ".";

    private final ResourceSet asmResourceSet;
    private final ResourceSet measureResourceSet;

    private final Map<String, EPackage> namespaceCache = new ConcurrentHashMap<>();

    public AsmModelAdapter(final ResourceSet asmResourceSet, final ResourceSet measureResourceSet) {
        this.asmResourceSet = asmResourceSet;
        this.measureResourceSet = measureResourceSet;

        final Iterable<Notifier> contents = asmResourceSet::getAllContents;
        StreamSupport.stream(contents.spliterator(), false)
                .filter(e -> e instanceof EPackage).map(e -> (EPackage) e)
                .filter(e -> e.getESuperPackage() == null)
                .forEach(m -> initNamespace(Collections.emptyList(), m));
    }

    @Override
    public Optional<EClassifier> get(final TypeName elementName) {
        final EPackage namespace = namespaceCache.get(elementName.getNamespace().replace(".", NAMESPACE_SEPARATOR));
        if (namespace != null) {
            return namespace.getEClassifiers().stream().filter(e -> Objects.equals(e.getName(), elementName.getName()))
                    .findFirst();
        } else {
            log.warn("Namespace not found: {}", elementName.getNamespace());
            return Optional.empty();
        }
    }

    @Override
    public Optional get(final MeasureName measureName) {
        final Iterable<Notifier> contents = measureResourceSet::getAllContents;
        return StreamSupport.stream(contents.spliterator(), false)
                .filter(e -> e instanceof Measure).map(e -> (Measure) e)
                .filter(m -> Objects.equals(m.getName(), measureName.getName()) && Objects.equals(m.getNamespace().replace(".", NAMESPACE_SEPARATOR), measureName.getNamespace()))
                .findFirst();
    }

    @Override
    public boolean isObjectType(final EClassifier namespaceElement) {
        return namespaceElement instanceof EClass;
    }

    @Override
    public Optional<? extends EReference> getReference(final EClass clazz, final String referenceName) {
        return clazz.getEAllReferences().stream().filter(r -> Objects.equals(r.getName(), referenceName)).findFirst();
    }

    @Override
    public boolean isCollection(final EReference reference) {
        return reference.isMany();
    }

    @Override
    public EClass getTarget(final EReference reference) {
        return reference.getEReferenceType();
    }

    @Override
    public Optional<? extends EAttribute> getAttribute(final EClass clazz, final String attributeName) {
        return clazz.getEAllAttributes().stream().filter(r -> Objects.equals(r.getName(), attributeName)).findFirst();
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
    public boolean isDate(final EDataType primitive) {
        return AsmUtils.isDate(primitive);
    }

    @Override
    public boolean isTimestamp(final EDataType primitive) {
        return AsmUtils.isTimestamp(primitive);
    }

    @Override
    public boolean isCustom(final EDataType primitive) {
        return !AsmUtils.isBoolean(primitive)
                && !AsmUtils.isNumeric(primitive)
                && !AsmUtils.isString(primitive)
                && !AsmUtils.isEnumeration(primitive)
                && !AsmUtils.isDate(primitive)
                && !AsmUtils.isTimestamp(primitive);
    }

    @Override
    public boolean isMeasured(final EAttribute attribute) {
        if (AsmUtils.isNumeric(attribute.getEAttributeType())) {

            final Optional<EAnnotation> annotation = AsmUtils.getExtensionAnnotation(attribute, false);
            if (annotation.isPresent()) {
                log.info("Found measured attribute: {}", attribute);
                // TODO - try to resolve measure, log if it is invalid
                return annotation.get().getDetails().containsKey("measure");
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean contains(EEnum enumeration, String memberName) {
        return enumeration.getELiterals().stream().filter(l -> Objects.equals(l.getLiteral(), memberName)).findAny().isPresent();
    }

    private void initNamespace(final Iterable<EPackage> path, final EPackage namespace) {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.join(NAMESPACE_SEPARATOR, StreamSupport.stream(path.spliterator(), false).map(p -> p.getName()).collect(Collectors.toList())));
        if (sb.length() > 0) {
            sb.append(NAMESPACE_SEPARATOR);
        }
        sb.append(namespace.getName());

        final String key = sb.toString();

        namespaceCache.put(key, namespace);
        log.debug("Cached namespace: {}", key);

        final Iterable<EPackage> newPath = Iterables.concat(path, Collections.singleton(namespace));
        namespace.getESubpackages().parallelStream().forEach(p -> initNamespace(newPath, p));
    }
}
