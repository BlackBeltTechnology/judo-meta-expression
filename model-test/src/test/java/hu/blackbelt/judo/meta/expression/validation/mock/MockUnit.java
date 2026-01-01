package hu.blackbelt.judo.meta.expression.validation.mock;

/**
 * Mock unit for testing measure-related validation constraints.
 */
public class MockUnit implements MockNamespaceElement {

    private final String name;
    private final String namespace;
    private final String symbol;
    private final boolean durationSupportingAddition;
    private MockMeasure measure;

    public MockUnit(String name, String namespace, String symbol, boolean durationSupportingAddition) {
        this.name = name;
        this.namespace = namespace;
        this.symbol = symbol;
        this.durationSupportingAddition = durationSupportingAddition;
    }

    public MockUnit(String name, String symbol) {
        this(name, null, symbol, false);
    }

    public MockUnit(String name) {
        this(name, null, name, false);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isDurationSupportingAddition() {
        return durationSupportingAddition;
    }

    public MockMeasure getMeasure() {
        return measure;
    }

    void setMeasure(MockMeasure measure) {
        this.measure = measure;
    }

    /**
     * Creates a duration unit that supports addition to timestamps.
     */
    public static MockUnit durationUnit(String name, String symbol) {
        return new MockUnit(name, null, symbol, true);
    }

    @Override
    public String toString() {
        return "MockUnit{" +
                "name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}
