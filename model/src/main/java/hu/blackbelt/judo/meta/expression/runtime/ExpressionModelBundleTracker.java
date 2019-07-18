package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.osgi.utils.osgi.api.BundleCallback;
import hu.blackbelt.osgi.utils.osgi.api.BundleTrackerManager;
import hu.blackbelt.osgi.utils.osgi.api.BundleUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.osgi.framework.*;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModelLoader.loadExpressionModel;

@Component(immediate = true)
@Slf4j
public class ExpressionModelBundleTracker {

    public static final String EXPRESSION_MODELS = "Expression-Models";

    @Reference
    BundleTrackerManager bundleTrackerManager;

    Map<String, ServiceRegistration<ExpressionModel>> registrations = new ConcurrentHashMap<>();

    Map<String, ExpressionModel> models = new HashMap<>();

    @Activate
    public void activate(final ComponentContext componentContext) {
        bundleTrackerManager.registerBundleCallback(this.getClass().getName(),
                new ExpressionRegisterCallback(componentContext.getBundleContext()),
                new ExpressionUnregisterCallback(componentContext.getBundleContext()),
                new ExpressionBundlePredicate());
    }

    @Deactivate
    public void deactivate(final ComponentContext componentContext) {
        bundleTrackerManager.unregisterBundleCallback(this.getClass().getName());
    }

    private static class ExpressionBundlePredicate implements Predicate<Bundle> {
        @Override
        public boolean test(Bundle trackedBundle) {
            return BundleUtil.hasHeader(trackedBundle, EXPRESSION_MODELS);
        }
    }

    private class ExpressionRegisterCallback implements BundleCallback {

        BundleContext bundleContext;

        public ExpressionRegisterCallback(BundleContext bundleContext) {
            this.bundleContext = bundleContext;
        }

        @Override
        public void accept(Bundle trackedBundle) {
            List<Map<String, String>> entries = BundleUtil.getHeaderEntries(trackedBundle, EXPRESSION_MODELS);


            for (Map<String, String> params : entries) {
                String key = params.get(ExpressionModel.NAME);
                if (registrations.containsKey(key)) {
                    log.error("Model already loaded: " + key);
                } else {
                    if (params.containsKey(ExpressionModel.META_VERSION_RANGE)) {
                        VersionRange versionRange = new VersionRange(params.get(ExpressionModel.META_VERSION_RANGE).replaceAll("\"", ""));
                        if (versionRange.includes(bundleContext.getBundle().getVersion())) {
                            // Unpack model
                            try {
                                File file = BundleUtil.copyBundleFileToPersistentStorage(trackedBundle, key + ".judo-meta-expression", params.get("file"));
                                Version version = bundleContext.getBundle().getVersion();
                                ExpressionModel expressionModel = loadExpressionModel(
                                        new ResourceSetImpl(),
                                        URI.createURI(file.getAbsolutePath()),
                                        params.get(ExpressionModel.NAME),
                                        version.toString(),
                                        params.get(ExpressionModel.CHECKSUM),
                                        versionRange.toString());

                                log.info("Registering model: " + expressionModel);

                                ServiceRegistration<ExpressionModel> modelServiceRegistration = bundleContext.registerService(ExpressionModel.class, expressionModel, expressionModel.toDictionary());
                                models.put(key, expressionModel);
                                registrations.put(key, modelServiceRegistration);

                            } catch (IOException e) {
                                log.error("Could not load model: " + params.get(ExpressionModel.NAME) + " from bundle: " + trackedBundle.getBundleId());
                            }
                        }
                    }
                }
            }
        }

        @Override
        public Thread process(Bundle bundle) {
            return null;
        }
    }

    private class ExpressionUnregisterCallback implements BundleCallback {
        BundleContext bundleContext;

        public ExpressionUnregisterCallback(BundleContext bundleContext) {
            this.bundleContext = bundleContext;
        }

        @Override
        public void accept(Bundle trackedBundle) {
            List<Map<String, String>> entries = BundleUtil.getHeaderEntries(trackedBundle, EXPRESSION_MODELS);
            for (Map<String, String> params : entries) {
                String key = params.get(ExpressionModel.NAME);

                if (models.containsKey(key)) {
                    ServiceRegistration<ExpressionModel> modelServiceRegistration = registrations.get(key);

                    if (modelServiceRegistration != null) {
                        log.info("Unregistering moodel: " + models.get(key));
                        modelServiceRegistration.unregister();
                        registrations.remove(key);
                        models.remove(key);
                    }
                } else {
                    log.error("Model is not registered: " + key);
                }
            }
        }

        @Override
        public Thread process(Bundle bundle) {
            return null;
        }
    }

}
