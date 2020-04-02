package hu.blackbelt.judo.meta.expression.osgi.itest;

import hu.blackbelt.epsilon.runtime.execution.exceptions.ScriptExecutionException;
import hu.blackbelt.epsilon.runtime.execution.impl.StringBuilderLogger;
import hu.blackbelt.judo.meta.expression.adapters.psm.ExpressionEpsilonValidatorOnPsm;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionEpsilonValidator;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.osgi.utils.osgi.api.BundleTrackerManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.log.LogService;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;

import static hu.blackbelt.judo.meta.expression.osgi.itest.ExpressionKarafFeatureProvider.getRuntimeFeaturesForMetamodel;
import static hu.blackbelt.judo.meta.expression.osgi.itest.ExpressionKarafFeatureProvider.testTargetDir;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.swissbox.core.BundleUtils.getBundle;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;
import static org.ops4j.pax.tinybundles.core.TinyBundles.withBnd;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class ExpressionBundleITest {

    public static final String HU_BLACKBELT_JUDO_META_EXPRESSION_OSGI = "hu.blackbelt.judo.meta.expression.osgi";
    public static final String HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_MEASURE = "hu.blackbelt.judo.meta.expression.model.adapter.measure";
    public static final String HU_BLACKBELT_JUDO_META = "hu.blackbelt.judo.meta";
    public static final String HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_PSM = "hu.blackbelt.judo.meta.expression.model.adapter.psm";
    public static final String HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_ASM = "hu.blackbelt.judo.meta.expression.model.adapter.asm";
    public static final String HU_BLACKBELT_JUDO_META_MEASURE_OSGI = "hu.blackbelt.judo.meta.measure.osgi";
    public static final String HU_BLACKBELT_JUDO_META_PSM_OSGI = "hu.blackbelt.judo.meta.psm.osgi";
    public static final String HU_BLACKBELT_JUDO_META_ASM_OSGI = "hu.blackbelt.judo.meta.asm.osgi";

    private static final String DEMO_PSM = "northwind-psm";
    private static final String DEMO_EXPRESSION = "t002";
    @Inject
    LogService log;

    @Inject
    protected BundleTrackerManager bundleTrackerManager;

    @Inject
    BundleContext bundleContext;
    
    @Inject
    PsmModel psmModel;
    
    @Inject
    ExpressionModel expressionModel;

    @Configuration
    public Option[] config() throws FileNotFoundException {

        return combine(getRuntimeFeaturesForMetamodel(this.getClass()),
                mavenBundle(maven()
                        .groupId(HU_BLACKBELT_JUDO_META)
                        .artifactId(HU_BLACKBELT_JUDO_META_EXPRESSION_OSGI)
                        .versionAsInProject()),

                mavenBundle(maven()
                        .groupId(HU_BLACKBELT_JUDO_META)
                        .artifactId(HU_BLACKBELT_JUDO_META_ASM_OSGI)
                        .versionAsInProject()),

                mavenBundle(maven()
                        .groupId(HU_BLACKBELT_JUDO_META)
                        .artifactId(HU_BLACKBELT_JUDO_META_PSM_OSGI)
                        .versionAsInProject()),

                mavenBundle(maven()
                        .groupId(HU_BLACKBELT_JUDO_META)
                        .artifactId(HU_BLACKBELT_JUDO_META_MEASURE_OSGI)
                        .versionAsInProject()),

                mavenBundle(maven()
                        .groupId(HU_BLACKBELT_JUDO_META)
                        .artifactId(HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_ASM)
                        .versionAsInProject()),

                mavenBundle(maven()
                        .groupId(HU_BLACKBELT_JUDO_META)
                        .artifactId(HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_PSM)
                        .versionAsInProject()),

                mavenBundle(maven()
                        .groupId(HU_BLACKBELT_JUDO_META)
                        .artifactId(HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_MEASURE)
                        .versionAsInProject()),
                getProvisonModelBundle()

        );
    }

    @Test
    public void testBundleActive() {
        assertNotNull(getBundle(bundleContext, HU_BLACKBELT_JUDO_META_EXPRESSION_OSGI));
        assertEquals(Bundle.ACTIVE, getBundle(bundleContext, HU_BLACKBELT_JUDO_META_EXPRESSION_OSGI)
                .getState());

        assertNotNull(getBundle(bundleContext, HU_BLACKBELT_JUDO_META_ASM_OSGI));
        assertEquals(Bundle.ACTIVE, getBundle(bundleContext, HU_BLACKBELT_JUDO_META_ASM_OSGI)
                .getState());

        assertNotNull(getBundle(bundleContext, HU_BLACKBELT_JUDO_META_PSM_OSGI));
        assertEquals(Bundle.ACTIVE, getBundle(bundleContext, HU_BLACKBELT_JUDO_META_PSM_OSGI)
                .getState());

        assertNotNull(getBundle(bundleContext, HU_BLACKBELT_JUDO_META_MEASURE_OSGI));
        assertEquals(Bundle.ACTIVE, getBundle(bundleContext, HU_BLACKBELT_JUDO_META_MEASURE_OSGI)
                .getState());

        assertNotNull(getBundle(bundleContext, HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_ASM));
        assertEquals(Bundle.ACTIVE, getBundle(bundleContext, HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_ASM)
                .getState());

        assertNotNull(getBundle(bundleContext, HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_PSM));
        assertEquals(Bundle.ACTIVE, getBundle(bundleContext, HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_PSM)
                .getState());

        assertNotNull(getBundle(bundleContext, HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_MEASURE));
        assertEquals(Bundle.ACTIVE, getBundle(bundleContext, HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_MEASURE)
                .getState());

    }
    
    public Option getProvisonModelBundle() throws FileNotFoundException {
        return provision(
                getPsmModelBundle(),
                getExpressionModelBundle()
        );
    }

    
    private InputStream getPsmModelBundle() throws FileNotFoundException {
        return bundle()
                .add( "model/" + DEMO_PSM + ".judo-meta-psm",
                        new FileInputStream(new File(testTargetDir(getClass()).getAbsolutePath(),  "northwind-psm.model")))
                .set( Constants.BUNDLE_MANIFESTVERSION, "2")
                .set( Constants.BUNDLE_SYMBOLICNAME, DEMO_PSM + "-psm" )
                .set( "Psm-Models", "file=model/" + DEMO_PSM + ".judo-meta-psm;version=1.0.0;name=" + DEMO_PSM + ";checksum=notset;meta-version-range=\"[1.0.0,2)\"")
                .build( withBnd());
    }
    
    private InputStream getExpressionModelBundle() throws FileNotFoundException {
        return bundle()
                .add( "model/" + DEMO_EXPRESSION + ".judo-meta-expression",
                        new FileInputStream(new File(testTargetDir(getClass()).getAbsolutePath(),  "t002.model")))
                .set( Constants.BUNDLE_MANIFESTVERSION, "2")
                .set( Constants.BUNDLE_SYMBOLICNAME, DEMO_EXPRESSION + "-expression" )
                .set( "Expression-Models", "file=model/" + DEMO_EXPRESSION + ".judo-meta-expression;version=1.0.0;name=" + DEMO_EXPRESSION + ";checksum=notset;meta-version-range=\"[1.0.0,2)\"")
                .build( withBnd());
    }
    
    @Test
    public void testModelValidation() throws ScriptExecutionException, URISyntaxException {
        StringBuilderLogger logger = new StringBuilderLogger(StringBuilderLogger.LogLevel.DEBUG);

        ExpressionEpsilonValidatorOnPsm.validateExpressionOnPsm(logger,
                psmModel, expressionModel,
                ExpressionEpsilonValidator.calculateExpressionValidationScriptURI());
    }
}
