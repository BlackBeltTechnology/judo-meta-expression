package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;
import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.api.ModelContext;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.*;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.ProgramParameter.programParameterBuilder;
import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModelLoader.createExpressionResourceSet;

abstract class ExecutionContextTest {

    protected List<ModelContext> modelContexts = new ArrayList<>();

    protected final Log log = new Slf4jLog();

    void tearDown() {
        modelContexts.clear();
    }

    void execute() throws Exception {
        final ResourceSet executionResourceSet = createExpressionResourceSet();

        // Execution context
        ExecutionContext executionContext = executionContextBuilder()
                .log(log)
                .resourceSet(executionResourceSet)
                .metaModels(getMetaModels())
                .modelContexts(modelContexts)
                .sourceDirectory(scriptDir())
                .build();

        // run the model / metadata loading
        executionContext.load();

        // Transformation script
        executionContext.executeProgram(
                evlExecutionContextBuilder()
                        .source(getEvlSource())
                        .parameters(ImmutableList.of(programParameterBuilder()
                                .name("extendedMetadataURI")
                                .value(AsmUtils.extendedMetadataUri)
                                .build()))
                        .expectedErrors(getExpectedErrors())
                        .expectedWarnings(getExpectedWarnings())
                        .build());

        executionContext.commit();
        executionContext.close();
    }

    protected File scriptDir() {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath + "../../src/test");
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }

    protected File srcDir() {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath + "../../src");
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }

    protected List<String> getMetaModels() {
        return ImmutableList.of();
    }

    protected abstract Resource getExpressionResource() throws Exception;

    protected abstract String getEvlSource();

    protected List<String> getExpectedErrors() {
        return null;
    }

    protected List<String> getExpectedWarnings() {
        return null;
    }
}
