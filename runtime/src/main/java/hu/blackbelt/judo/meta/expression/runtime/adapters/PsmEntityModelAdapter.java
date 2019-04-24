package hu.blackbelt.judo.meta.expression.runtime.adapters;

import com.google.common.collect.Iterables;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.data.PrimitiveTypedElement;
import hu.blackbelt.judo.meta.psm.data.ReferenceTypedElement;
import hu.blackbelt.judo.meta.psm.measure.Measure;
import hu.blackbelt.judo.meta.psm.measure.MeasuredType;
import hu.blackbelt.judo.meta.psm.namespace.Model;
import hu.blackbelt.judo.meta.psm.namespace.Namespace;
import hu.blackbelt.judo.meta.psm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.psm.type.EnumerationType;
import hu.blackbelt.judo.meta.psm.type.Primitive;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.notify.Notifier;
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
public class PsmEntityModelAdapter implements ModelAdapter<NamespaceElement, Primitive, PrimitiveTypedElement, EnumerationType, EntityType, ReferenceTypedElement, Measure> {

    private static final String NAMESPACE_SEPARATOR = ".";

    private final ResourceSet resourceSet;

    private final Map<String, Namespace> namespaceCache = new ConcurrentHashMap<>();

    public PsmEntityModelAdapter(final ResourceSet resourceSet) {
        this.resourceSet = resourceSet;

        final Iterable<Notifier> contents = resourceSet::getAllContents;
        StreamSupport.stream(contents.spliterator(), false)
                .filter(e -> e instanceof Model).map(e -> (Model) e)
                .forEach(m -> initNamespace(Collections.emptyList(), m));
    }

    @Override
    public Optional<NamespaceElement> get(final TypeName elementName) {
        final Namespace namespace = namespaceCache.get(elementName.getNamespace().replace("::", NAMESPACE_SEPARATOR));
        if (namespace != null) {
            return namespace.getElements().stream().filter(e -> Objects.equals(e.getName(), elementName.getName()))
                    .findFirst();
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
                    .findFirst();
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
    public boolean isCollection(final ReferenceTypedElement reference) {
        return reference.isCollection();
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
    public boolean isMeasured(final PrimitiveTypedElement primitiveTypedElement) {
        return primitiveTypedElement.getDataType() instanceof MeasuredType;
    }

    @Override
    public boolean contains(EnumerationType enumeration, String memberName) {
        return enumeration.contains(memberName);
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
        namespace.getPackages().parallelStream().forEach(p -> initNamespace(newPath, p));
    }
}
