package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.ExpressionPackage;
import hu.blackbelt.judo.meta.expression.util.ExpressionResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryRegistryImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.URIHandlerImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExpressionModelLoader {

    public static void registerExpressionMetamodel(ResourceSet resourceSet) {
        resourceSet.getPackageRegistry().put(ExpressionPackage.eINSTANCE.getNsURI(), ExpressionPackage.eINSTANCE);
    }


    public static Resource.Factory getExpressionFactory() {
        return new ExpressionResourceFactoryImpl();
    }

    public static ResourceSet createExpressionResourceSet() {
        return createExpressionResourceSet(null);
    }

    public static ResourceSet createExpressionResourceSet(URIHandler uriHandler) {
        ResourceSet resourceSet = new ResourceSetImpl();
        registerExpressionMetamodel(resourceSet);
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(ResourceFactoryRegistryImpl.DEFAULT_EXTENSION, getExpressionFactory());
        if (uriHandler != null) {
            resourceSet.getURIConverter().getURIHandlers().add(0, uriHandler);
        }
        return resourceSet;
    }


    public static ExpressionModel loadExpressionModel(URI uri, String name, String version) throws IOException {
        return loadExpressionModel(createExpressionResourceSet(), uri, name, version, null, null);
    }

    public static ExpressionModel loadExpressionModel(ResourceSet resourceSet, URI uri, String name, String version) throws IOException {
        return loadExpressionModel(resourceSet, uri, name, version, null, null);
    }

    public static ExpressionModel loadExpressionModel(ResourceSet resourceSet, URI uri, String name, String version, String checksum, String acceptedMetaVersionRange) throws IOException {
        registerExpressionMetamodel(resourceSet);
        Resource resource = resourceSet.createResource(uri);
        Map<Object, Object> loadOptions = new HashMap<>();
        //loadOptions.put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, Boolean.TRUE);
        //loadOptions.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
        loadOptions.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
        loadOptions.put(XMLResource.OPTION_LAX_FEATURE_PROCESSING, Boolean.TRUE);
        loadOptions.put(XMLResource.OPTION_PROCESS_DANGLING_HREF, XMLResource.OPTION_PROCESS_DANGLING_HREF_DISCARD);
        resource.load(loadOptions);

        ExpressionModel.ExpressionModelBuilder b = ExpressionModel.buildExpressionModel();

        b.name(name)
                .version(version)
                .uri(uri)
                .checksum(checksum)
                .resourceSet(resourceSet);

        if (checksum != null) {
            b.checksum(checksum);
        }

        if (acceptedMetaVersionRange != null)  {
            b.metaVersionRange(acceptedMetaVersionRange);
        }
        return b.build();
    }

    public static Map<Object, Object> getExpressionDefaultSaveOptions() {
        final Map<Object, Object> saveOptions = new HashMap<>();
        saveOptions.put(XMIResource.OPTION_DECLARE_XML,Boolean.TRUE);
        saveOptions.put(XMIResource.OPTION_PROCESS_DANGLING_HREF,XMIResource.OPTION_PROCESS_DANGLING_HREF_DISCARD);
        saveOptions.put(XMIResource.OPTION_URI_HANDLER, new URIHandlerImpl() {
            @Override
            public URI deresolve(URI uri) {
                if (uri.hasFragment() && uri.hasOpaquePart() && baseURI.hasOpaquePart()) {
                    if (uri.opaquePart().equals(baseURI.opaquePart())) {
                        return URI.createURI("#" + uri.fragment());
                    }
                }
                return super.deresolve(uri);
            }
        });
        saveOptions.put(XMIResource.OPTION_SCHEMA_LOCATION,Boolean.TRUE);
        saveOptions.put(XMIResource.OPTION_DEFER_IDREF_RESOLUTION,Boolean.TRUE);
        saveOptions.put(XMIResource.OPTION_SKIP_ESCAPE_URI,Boolean.FALSE);
        saveOptions.put(XMIResource.OPTION_ENCODING,"UTF-8");
        return saveOptions;
    }


    public static void saveExpressionModel(ExpressionModel expressionModel) throws IOException {
        expressionModel.getResourceSet().getResource(expressionModel.getUri(), false).save(getExpressionDefaultSaveOptions());
    }

}
