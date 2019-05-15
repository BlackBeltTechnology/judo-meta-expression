package hu.blackbelt.judo.meta.expression.runtime.query;

@lombok.Getter
@lombok.Builder
public class IdAttribute implements Feature, TargetFeature {

    private Source source;

    private Target target;

    @Override
    public String toString() {
        return "ID of " + source.getSourceAlias() + " AS ID@" + target.hashCode();
    }
}
