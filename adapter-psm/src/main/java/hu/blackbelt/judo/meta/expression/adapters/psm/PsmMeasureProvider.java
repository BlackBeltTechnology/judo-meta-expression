package hu.blackbelt.judo.meta.expression.adapters.psm;

import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureChangedHandler;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.psm.measure.*;
import hu.blackbelt.judo.meta.psm.namespace.Namespace;
import hu.blackbelt.judo.meta.psm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EContentAdapter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Measure provider for measure metamodel that is used by PSM models.
 */
@Slf4j
public class PsmMeasureProvider implements MeasureProvider<Measure, Unit> {

    private static final List<DurationType> DURATION_UNITS_SUPPORTING_ADDITION = Arrays.asList(DurationType.MILLISECOND, DurationType.SECOND, DurationType.MINUTE, DurationType.HOUR, DurationType.DAY, DurationType.WEEK);

    private static final String NAMESPACE_SEPARATOR = "::";

    private final ResourceSet resourceSet;

    public PsmMeasureProvider(final ResourceSet resourceSet) {
        this.resourceSet = resourceSet;
    }

    @Override
    public String getMeasureNamespace(final Measure measure) {
        final Optional<Namespace> namespace = getPsmElement(Namespace.class)
                .filter(ns -> ns.getElements().contains(measure))
                .findAny();

        if (namespace.isPresent()) {
            return getNamespaceFQName(namespace.get());
        } else {
            return null;
        }
    }

    @Override
    public String getMeasureName(Measure measure) {
        return measure.getName();
    }

    @Override
    public Optional<Measure> getMeasure(String namespace, String name) {
        return getPsmElement(Measure.class)
                .filter(m -> Objects.equals(getMeasureNamespace(m), namespace) && Objects.equals(m.getName(), name))
                .findAny();
    }

    //TODO: check
    @Override
    public Map<Measure, Integer> getBaseMeasures(final Measure measure) {
        if (measure instanceof DerivedMeasure) {
            final DerivedMeasure derivedMeasure = (DerivedMeasure) measure;
            final Map<Measure, Integer> base = new ConcurrentHashMap<>();
            derivedMeasure.getTerms()
                    .forEach(t -> getBaseMeasures(getMeasure(t.getUnit())).forEach((m, exponent) -> {
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
            return Collections.singletonMap(measure, 1);
        }
    }

    //TODO: check
    private Measure getMeasure(final Unit unit) {
        return getMeasures()
                .filter(m -> getUnits(m).contains(unit))
                .findAny().get();
    }

    @Override
    public Collection<Unit> getUnits(Measure measure) {
        return measure.getUnits();
    }

    @Override
    public boolean isDurationSupportingAddition(Unit unit) {
        if (unit instanceof DurationUnit) {
            return DURATION_UNITS_SUPPORTING_ADDITION.contains(((DurationUnit) unit).getUnitType());
        } else {
            return false;
        }
    }

    @Override
    public Optional<Unit> getUnitByNameOrSymbol(Optional<Measure> measure, String nameOrSymbol) {
        if (measure.isPresent()) {
            return measure.map(m -> m.getUnits().stream()
                    .filter(u -> Objects.equals(u.getName(), nameOrSymbol) || u.getSymbol() != null && Objects.equals(u.getSymbol(), nameOrSymbol))
                    .findAny().orElse(null));
        } else {
            // NOTE - non-deterministic if multiple units exist
            return getPsmElement(Unit.class)
                    .filter(u -> Objects.equals(u.getName(), nameOrSymbol) || u.getSymbol() != null && Objects.equals(u.getSymbol(), nameOrSymbol))
                    .findAny();
        }
    }

    @Override
    public Stream<Measure> getMeasures() {
        return getPsmElement(Measure.class);
    }

    @Override
    public boolean isBaseMeasure(Measure measure) {
        return !(measure instanceof DerivedMeasure); //TODO: check if being dummy
    }

    //TODO: check
    @Override
    public void setMeasureChangeHandler(MeasureChangedHandler measureChangeHandler) {
        resourceSet.eAdapters().add(new EContentAdapter() {
            @Override
            public void notifyChanged(final Notification notification) {
                super.notifyChanged(notification);

                if (measureChangeHandler == null) {
                    return;
                }

                switch (notification.getEventType()) {
                    case Notification.ADD:
                    case Notification.ADD_MANY:
                        if (notification.getNewValue() instanceof Measure && !(notification.getNewValue() instanceof DerivedMeasure)) {
                            measureChangeHandler.measureAdded(notification.getNewValue());
                        } else if (notification.getNewValue() instanceof DerivedMeasure) {
                            measureChangeHandler.measureAdded(notification.getNewValue());
                        } else if (notification.getNewValue() instanceof Collection) {
                            ((Collection) notification.getNewValue()).forEach(newValue -> {
                                if (newValue instanceof Measure && !(newValue instanceof DerivedMeasure)) {
                                    measureChangeHandler.measureAdded(newValue);
                                } else if (newValue instanceof DerivedMeasure) {
                                    measureChangeHandler.measureAdded(newValue);
                                }
                            });
                        } else if (notification.getFeatureID(DerivedMeasure.class) == MeasurePackage.DERIVED_MEASURE__TERMS) {
                            measureChangeHandler.measureChanged(notification.getNotifier());
                        }
                        break;
                    case Notification.REMOVE:
                    case Notification.REMOVE_MANY:
                        if (notification.getOldValue() instanceof Measure && !(notification.getOldValue() instanceof DerivedMeasure)) {
                            measureChangeHandler.measureRemoved(notification.getOldValue());
                        } else if (notification.getOldValue() instanceof DerivedMeasure) {
                            measureChangeHandler.measureRemoved(notification.getOldValue());
                        } else if (notification.getOldValue() instanceof Collection) {
                            ((Collection) notification.getOldValue()).forEach(oldValue -> {
                                if (oldValue instanceof Measure && !(oldValue instanceof DerivedMeasure)) {
                                    measureChangeHandler.measureRemoved(oldValue);
                                } else if (oldValue instanceof DerivedMeasure) {
                                    measureChangeHandler.measureRemoved(oldValue);
                                }
                            });
                        } else if (notification.getFeatureID(DerivedMeasure.class) == MeasurePackage.DERIVED_MEASURE__TERMS) {
                            measureChangeHandler.measureChanged(notification.getNotifier());
                        }
                        break;
                }
            }
        });
    }

    <T> Stream<T> getPsmElement(final Class<T> clazz) {
        final Iterable<Notifier> psmContents = resourceSet::getAllContents;
        return StreamSupport.stream(psmContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }

    /**
     * Get namespace of a given namespace element.
     *
     * @param namespaceElement namespace element
     * @return namespace of namespace element
     */
    private Optional<Namespace> getNamespaceOfNamespaceElement(final NamespaceElement namespaceElement) {
        return getPsmElement(Namespace.class)
                .filter(ns -> ns.getElements().contains(namespaceElement))
                .findAny();

    }

    /**
     * Get namespace of a given package.
     *
     * @param pkg package
     * @return namespace of the package
     */
    private Optional<Namespace> getNamespaceOfPackage(final Package pkg) {
        return getPsmElement(Namespace.class)
                .filter(ns -> ns.getPackages().contains(pkg))
                .findAny();
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
            containerNamespace = getNamespaceOfPackage((Package) namespace);
        } else {
            containerNamespace = Optional.empty();
        }

        return (containerNamespace.isPresent() ? getNamespaceFQName(containerNamespace.get()) + NAMESPACE_SEPARATOR : "") + namespace.getName();
    }

    private String getNamespaceElementFQName(final NamespaceElement namespaceElement) {
        final Optional<Namespace> namespace = getNamespaceOfNamespaceElement(namespaceElement);
        return (namespace.isPresent() ? getNamespaceFQName(namespace.get()) + "." : "") + namespaceElement.getName();
    }
}
