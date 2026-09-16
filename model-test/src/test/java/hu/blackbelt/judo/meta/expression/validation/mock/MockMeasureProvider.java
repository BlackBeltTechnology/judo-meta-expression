package hu.blackbelt.judo.meta.expression.validation.mock;

import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureChangedHandler;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Mock implementation of MeasureProvider for testing measure-related validation constraints.
 */
public class MockMeasureProvider implements MeasureProvider<MockMeasure, MockUnit> {

    private final List<MockMeasure> measures = new ArrayList<>();
    private MeasureChangedHandler measureChangeHandler;

    public MockMeasureProvider() {
    }

    public MockMeasureProvider addMeasure(MockMeasure measure) {
        measures.add(measure);
        return this;
    }

    @Override
    public String getMeasureNamespace(MockMeasure measure) {
        return measure.getNamespace();
    }

    @Override
    public String getMeasureName(MockMeasure measure) {
        return measure.getName();
    }

    @Override
    public Optional<MockMeasure> getMeasure(String namespace, String name) {
        return measures.stream()
                .filter(m -> {
                    boolean nameMatches = m.getName().equals(name);
                    if (namespace == null || namespace.isEmpty()) {
                        return nameMatches && (m.getNamespace() == null || m.getNamespace().isEmpty());
                    }
                    return nameMatches && namespace.equals(m.getNamespace());
                })
                .findFirst();
    }

    @Override
    public EMap<MockMeasure, Integer> getBaseMeasures(MockMeasure measure) {
        EMap<MockMeasure, Integer> result = new BasicEMap<>();
        for (Map.Entry<MockMeasure, Integer> entry : measure.getBaseMeasures().entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public EList<MockUnit> getUnits(MockMeasure measure) {
        EList<MockUnit> result = new BasicEList<>();
        result.addAll(measure.getUnits());
        return result;
    }

    @Override
    public boolean isDurationSupportingAddition(MockUnit unit) {
        return unit.isDurationSupportingAddition();
    }

    @Override
    public Optional<MockUnit> getUnitByNameOrSymbol(Optional<MockMeasure> measure, String nameOrSymbol) {
        if (measure.isPresent()) {
            return measure.get().getUnitByNameOrSymbol(nameOrSymbol);
        }
        // Search across all measures
        return measures.stream()
                .flatMap(m -> m.getUnits().stream())
                .filter(u -> u.getName().equals(nameOrSymbol) || u.getSymbol().equals(nameOrSymbol))
                .findFirst();
    }

    @Override
    public Stream<MockMeasure> getMeasures() {
        return measures.stream();
    }

    @Override
    public Stream<MockUnit> getUnits() {
        return measures.stream()
                .flatMap(m -> m.getUnits().stream());
    }

    @Override
    public boolean isBaseMeasure(MockMeasure measure) {
        return measure.isBaseMeasure();
    }

    @Override
    public void setMeasureChangeHandler(MeasureChangedHandler measureChangeHandler) {
        this.measureChangeHandler = measureChangeHandler;
    }

    /**
     * Get all registered measures.
     */
    public List<MockMeasure> getAllMeasures() {
        return new ArrayList<>(measures);
    }

    /**
     * Clear all measures.
     */
    public void clear() {
        measures.clear();
    }
}
