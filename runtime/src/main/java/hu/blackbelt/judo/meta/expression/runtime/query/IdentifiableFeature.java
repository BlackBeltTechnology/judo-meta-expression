package hu.blackbelt.judo.meta.expression.runtime.query;

public interface IdentifiableFeature extends Feature {

    Identifiable getIdentifiable();

    void setIdentifiable(Identifiable identifiable);
}
