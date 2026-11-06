package hu.blackbelt.judo.meta.expression.validation.mock;

/**
 * Mock sequence for testing purposes.
 * Sequences are used for generating unique identifiers.
 */
public class MockSequence implements MockNamespaceElement {

    private final String name;
    private final String namespace;

    public MockSequence(String name, String namespace) {
        this.name = name;
        this.namespace = namespace;
    }

    public MockSequence(String name) {
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
}
