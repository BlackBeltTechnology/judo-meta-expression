package hu.blackbelt.judo.meta.expression.adapters.psm;

import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureChangedHandler;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.psm.measure.DurationType;
import hu.blackbelt.judo.meta.psm.measure.Measure;
import hu.blackbelt.judo.meta.psm.measure.Unit;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Measure provider for measure metamodel that is used by PSM models.
 */
@Slf4j
public class PsmMeasureProvider implements MeasureProvider<Measure, Unit> {

    private static final List<DurationType> DURATION_UNITS_SUPPORTING_ADDITION = Arrays.asList(DurationType.MILLISECOND, DurationType.SECOND, DurationType.MINUTE, DurationType.HOUR, DurationType.DAY, DurationType.WEEK);

    private final ResourceSet resourceSet;

    public PsmMeasureProvider(final ResourceSet resourceSet) {
        this.resourceSet = resourceSet;
    }

    @Override
    public String getMeasureNamespace(Measure measure) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String getMeasureName(Measure measure) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Measure> getMeasure(String namespace, String name) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Map<Measure, Integer> getBaseMeasures(Measure measure) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Collection<Unit> getUnits(Measure measure) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isDurationSupportingAddition(Unit unit) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Unit> getUnitByNameOrSymbol(Optional<Measure> measure, String nameOrSymbol) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Stream<Measure> getMeasures() {
        return getMeasureElement(Measure.class);
    }

    @Override
    public boolean isBaseMeasure(Measure measure) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void setMeasureChangeHandler(MeasureChangedHandler measureChangeHandler) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    <T> Stream<T> getMeasureElement(final Class<T> clazz) {
        final Iterable<Notifier> psmContents = resourceSet::getAllContents;
        return StreamSupport.stream(psmContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }
}
