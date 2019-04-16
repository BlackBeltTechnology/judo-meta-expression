package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;
import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.asm.runtime.AsmModel;
import hu.blackbelt.judo.meta.asm.runtime.AsmModelLoader;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModelLoader;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;
import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModelLoader.createExpressionResourceSet;

class MinimalExecutionContextTest {

    @Test
    void testReflectiveCreated() throws Exception {

        final String createdSourceModelName = "urn:expression.judo-meta-expression";

        final ResourceSet executionResourceSet = createExpressionResourceSet();
        final Resource expressionResource = executionResourceSet.createResource(
                URI.createURI(createdSourceModelName));

        final ResourceSet measureModelResourceSet = MeasureModelLoader.createMeasureResourceSet();
        final MeasureModel measureModel = MeasureModelLoader.loadMeasureModel(measureModelResourceSet,
                URI.createURI(new File(srcDir(), "test/models/measure.model").getAbsolutePath()),
                "test",
                "1.0.0");

        final ResourceSet asmModelResourceSet = AsmModelLoader.createAsmResourceSet();
        final AsmModel asmModel = AsmModelLoader.loadAsmModel(asmModelResourceSet,
                URI.createURI(new File(srcDir(), "test/models/northwind-asm.model").getAbsolutePath()),
                "test",
                "1.0.0");

        expressionResource.getContents().add(newIntegerConstantBuilder().withValue(BigInteger.valueOf(10)).build());
        expressionResource.getContents().add(newDecimalConstantBuilder().withValue(BigDecimal.valueOf(3.14)).build());
        expressionResource.getContents().add(newStringConstantBuilder().withValue("Sample text").build());
        expressionResource.getContents().add(newBooleanConstantBuilder().withValue(true).build());
        expressionResource.getContents().add(newLiteralBuilder().withValue("RED").build());
        final MeasureName measureName = newMeasureNameBuilder().withNamespace("Template::measures").withName("Mass").build();
        expressionResource.getContents().add(measureName);
        expressionResource.getContents().add(newMeasuredDecimalBuilder()
                .withMeasure(measureName)
                .withValue(BigDecimal.valueOf(10))
                .withUnitName("kilogram")
                .build());

        Log log = new Slf4jLog();

        // Execution context
        ExecutionContext executionContext = executionContextBuilder()
                .log(log)
                .resourceSet(executionResourceSet)
                .metaModels(ImmutableList.of())
                .modelContexts(ImmutableList.of(
                        wrappedEmfModelContextBuilder()
                                .log(log)
                                .name("ASM")
                                .resource(asmModelResourceSet.getResources().get(0))
                                .build(),
                        wrappedEmfModelContextBuilder()
                                .log(log)
                                .name("MEASURES")
                                .resource(measureModelResourceSet.getResources().get(0))
                                .build(),
                        wrappedEmfModelContextBuilder()
                                .log(log)
                                .name("EXPR")
                                .resource(expressionResource)
                                .build()))
                .sourceDirectory(scriptDir())
                .build();

        // run the model / metadata loading
        executionContext.load();

        // Transformation script
        executionContext.executeProgram(
                evlExecutionContextBuilder()
                        .source("epsilon/validations/expressionOnAsm.evl")
                        .build());

        executionContext.commit();
        executionContext.close();
    }

    public File scriptDir() {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath + "../../src/test");
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }

    public File srcDir() {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath + "../../src");
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }
}
