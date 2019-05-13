package hu.blackbelt.judo.meta.expression.runtime.query;

@lombok.Data
public class IdAttribute implements Feature {

    private Identifiable identifiable;

    @Override
    public String toString() {
        return "ID of " + identifiable.getAlias();
    }
}
