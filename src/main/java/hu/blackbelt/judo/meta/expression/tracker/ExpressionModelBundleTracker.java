package hu.blackbelt.judo.meta.expression.tracker;

import hu.blackbelt.judo.meta.expression.ExpressionMetaModel;
import hu.blackbelt.judo.meta.expression.ExpressionModelInfo;
import hu.blackbelt.osgi.utils.osgi.api.BundleCallback;
import hu.blackbelt.osgi.utils.osgi.api.BundleTrackerManager;
import hu.blackbelt.osgi.utils.osgi.api.BundleUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
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

@Component(immediate = true)
@Slf4j
public class ExpressionModelBundleTracker {

    public static final String EXPRESSION_MODELS = "Expression-Models";

    @Reference
    BundleTrackerManager bundleTrackerManager;

    @Reference
    ExpressionMetaModel expressionMetaModel;

    Map<String, ServiceRegistration<ExpressionModelInfo>> expressionRegistrations = new ConcurrentHashMap<>();
    Map<String, ExpressionModelInfo> expressionModels = new HashMap<>();

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
                if (params.containsKey(ExpressionModelInfo.META_VERSION)) {
                    VersionRange versionRange = new VersionRange(params.get(ExpressionModelInfo.META_VERSION).replaceAll("\"", ""));
                    if (versionRange.includes(bundleContext.getBundle().getVersion())) {

                        // Unpack model
                        try {
                            String key = trackedBundle.getBundleId() + "-" + params.get(ExpressionModelInfo.NAME);

                            File file = BundleUtil.copyBundleFileToPersistentStorage(trackedBundle, key + ".model", params.get(ExpressionModelInfo.FILE));

                            ExpressionModelInfo expressionModelInfo = new ExpressionModelInfo(
                                    file,
                                    params.get(ExpressionModelInfo.NAME),
                                    new Version(params.get(ExpressionModelInfo.VERSION)),
                                    URI.createURI(file.getAbsolutePath()),
                                    params.get(ExpressionModelInfo.CHECKSUM),
                                    versionRange);

                            log.info("Registering model: " + expressionModelInfo);

                            ServiceRegistration<ExpressionModelInfo> modelServiceRegistration = bundleContext.registerService(ExpressionModelInfo.class, expressionModelInfo, expressionModelInfo.toDictionary());
                            expressionModels.put(key, expressionModelInfo);
                            expressionRegistrations.put(key, modelServiceRegistration);

                        } catch (IOException e) {
                            log.error("Could not load model: " + params.get(ExpressionModelInfo.NAME) + " from bundle: " + trackedBundle.getBundleId());
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
                VersionRange versionRange = new VersionRange(params.get(ExpressionModelInfo.META_VERSION).replaceAll("\"", ""));
                if (params.containsKey(ExpressionModelInfo.META_VERSION)) {
                    if (versionRange.includes(bundleContext.getBundle().getVersion())) {
                        String key = trackedBundle.getBundleId() + "-" + params.get(ExpressionModelInfo.NAME);
                        ServiceRegistration<ExpressionModelInfo> modelServiceRegistration = expressionRegistrations.get(key);

                        if (modelServiceRegistration != null) {
                            log.info("Unregistering moodel: " + expressionModels.get(key));
                            modelServiceRegistration.unregister();
                            expressionRegistrations.remove(key);
                            expressionModels.remove(key);
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
}
