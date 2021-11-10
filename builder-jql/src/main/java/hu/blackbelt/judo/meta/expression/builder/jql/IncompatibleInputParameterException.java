package hu.blackbelt.judo.meta.expression.builder.jql;

public class IncompatibleInputParameterException extends RuntimeException {

    private final String accessorName;

    public IncompatibleInputParameterException(String accessorName, String inputParameterName) {
        super("Parameter of derived feature " + accessorName + " is incompatible with input parameter " + inputParameterName);
        this.accessorName = accessorName;
    }

    public String getAccessorName() {
        return accessorName;
    }
}
