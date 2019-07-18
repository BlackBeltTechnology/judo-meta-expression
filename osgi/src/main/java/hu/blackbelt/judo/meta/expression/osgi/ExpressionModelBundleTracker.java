package hu.blackbelt.judo.meta.expression.osgi;

import hu.blackbelt.epsilon.runtime.osgi.BundleURIHandler;
import hu.blackbelt.osgi.utils.osgi.api.BundleCallback;
import hu.blackbelt.osgi.utils.osgi.api.BundleTrackerManager;
import hu.blackbelt.osgi.utils.osgi.api.BundleUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.VersionRange;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Component(immediate = true)
@Slf4j
public class ExpressionModelBundleTracker {

    public static final String EXPRESSION_MODELS = "Expression-Models";

    @Reference
    BundleTrackerManager bundleTrackerManager;

    Map<String, ServiceRegistration<ExpressionModel>> expressionModelRegistrations = new ConcurrentHashMap<>();

    Map<String, ExpressionModel> expressionModels = new HashMap<>();

    @Activate
    public void activate(final ComponentContext componentContext) {
        bundleTrackerManager.registerBundleCallback(this.getClass().getName(),
                new ExpressionRegisterCallback(componentContext.getBundleContext()),
                new ExpressionUnregisterCallback(),
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
                if (expressionModelRegistrations.containsKey(key)) {
                    log.error("Expression model already loaded: " + key);
                } else {
                    if (params.containsKey(ExpressionModel.META_VERSION_RANGE)) {
                        VersionRange versionRange = new VersionRange(params.get(ExpressionModel.META_VERSION_RANGE).replaceAll("\"", ""));
                        if (versionRange.includes(bundleContext.getBundle().getVersion())) {
                            // Unpack model
                            try {
                                        ExpressionModel expressionModel = ExpressionModel.loadExpressionModel(
                                        ExpressionModel.LoadArguments.loadArgumentsBuilder()
                                                .uriHandler(Optional.of(new BundleURIHandler("urn", "", trackedBundle)))
                                                .uri(URI.createURI(params.get("file")))
                                                .name(params.get(ExpressionModel.NAME))
                                                .version(Optional.of(trackedBundle.getVersion().toString()))
                                                .checksum(Optional.ofNullable(params.get(ExpressionModel.CHECKSUM)))
                                                .acceptedMetaVersionRange(Optional.of(versionRange.toString()))
                                                .build()
                                );

                                log.info("Registering Expression model: " + expressionModel);

                                ServiceRegistration<ExpressionModel> modelServiceRegistration = bundleContext.registerService(ExpressionModel.class, expressionModel, expressionModel.toDictionary());
                                expressionModels.put(key, expressionModel);
                                expressionModelRegistrations.put(key, modelServiceRegistration);

                            } catch (IOException e) {
                                log.error("Could not load Expression model: " + params.get(ExpressionModel.NAME) + " from bundle: " + trackedBundle.getBundleId());
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

        @Override
        public void accept(Bundle trackedBundle) {
            List<Map<String, String>> entries = BundleUtil.getHeaderEntries(trackedBundle, EXPRESSION_MODELS);
            for (Map<String, String> params : entries) {
                String key = params.get(ExpressionModel.NAME);

                if (expressionModels.containsKey(key)) {
                    ServiceRegistration<ExpressionModel> modelServiceRegistration = expressionModelRegistrations.get(key);

                    if (modelServiceRegistration != null) {
                        log.info("Unregistering Expression model: " + expressionModels.get(key));
                        modelServiceRegistration.unregister();
                        expressionModelRegistrations.remove(key);
                        expressionModels.remove(key);
                    }
                } else {
                    log.error("Expression Model is not registered: " + key);
                }
            }
        }

        @Override
        public Thread process(Bundle bundle) {
            return null;
        }
    }

}
