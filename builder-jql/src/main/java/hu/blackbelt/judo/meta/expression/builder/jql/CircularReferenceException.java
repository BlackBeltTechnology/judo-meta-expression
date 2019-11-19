package hu.blackbelt.judo.meta.expression.builder.jql;

public class CircularReferenceException extends RuntimeException {

    private final String accessorName;

    public CircularReferenceException(String accessorName) {
        super("Already resolved accessor: " + accessorName);
        this.accessorName = accessorName;
    }

    public String getAccessorName() {
        return accessorName;
    }
}
