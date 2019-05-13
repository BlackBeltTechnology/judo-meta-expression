package hu.blackbelt.judo.meta.expression.runtime.query;

import org.eclipse.emf.ecore.EClass;

public interface Identifiable {

    EClass getObjectType();

    String getAlias();
    void setAlias(String alias);
}
