package hu.blackbelt.judo.meta.expression.builder.jql;

public class IncompatibleInputParameterException extends RuntimeException {

    private final String accessorName;

    public IncompatibleInputParameterException(String accessorName) {
        super("Incompatible input parameter: " + accessorName);
        this.accessorName = accessorName;
    }

    public String getAccessorName() {
        return accessorName;
    }
}
