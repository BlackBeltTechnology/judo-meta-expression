package hu.blackbelt.judo.meta.expression.osgi.itest;

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

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionModel;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.ExpressionValidationException;
//import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
//import hu.blackbelt.judo.meta.psm.runtime.PsmModel.PsmValidationException;
//import hu.blackbelt.judo.meta.psm.runtime.PsmModel.SaveArguments;
import hu.blackbelt.osgi.utils.osgi.api.BundleTrackerManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.*;

import javax.inject.Inject;
import java.io.*;

//import static hu.blackbelt.judo.meta.expression.adapters.psm.ExpressionEpsilonValidatorOnPsm.validateExpressionOnPsm;
import static hu.blackbelt.judo.meta.expression.osgi.itest.KarafFeatureProvider.karafConfig;
import static hu.blackbelt.judo.meta.expression.runtime.ExpressionEpsilonValidator.calculateExpressionValidationScriptURI;
import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.buildExpressionModel;
//import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.buildPsmModel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.swissbox.core.BundleUtils.getBundle;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;
import static org.ops4j.pax.tinybundles.core.TinyBundles.withBnd;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@Slf4j
public class ExpressionBundleITest {

    public static final String HU_BLACKBELT_JUDO_META_EXPRESSION_OSGI = "hu.blackbelt.judo.meta.expression.osgi";
    public static final String HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_MEASURE = "hu.blackbelt.judo.meta.expression.model.adapter.measure";
    public static final String HU_BLACKBELT_JUDO_META = "hu.blackbelt.judo.meta";
//    public static final String HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_PSM = "hu.blackbelt.judo.meta.expression.model.adapter.psm";
//    public static final String HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_ASM = "hu.blackbelt.judo.meta.expression.model.adapter.asm";
    public static final String HU_BLACKBELT_JUDO_META_MEASURE_OSGI = "hu.blackbelt.judo.meta.measure.osgi";
    public static final String HU_BLACKBELT_JUDO_META_PSM_OSGI = "hu.blackbelt.judo.meta.psm.osgi";
    public static final String HU_BLACKBELT_JUDO_META_ASM_OSGI = "hu.blackbelt.judo.meta.asm.osgi";

  //  private static final String DEMO_PSM = "northwind-psm";
    private static final String DEMO_EXPRESSION = "t002";

    @Inject
    protected BundleTrackerManager bundleTrackerManager;

    @Inject
    BundleContext bundleContext;
    
//    @Inject
//    PsmModel psmModel;
    
    @Inject
    ExpressionModel expressionModel;

    @Configuration
    public Option[] config() throws IOException, ExpressionValidationException { // //, PsmValidationException, 

        return combine(karafConfig(this.getClass()),
                mavenBundle(maven()
                        .groupId(HU_BLACKBELT_JUDO_META)
                        .artifactId(HU_BLACKBELT_JUDO_META_EXPRESSION_OSGI)
                        .versionAsInProject()),

        /*
                mavenBundle(maven()
                        .groupId(HU_BLACKBELT_JUDO_META)
                        .artifactId(HU_BLACKBELT_JUDO_META_ASM_OSGI)
                        .versionAsInProject()),

                mavenBundle(maven()
                        .groupId(HU_BLACKBELT_JUDO_META)
                        .artifactId(HU_BLACKBELT_JUDO_META_PSM_OSGI)
                        .versionAsInProject()),

                */
                mavenBundle(maven()
                        .groupId(HU_BLACKBELT_JUDO_META)
                        .artifactId(HU_BLACKBELT_JUDO_META_MEASURE_OSGI)
                        .versionAsInProject()),

                /*
                mavenBundle(maven()
                        .groupId(HU_BLACKBELT_JUDO_META)
                        .artifactId(HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_ASM)
                        .versionAsInProject()),

                mavenBundle(maven()
                        .groupId(HU_BLACKBELT_JUDO_META)
                        .artifactId(HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_PSM)
                        .versionAsInProject()),
                */
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

        /*
        assertNotNull(getBundle(bundleContext, HU_BLACKBELT_JUDO_META_ASM_OSGI));
        assertEquals(Bundle.ACTIVE, getBundle(bundleContext, HU_BLACKBELT_JUDO_META_ASM_OSGI)
                .getState());

        assertNotNull(getBundle(bundleContext, HU_BLACKBELT_JUDO_META_PSM_OSGI));
        assertEquals(Bundle.ACTIVE, getBundle(bundleContext, HU_BLACKBELT_JUDO_META_PSM_OSGI)
                .getState());
        */
        assertNotNull(getBundle(bundleContext, HU_BLACKBELT_JUDO_META_MEASURE_OSGI));
        assertEquals(Bundle.ACTIVE, getBundle(bundleContext, HU_BLACKBELT_JUDO_META_MEASURE_OSGI)
                .getState());

        /*
        assertNotNull(getBundle(bundleContext, HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_ASM));
        assertEquals(Bundle.ACTIVE, getBundle(bundleContext, HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_ASM)
                .getState());

        assertNotNull(getBundle(bundleContext, HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_PSM));
        assertEquals(Bundle.ACTIVE, getBundle(bundleContext, HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_PSM)
                .getState());
        */
        assertNotNull(getBundle(bundleContext, HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_MEASURE));
        assertEquals(Bundle.ACTIVE, getBundle(bundleContext, HU_BLACKBELT_JUDO_META_EXPRESSION_MODEL_ADAPTER_MEASURE)
                .getState());

    }
    
    public Option getProvisonModelBundle() throws IOException, ExpressionValidationException { // PsmValidationException, 
        return provision(
//                getPsmModelBundle(),
                getExpressionModelBundle()
        );
    }

    /*
    private InputStream getPsmModelBundle() throws IOException, PsmValidationException {
    	
    	PsmModel psmModel = buildPsmModel()
                .name(DEMO_PSM)
                .build();
    	
    	ByteArrayOutputStream os = new ByteArrayOutputStream();
    	
    	psmModel.savePsmModel(SaveArguments.psmSaveArgumentsBuilder().outputStream(os));
 	
        return bundle()
                .add( "model/" + DEMO_PSM + ".judo-meta-psm",
                		new ByteArrayInputStream(os.toByteArray()))
                .set( Constants.BUNDLE_MANIFESTVERSION, "2")
                .set( Constants.BUNDLE_SYMBOLICNAME, DEMO_PSM + "-psm" )
                .set( "Psm-Models", "file=model/" + DEMO_PSM + ".judo-meta-psm;name=" + DEMO_PSM)
                .build( withBnd());
    }
    */
    
    private InputStream getExpressionModelBundle() throws IOException, ExpressionValidationException {
    	
    	ExpressionModel expressionModel = buildExpressionModel()
                .name(DEMO_EXPRESSION)
                .build();
    	
    	ByteArrayOutputStream os = new ByteArrayOutputStream();
    	
    	expressionModel.saveExpressionModel(hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.SaveArguments
    			.expressionSaveArgumentsBuilder().outputStream(os));
    	
        return bundle()
                .add( "model/" + DEMO_EXPRESSION + ".judo-meta-expression",
                		new ByteArrayInputStream(os.toByteArray()))
                .set( Constants.BUNDLE_MANIFESTVERSION, "2")
                .set( Constants.BUNDLE_SYMBOLICNAME, DEMO_EXPRESSION + "-expression" )
                .set( "Expression-Models", "file=model/" + DEMO_EXPRESSION + ".judo-meta-expression;name=" + DEMO_EXPRESSION)
                .build( withBnd());
    }
    
    /*
    @Test
    public void testModelValidation() throws Exception {
        try (Log bufferedLog = new BufferedSlf4jLogger(log)) {
            validateExpressionOnPsm(bufferedLog, psmModel, expressionModel, calculateExpressionValidationScriptURI());
        }
    }
    */
}
