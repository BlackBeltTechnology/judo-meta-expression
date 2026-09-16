package hu.blackbelt.judo.meta.expression.validation.mock;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Mock measure for testing measure-related validation constraints.
 */
public class MockMeasure implements MockNamespaceElement {

    private final String name;
    private final String namespace;
    private final List<MockUnit> units = new ArrayList<>();
    private final Map<MockMeasure, Integer> baseMeasures = new LinkedHashMap<>();

    public MockMeasure(String name, String namespace) {
        this.name = name;
        this.namespace = namespace;
    }

    public MockMeasure(String name) {
        this(name, null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public MockMeasure addUnit(MockUnit unit) {
        units.add(unit);
        unit.setMeasure(this);
        return this;
    }

    public MockMeasure addBaseMeasure(MockMeasure baseMeasure, int exponent) {
        baseMeasures.put(baseMeasure, exponent);
        return this;
    }

    public List<MockUnit> getUnits() {
        return new ArrayList<>(units);
    }

    public Map<MockMeasure, Integer> getBaseMeasures() {
        return new LinkedHashMap<>(baseMeasures);
    }

    /**
     * Check if this measure is a base measure (has no base measures defined).
     */
    public boolean isBaseMeasure() {
        return baseMeasures.isEmpty();
    }

    /**
     * Find a unit by name or symbol.
     */
    public Optional<MockUnit> getUnitByNameOrSymbol(String nameOrSymbol) {
        return units.stream()
                .filter(u -> u.getName().equals(nameOrSymbol) || u.getSymbol().equals(nameOrSymbol))
                .findFirst();
    }

    @Override
    public String toString() {
        return "MockMeasure{" +
                "name='" + name + '\'' +
                ", namespace='" + namespace + '\'' +
                ", units=" + units.size() +
                '}';
    }
}
