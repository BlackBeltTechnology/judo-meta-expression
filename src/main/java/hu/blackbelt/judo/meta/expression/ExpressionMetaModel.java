package hu.blackbelt.judo.meta.expression;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

public interface ExpressionMetaModel {

    Resource.Factory getFactory();

    void registerExpressionMetamodel(ResourceSet resourceSet);
}
