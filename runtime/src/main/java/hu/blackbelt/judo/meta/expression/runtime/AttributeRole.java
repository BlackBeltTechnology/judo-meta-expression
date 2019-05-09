package hu.blackbelt.judo.meta.expression.runtime;

@lombok.Getter
@lombok.Builder
public class AttributeRole {

    private String alias;

    private boolean projection;

    private boolean argument;

    @Override
    public String toString() {
        return "[" + alias + "|" + projection + "," + argument + "]";
    }
}
