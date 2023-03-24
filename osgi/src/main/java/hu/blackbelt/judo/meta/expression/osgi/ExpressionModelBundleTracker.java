package hu.blackbelt.judo.meta.expression.osgi;

/*-
 * #%L
 * JUDO :: Expression :: Model
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import hu.blackbelt.judo.meta.expression.runtime.ExpressionModel;
import hu.blackbelt.osgi.utils.osgi.api.BundleCallback;
import hu.blackbelt.osgi.utils.osgi.api.BundleTrackerManager;
import hu.blackbelt.osgi.utils.osgi.api.BundleUtil;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.VersionRange;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.LoadArguments.expressionLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.loadExpressionModel;
import static java.util.Optional.ofNullable;

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
                    // Unpack model
                    try {
                        ExpressionModel expressionModel = loadExpressionModel(expressionLoadArgumentsBuilder()
                                .inputStream(trackedBundle.getEntry(params.get("file")).openStream())
                                .name(params.get(ExpressionModel.NAME))
                                .version(trackedBundle.getVersion().toString()));

                        log.info("Registering Expression model: " + expressionModel);

                        ServiceRegistration<ExpressionModel> modelServiceRegistration = bundleContext.registerService(ExpressionModel.class, expressionModel, expressionModel.toDictionary());
                        expressionModels.put(key, expressionModel);
                        expressionModelRegistrations.put(key, modelServiceRegistration);

                    } catch (IOException | ExpressionModel.ExpressionValidationException e) {
                        log.error("Could not load Expression model: " + params.get(ExpressionModel.NAME) + " from bundle: " + trackedBundle.getBundleId(), e);
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
