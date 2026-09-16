package hu.blackbelt.judo.meta.expression.validation.mock;

/**
 * Mock transfer attribute for testing purposes.
 * This is a stub class as transfer object validation is not the primary focus.
 */
public class MockTransferAttribute implements MockNamespaceElement {

    private final String name;
    private final String namespace;
    private final MockPrimitive type;
    private final MockAttribute binding;

    public MockTransferAttribute(String name, String namespace, MockPrimitive type, MockAttribute binding) {
        this.name = name;
        this.namespace = namespace;
        this.type = type;
        this.binding = binding;
    }

    public MockTransferAttribute(String name, MockPrimitive type) {
        this(name, null, type, null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public MockPrimitive getType() {
        return type;
    }

    public MockAttribute getBinding() {
        return binding;
    }

    public boolean hasBinding() {
        return binding != null;
    }
}
