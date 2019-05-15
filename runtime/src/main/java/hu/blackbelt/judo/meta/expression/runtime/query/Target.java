package hu.blackbelt.judo.meta.expression.runtime.query;

import lombok.NonNull;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import java.util.ArrayList;
import java.util.Collection;

@lombok.Getter
@lombok.Builder
public class Target {

    /**
     * ASM transfer object type.
     */
    @NonNull
    private EClass type;

    private EReference role;

    private final Collection<IdAttribute> idAttributes = new ArrayList<>();

    /**
     * Attributes of {@link Select#getFrom()} and {@link Join#getReference()} (called recursively) or function calls.
     */
    private final Collection<Feature> features = new ArrayList<>();

    /**
     * ASM transfer object containments.
     */
    private final Collection<Target> containments = new ArrayList<>();

    @Override
    public String toString() {
        return type.getName() + "@" + hashCode();
    }
}
