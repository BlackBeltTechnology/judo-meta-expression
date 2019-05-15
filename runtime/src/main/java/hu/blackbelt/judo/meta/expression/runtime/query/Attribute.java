package hu.blackbelt.judo.meta.expression.runtime.query;

import lombok.NonNull;
import org.eclipse.emf.ecore.EAttribute;

@lombok.Getter
@lombok.Builder
public class Attribute implements Feature, TargetFeature {

    /**
     * ASM entity type attribute.
     */
    @NonNull
    private EAttribute sourceAttribute;

    /**
     * ASM transfer object type attribute.
     */
    @NonNull
    private EAttribute targetAttribute;

    private Source source;

    private Target target;

    @Override
    public String toString() {
        return source.getSourceAlias() + "." + sourceAttribute.getName() + " AS " + targetAttribute.getName() + "@" + target.hashCode();
    }
}
