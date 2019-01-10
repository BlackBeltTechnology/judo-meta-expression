package hu.blackbelt.judo.meta.expression;

import hu.blackbelt.judo.meta.expression.collection.CollectionPackage;
import hu.blackbelt.judo.meta.expression.constant.ConstantPackage;
import hu.blackbelt.judo.meta.expression.enumeration.EnumerationPackage;
import hu.blackbelt.judo.meta.expression.logical.LogicalPackage;
import hu.blackbelt.judo.meta.expression.numeric.NumericPackage;
import hu.blackbelt.judo.meta.expression.object.ObjectPackage;
import hu.blackbelt.judo.meta.expression.operator.OperatorPackage;
import hu.blackbelt.judo.meta.expression.string.StringPackage;
import hu.blackbelt.judo.meta.expression.variable.VariablePackage;
import hu.blackbelt.judo.meta.expression.util.ExpressionResourceFactoryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.Dictionary;
import java.util.Hashtable;

@Component(immediate = true, service = ExpressionMetaModel.class)
public class ExpressionMetaModelRegistration implements ExpressionMetaModel {

    ServiceRegistration<Resource.Factory> expressionFactoryRegistration;
    Resource.Factory factory;

    @Activate
    public void activate(ComponentContext componentContext) {
        Dictionary<String, Object> params = new Hashtable<>();
        params.put("meta", "psm");
        params.put("version", componentContext.getBundleContext().getBundle().getVersion());
        params.put("bundle", componentContext.getBundleContext().getBundle());

        factory = new ExpressionResourceFactoryImpl();
        expressionFactoryRegistration = componentContext.getBundleContext()
                .registerService(Resource.Factory.class, factory, params);
    }

    @Deactivate
    public void deactivate() {
        expressionFactoryRegistration.unregister();
    }

    @Override
    public Resource.Factory getFactory() {
        return factory;
    }

    @Override
    public void registerExpressionMetamodel(ResourceSet resourceSet) {
        resourceSet.getPackageRegistry().put(ExpressionPackage.eINSTANCE.getNsURI(), ExpressionPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(CollectionPackage.eINSTANCE.getNsURI(), CollectionPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(ConstantPackage.eINSTANCE.getNsURI(), ConstantPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(EnumerationPackage.eINSTANCE.getNsURI(), EnumerationPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(LogicalPackage.eINSTANCE.getNsURI(), LogicalPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(NumericPackage.eINSTANCE.getNsURI(), NumericPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(ObjectPackage.eINSTANCE.getNsURI(), ObjectPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(OperatorPackage.eINSTANCE.getNsURI(), OperatorPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(StringPackage.eINSTANCE.getNsURI(), StringPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(VariablePackage.eINSTANCE.getNsURI(), VariablePackage.eINSTANCE);
    }
}
