package hu.blackbelt.judo.meta.expression.adapters.esm;

import hu.blackbelt.judo.meta.esm.measure.*;
import hu.blackbelt.judo.meta.esm.namespace.NamedElement;
import hu.blackbelt.judo.meta.esm.namespace.Namespace;
import hu.blackbelt.judo.meta.esm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.esm.namespace.Package;
import hu.blackbelt.judo.meta.esm.runtime.EsmUtils;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureChangedHandler;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * Measure provider for measure metamodel that is used runtime (with ESM models).
 */
public class EsmMeasureProvider implements MeasureProvider<Measure, Unit> {

    private static final List<DurationType> DURATION_TYPES_SUPPORTING_ADDITION = Arrays.asList(DurationType.MILLISECOND, DurationType.SECOND, DurationType.MINUTE, DurationType.HOUR, DurationType.DAY, DurationType.WEEK);
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(EsmMeasureProvider.class);
    private static final String NAMESPACE_SEPARATOR = "::";

    private final ResourceSet resourceSet;

    public EsmMeasureProvider(final ResourceSet resourceSet) {
        this.resourceSet = resourceSet;
    }

    public static <T> Stream<T> getAllContents(EObject eObject, Class<T> clazz) {
        if (eObject.eResource() == null) {
            throw new IllegalStateException("No object added to resource - " + eObject.eClass().toString());
        } else {
            ResourceSet resourceSet = eObject.eResource().getResourceSet();
            if (resourceSet == null) {
                throw new IllegalStateException("No resource in resourceset");
            } else {
                Iterable<Notifier> esmContents = resourceSet::getAllContents;
                return StreamSupport.stream(esmContents.spliterator(), true)
                        .filter((e) -> clazz.isAssignableFrom(e.getClass()))
                        .map((e) -> (T) e);
            }
        }
    }

    @Override
    public String getMeasureNamespace(final Measure measure) {
        Optional<Namespace> namespace = getEsmElement(Namespace.class)
                .filter(ns -> ns.getElements().contains(measure))
                .findAny();
        return namespace.map(this::getNamespaceFQName).orElse(null);
    }

    @Override
    public String getMeasureName(final Measure measure) {
        return measure.getName();
    }

    @Override
    public Optional<Measure> getMeasure(final String namespace, final String name) {
        return getEsmElement(Measure.class)
                .filter(m -> Objects.equals(getMeasureNamespace(m), namespace) && Objects.equals(m.getName(), name))
                .findAny();
    }

    @Override
    public EMap<Measure, Integer> getBaseMeasures(final Measure measure) {
        EList<MeasureDefinitionTerm> terms = measure.getTerms();
        Map<Measure, Integer> base = new ConcurrentHashMap<>();
        if (terms != null && !terms.isEmpty()) {
            terms.forEach(term -> getBaseMeasures(findMeasureOfUnit(term.getUnit())).forEach(e -> {
                final Measure m = e.getKey();
                final Integer exponent = e.getValue();
                final int currentExponent = base.getOrDefault(findMeasureOfUnit(term.getUnit()), 0);
                final int calculatedBaseExponent = exponent * term.getExponent();
                final int newExponent = currentExponent + calculatedBaseExponent;
                if (newExponent != 0) {
                    base.put(m, newExponent);
                } else {
                    base.remove(m);
                }
            }));
            return ECollections.asEMap(base);
        } else {
            return ECollections.singletonEMap(measure, 1);
        }
    }

    private Measure findMeasureOfUnit(Unit unit) {
        return getMeasures()
                .filter(m -> getUnits(m).contains(unit))
                .findAny().get();
    }

    @Override
    public EList<Unit> getUnits(final Measure measure) {
        return ECollections.asEList(measure.getUnits());
    }

    @Override
    public boolean isDurationSupportingAddition(final Unit unit) {
        if (unit instanceof DurationUnit) {
            return DURATION_TYPES_SUPPORTING_ADDITION.contains(((DurationUnit) unit).getUnitType());
        } else {
            return false;
        }
    }

    @Override
    public Optional<Unit> getUnitByNameOrSymbol(final Optional<Measure> measure, final String nameOrSymbol) {
        if (measure.isPresent()) {
            return measure.map(m -> m.getUnits().stream()
                    .filter(u -> Objects.equals(u.getName(), nameOrSymbol) || u.getSymbol() != null && Objects.equals(u.getSymbol(), nameOrSymbol))
                    .findAny().orElse(null));
        } else {
            // NOTE - non-deterministic if multiple units exist
            return getEsmElement(Unit.class)
                    .filter(u -> Objects.equals(u.getName(), nameOrSymbol) || u.getSymbol() != null && Objects.equals(u.getSymbol(), nameOrSymbol))
                    .findAny();
        }
    }

    @Override
    public Stream<Measure> getMeasures() {
        return getEsmElement(Measure.class);
    }

    @Override
    public Stream<Unit> getUnits() {
        return getEsmElement(Unit.class);
    }

    @Override
    public void setMeasureChangeHandler(final MeasureChangedHandler measureChangeHandler) {
        resourceSet.eAdapters().add(new EContentAdapter() {
            @Override
            public void notifyChanged(final Notification notification) {
                super.notifyChanged(notification);
                return;
            }
        });
    }

    @Override
    public boolean isBaseMeasure(Measure measure) {
        return measure.getTerms() == null || measure.getTerms().isEmpty();
    }

    <T> Stream<T> getEsmElement(final Class<T> clazz) {
        final Iterable<Notifier> allContents = resourceSet::getAllContents;
        return StreamSupport.stream(allContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }

    public String getNamespaceFQName(Namespace namespace) {
        if (namespace instanceof NamespaceElement) {
            return EsmUtils.getNamespaceElementFQName((NamespaceElement) namespace);
        } else {
            return String.join(NAMESPACE_SEPARATOR, namespace.getElements().stream().map(NamedElement::getName).collect(toList()));
        }
    }

    private Optional<Namespace> getNamespaceOfPackage(Package pkg) {
        return getAllContents(pkg, Namespace.class)
                .filter((ns) -> ns.getElements().contains(pkg))
                .findAny();
    }

}
