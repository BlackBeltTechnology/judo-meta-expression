package hu.blackbelt.judo.meta.expression.runtime.query;

import org.eclipse.emf.ecore.EClass;

@lombok.Setter
@lombok.Getter
public abstract class Source {

    private String sourceAlias;

    public abstract EClass getType();
}
