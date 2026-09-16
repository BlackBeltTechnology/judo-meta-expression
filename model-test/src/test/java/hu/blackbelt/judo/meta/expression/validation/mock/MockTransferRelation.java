package hu.blackbelt.judo.meta.expression.validation.mock;

/**
 * Mock transfer relation for testing purposes.
 * This is a stub class as transfer object validation is not the primary focus.
 */
public class MockTransferRelation implements MockNamespaceElement {

    private final String name;
    private final String namespace;
    private final MockTransferObject target;
    private final MockReference binding;
    private final boolean collection;

    public MockTransferRelation(String name, String namespace, MockTransferObject target, 
                                 MockReference binding, boolean collection) {
        this.name = name;
        this.namespace = namespace;
        this.target = target;
        this.binding = binding;
        this.collection = collection;
    }

    public MockTransferRelation(String name, MockTransferObject target, boolean collection) {
        this(name, null, target, null, collection);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public MockTransferObject getTarget() {
        return target;
    }

    public MockReference getBinding() {
        return binding;
    }

    public boolean hasBinding() {
        return binding != null;
    }

    public boolean isCollection() {
        return collection;
    }
}
