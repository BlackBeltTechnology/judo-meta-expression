package hu.blackbelt.judo.meta.expression.runtime.query;

import lombok.NonNull;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

@lombok.Getter
@lombok.Builder
public class Join extends Source {

    /**
     * ASM reference of {@link Select#getFrom()}.
     */
    @NonNull
    private EReference reference;

    @NonNull
    private Source partner;

    @Override
    public EClass getType() {
        return reference.getEReferenceType();
    }

    // TODO - add filtering

    @Override
    public String toString() {
        return partner.getSourceAlias() + "." + reference.getName() + "<" + reference.getEReferenceType().getName() + ">" + " AS " + getSourceAlias();
    }
}
