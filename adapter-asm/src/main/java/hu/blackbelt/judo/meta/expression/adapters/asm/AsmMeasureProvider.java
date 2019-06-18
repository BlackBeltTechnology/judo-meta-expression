package hu.blackbelt.judo.meta.expression.adapters.asm;

import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.measure.*;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AsmMeasureProvider implements MeasureProvider<Measure, Unit> {

    private static final List<DurationType> DURATION_UNITS_SUPPORTING_ADDITION = Arrays.asList(DurationType.MILLISECOND, DurationType.SECOND, DurationType.MINUTE, DurationType.HOUR, DurationType.DAY, DurationType.WEEK);

    private final ResourceSet resourceSet;

    public AsmMeasureProvider(final ResourceSet resourceSet) {
        this.resourceSet = resourceSet;
    }

    @Override
    public String getMeasureNamespace(final Measure measure) {
        return measure.getNamespace();
    }

    @Override
    public String getMeasureName(final Measure measure) {
        return measure.getName();
    }

    @Override
    public Optional<Measure> getMeasure(final String namespace, final String name) {
        return getMeasureElement(Measure.class)
                .filter(m -> Objects.equals(m.getNamespace(), namespace) && Objects.equals(m.getName(), name))
                .findAny();
    }

    @Override
    public Map<Measure, Integer> getBaseMeasures(final Measure measure) {
        if (measure instanceof DerivedMeasure) {
            final DerivedMeasure derivedMeasure = (DerivedMeasure) measure;
            return derivedMeasure.getTerms().stream().collect(Collectors.toMap(t -> t.getBaseMeasure(), t -> t.getExponent()));
        } else {
            return Collections.singletonMap(measure, 1);
        }
    }

    @Override
    public Collection<Unit> getUnits(final Measure measure) {
        return measure.getUnits();
    }

    @Override
    public boolean isDurationSupportingAddition(final Unit unit) {
        if (unit instanceof DurationUnit) {
            return DURATION_UNITS_SUPPORTING_ADDITION.contains(((DurationUnit) unit).getType());
        } else {
            return false;
        }
    }

    @Override
    public Optional<Unit> getUnitByNameOrSymbol(final Optional<Measure> measure, final String nameOrSymbol) {
        if (measure.isPresent()) {
            return measure.map(m -> m.getUnits().stream()
                    .filter(u -> Objects.equals(u.getName(), nameOrSymbol) || Objects.equals(u.getSymbol(), nameOrSymbol))
                    .findAny().orElse(null));
        } else {
            // NOTE - non-deterministic if multiple units exist
            return getMeasureElement(Unit.class)
                    .filter(u -> Objects.equals(u.getName(), nameOrSymbol) || Objects.equals(u.getSymbol(), nameOrSymbol))
                    .findAny();
        }
    }

    @Override
    public Stream<Measure> getMeasures() {
        return getMeasureElement(Measure.class);
    }

    <T> Stream<T> getMeasureElement(final Class<T> clazz) {
        final Iterable<Notifier> asmContents = resourceSet::getAllContents;
        return StreamSupport.stream(asmContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }
}
